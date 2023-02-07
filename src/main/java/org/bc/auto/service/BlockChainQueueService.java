package org.bc.auto.service;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.bc.auto.dao.BCChannelOrgMapper;
import org.bc.auto.dao.BCChannelOrgPeerMapper;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.listener.BlockChainEvent;
import org.bc.auto.listener.BlockChainFabricNodeListener;
import org.bc.auto.listener.source.*;
import org.bc.auto.model.entity.*;

import org.bc.auto.service.system.CertService;
import org.bc.auto.service.system.NodeService;
import org.bc.auto.service.system.OrgService;
import org.bc.auto.utils.BlockChainShellQueueUtils;
import org.bc.auto.utils.HyperledgerFabricComponentsStartUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 判断类与实例的关系有以下三种方式
 * <p>
 * 1、instanceOf         关键字，用来判断对象是否是类的实例
 * 2、isInstance         方法，用来判断对象是否属于某个类型的实例
 * 3、isAssignableFrom，  用来判断类型间是否存在派生关系
 */
@Slf4j
@Service
public class BlockChainQueueService {

    @Resource
    private BCClusterMapper bcClusterMapper;

    @Resource
    private CertService certService;

    @Resource
    private NodeService nodeService;

    @Resource
    private OrgService orgService;

    @Resource
    private BCChannelOrgMapper bcChannelOrgMapper;

    @Resource
    private BCChannelOrgPeerMapper bcChannelOrgPeerMapper;


    public void run() {
        //开始轮询等待任务加入任务队列
        while (true) {
            BlockChainEventSource blockChainEventSource = null;
            try {
                //获取队列中的元素
                blockChainEventSource = BlockChainShellQueueUtils.peek();
            } catch (InterruptedException e) {
                log.info("[queue->exception] get the element error from shell queue，maybe system exception. please check it. exception info: \n", e);
            }

            //获取队列中元素的对象类型
            //根据元素中的对象类型进行相对应的业务处理
            String className = BlockChainShellQueueUtils.getElementClassName(blockChainEventSource);
            switch (className) {
                //如果是组织的类型，进行组织的脚本执行
                case "FabricOrgEventSource": {
                    log.debug("[queue->org] element's type is org，this is to get the org cert.");
                    //获取集群对象，以获取更多的集群信息
                    //并对返回的结果进行判断
                    FabricOrgEventSource chainFabricOrgEventSource = (FabricOrgEventSource) blockChainEventSource;
                    BCOrg bcOrg = chainFabricOrgEventSource.getBcOrg();
                    BCCluster bcCluster = bcClusterMapper.getClusterById(bcOrg.getClusterId());
                    BCCert bcCert = HyperledgerFabricComponentsStartUtils.generateOrgCerts(bcCluster, bcOrg);
                    //如果成功申请则进行组织的状态修改，并入库。
                    //这里需要进行事务操作：
                    //  shell执行成功之后，是没有办法回滚操作的，即非原子性操作，必须达到最终一致性。可以采用补偿的方式。
                    //  正式生产环境中可以采用消息队列，针对消息队列的消息进行最后确认以及补偿。
                    //  当把证书添加至数据库中，意味着组织创建成功。
                    certService.insertBCCert(bcCert);
                    break;
                }
                case "FabricNodeEventSource": {
                    log.debug("[queue->node] element's type is node，this is to get the node cert.");
                    FabricNodeEventSource<BCNode> bcNodeBlockChainArrayList = (FabricNodeEventSource<BCNode>) blockChainEventSource;
                    List<BCNode> bcNodeList = bcNodeBlockChainArrayList.getEList();
                    //为节点申请节点证书
                    //节点证书需要进行托管，节点证书要进行节点连接等操作。
                    for (int i = 0; i < bcNodeList.size(); i++) {
                        BCNode bcNode = bcNodeList.get(i);
                        //节点开始的时候生成证书
                        BCCluster bcCluster = bcClusterMapper.getClusterById(bcNode.getClusterId());
                        HyperledgerFabricComponentsStartUtils.generateNodeCerts(bcCluster, bcNode);
                    }

                    //监听节点事件，如果是orderer节点的情况下。
                    //需要创建创世区块等文件
                    if (bcNodeList.get(0).getNodeType() == 1) {
                        log.debug("[queue->node] element's type is node and node type is orderer，need to create genesis.block file");
                        BCCluster bcCluster = bcClusterMapper.getClusterById(bcNodeList.get(0).getClusterId());
                        BCOrg bcOrg = orgService.getOrgByOrgId(bcNodeList.get(0).getOrgId());
                        HyperledgerFabricComponentsStartUtils.buildFabricChain(bcCluster, bcOrg);
                    }

                    //启动节点
                    //通知K8S启动对应的pod节点,发布监听
                    new BlockChainEvent(new BlockChainFabricNodeListener(), bcNodeBlockChainArrayList).doEven();
                    log.info("[queue->node] element's type is node，start node pod success");
                    break;
                }
                case "FabricChannelEventSource": {
                    log.info("[queue->channel] 执行创建通道脚本");
                    //获取集群对象，以获取更多的集群信息
                    //并对返回的结果进行判断
                    FabricChannelEventSource fabricChannelEventSource = (FabricChannelEventSource) blockChainEventSource;
                    BCChannel bcChannel = fabricChannelEventSource.getBcChannel();

                    //获取所有的orderer列表
                    List<BCNode> bcNodeList = nodeService.getNodeByNodeTypeAndCluster(1, fabricChannelEventSource.getBcCluster().getId());
                    BCNode ordererNode = bcNodeList.get(new Random().nextInt(bcNodeList.size()));

                    List<BCOrg> bcOrgList = fabricChannelEventSource.getBcOrgs();
                    List<String> orgNameList = new ArrayList<>();
                    List<BCChannelOrg> bcChannelOrgList = new ArrayList<>();
                    for (BCOrg bcOgr : bcOrgList) {
                        orgNameList.add(bcOgr.getOrgName());
                        BCChannelOrg bcChannelOrg = new BCChannelOrg();
                        bcChannelOrg.setOrgId(bcOgr.getId());
                        bcChannelOrg.setChannelId(bcChannel.getId());
                        bcChannelOrgList.add(bcChannelOrg);
                    }
                    bcChannelOrgMapper.insertChannelOrg(bcChannelOrgList);
                    //执行创建通道脚本
                    //生成通道的通道的配置文件
                    HyperledgerFabricComponentsStartUtils.buildFabricChannel(fabricChannelEventSource.getBcCluster(), orgNameList, ordererNode, fabricChannelEventSource.getBcChannel());

                    break;
                }
                case "FabricJoinChannelEventSource":
                    log.info("[queue->join] 执行加入节点脚本");
                    FabricJoinChannelEventSource fabricJoinChannelEventSource = (FabricJoinChannelEventSource) blockChainEventSource;
                    JSONArray jsonArray = fabricJoinChannelEventSource.getJsonArray();
                    List<BCChannelOrgPeer> bcChannelOrgPeerList = fabricJoinChannelEventSource.getBcChannelOrgPeerList();
                    bcChannelOrgPeerMapper.insertChannelOrgPeer(bcChannelOrgPeerList);
                    HyperledgerFabricComponentsStartUtils.nodeJoinFabricChannel(jsonArray);
                    break;
                default:
                    log.error("unknown class type className=" + className);
            }
        }
    }

}
