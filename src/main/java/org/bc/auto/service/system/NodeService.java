package org.bc.auto.service.system;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bc.auto.constant.impl.ValidatorResultCode;
import org.bc.auto.dao.BCNodeMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.listener.source.FabricJoinChannelEventSource;
import org.bc.auto.listener.source.FabricNodeEventSource;
import org.bc.auto.model.entity.BCChannelOrgPeer;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCNode;
import org.bc.auto.model.vo.NodeVo;
import org.bc.auto.utils.BlockChainShellQueueUtils;
import org.bc.auto.utils.DateUtils;
import org.bc.auto.utils.StringUtils;
import org.bc.auto.utils.ValidatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class NodeService  {

    private static final Logger logger = LoggerFactory.getLogger(NodeService.class);

    @Resource
    private BCNodeMapper bcNodeMapper;

    @Resource
    private ClusterService clusterService;

    @Transactional
    public boolean createNode(NodeVo vo) throws BaseRuntimeException {

        //确认传入的节点列表是非空集合
        ValidatorUtils.isNotNull(vo.getNodeList(), ValidatorResultCode.VALIDATOR_NODE_ARRAY_ERROR);

        //定义节点对象集合
        List<BCNode> bcNodeInsertList = new ArrayList<>();
        //对元素进行循环处理
        for (NodeVo.NodeParam param : vo.getNodeList()) {

            //获取集群编号，该节点属于哪个网络下。
            String clusterId = param.getClusterId();
            ValidatorUtils.isNotNull(clusterId, ValidatorResultCode.VALIDATOR_CLUSTER_ID_NULL);
            logger.debug("[node->create] create blockchain's node，get the cluster id is:{}", clusterId);

            //获取创建的节点名称。
            String nodeName = param.getNodeName();
            ValidatorUtils.isNotNull(nodeName, ValidatorResultCode.VALIDATOR_NODE_NAME_NULL);
            //判断节点名称是否匹配
            if (!ValidatorUtils.isMatches(nodeName, ValidatorUtils.FABRIC_PEER_NAME_REGEX)) {
                logger.error("[node->create] create blockchain's node，node name error，make sure node name '{}' accord with naming convention", nodeName);
                throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_NAME_NOT_MATCH);
            }
            logger.debug("[node->create] create blockchain's node，get the node name is:{}", nodeName);
            //检查节点是否存在
            List<BCNode> bcNodeList = bcNodeMapper.getNodeByNodeNameAndCluster(nodeName, clusterId);
            if (!ValidatorUtils.isNull(bcNodeList)) {
                logger.error("[org->create] create blockchain's node，make sure node name '{}' is not exists", nodeName);
                throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_NAME_RE);
            }

            String orgId = param.getOrgId();
            logger.info("[node->create] create blockchain's node, get the node name is :{},cluster id is :{}, org id is :{}",
                    nodeName, clusterId, orgId);

            BCNode bcNode = new BCNode();
            bcNode.setId(StringUtils.getId());
            bcNode.setNodeName(nodeName);
            bcNode.setClusterId(clusterId);
            bcNode.setOrgId(orgId);
            bcNode.setNodeType(param.getNodeType());
            bcNode.setOrgName(param.getOrgName());
            bcNode.setCreateTime(DateUtils.getCurrentMillisTimeStamp());

            bcNodeInsertList.add(bcNode);
        }

        int nodeResult = bcNodeMapper.insertNodeList(bcNodeInsertList);
        //如果集群成功入库
        if (!ValidatorUtils.isGreaterThanZero(nodeResult)) {
            logger.error("[node->create] create blockchain's node，insert database error.");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_INSERT_ERROR);
        }

        FabricNodeEventSource<BCNode> fabricNodeEventSource = new FabricNodeEventSource<>();
        fabricNodeEventSource.setEList(bcNodeInsertList);

        return BlockChainShellQueueUtils.add(fabricNodeEventSource);
    }

    public int updateNode(BCNode bcNode) throws BaseRuntimeException {
        return bcNodeMapper.updateNode(bcNode);
    }


    public List<BCNode> getNodeByNodeTypeAndCluster(int nodeType, String clusterId) {
        return bcNodeMapper.getNodeByNodeTypeAndCluster(nodeType, clusterId);
    }

    public boolean joinChannel(NodeVo vo) throws BaseRuntimeException {

        //获取集群编号，该节点属于哪个网络下。
        String clusterId = vo.getClusterId();
        ValidatorUtils.isNotNull(clusterId, ValidatorResultCode.VALIDATOR_CLUSTER_ID_NULL);
        logger.debug("[node->join] node join blockchain's channel，get the cluster id is:{}", clusterId);

        BCCluster bcCluster = clusterService.getBCCluster(clusterId);

        // 校验 通道名称 是否为空
        String channelName = vo.getChannelName();
        String channelId = vo.getChannelId();
        ValidatorUtils.isNotNull(channelName, ValidatorResultCode.VALIDATOR_CHANNEL_NAME_NULL);
        logger.debug("[node->join] node join blockchain's channel，get the channel name is:{}", channelName);

        //获取所有的orderer列表
        List<BCNode> bcNodeList = this.getNodeByNodeTypeAndCluster(1, clusterId);
        BCNode ordererNode = bcNodeList.get(new Random().nextInt(bcNodeList.size()));

        //获取需要加入的节点列表对象
        JSONArray jsonArray = new JSONArray();


        List<String> peerIdList = vo.getPeerIds();
        List<BCNode> peerList = bcNodeMapper.getNodeByPeerIds(peerIdList);
        if (peerList.size() != peerIdList.size()) {
            logger.error("[channel->join] join channel，get the peer list is not match parameters, get the peer list size is:{}, but expect value is:{} ", peerList.size(), peerIdList.size());
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CHANNEL_PEER_LIST_ERROR);
        }

        List<BCChannelOrgPeer> bcChannelOrgPeerList = new ArrayList<>();
        for (BCNode bcNode : peerList) {
            JSONObject jsonObjectTemp = new JSONObject();
            BCChannelOrgPeer bcChannelOrgPeer = new BCChannelOrgPeer();
            jsonObjectTemp.put("clusterName", bcCluster.getClusterName());
            jsonObjectTemp.put("channelName", channelName);
            jsonObjectTemp.put("ordererOrgName", ordererNode.getOrgName());
            jsonObjectTemp.put("ordererName", ordererNode.getNodeName());
            jsonObjectTemp.put("clusterVersion", bcCluster.getClusterVersion());
            jsonObjectTemp.put("peerName", bcNode.getNodeName());
            jsonObjectTemp.put("orgName", bcNode.getOrgName());
            jsonArray.add(jsonObjectTemp);
            bcChannelOrgPeer.setOrgId(bcNode.getOrgId());
            bcChannelOrgPeer.setPeerId(bcNode.getId());
            bcChannelOrgPeer.setChannelId(channelId);
            bcChannelOrgPeerList.add(bcChannelOrgPeer);
        }


        FabricJoinChannelEventSource fabricJoinChannelEventSource = new FabricJoinChannelEventSource();
        fabricJoinChannelEventSource.setJsonArray(jsonArray);
        fabricJoinChannelEventSource.setBcChannelOrgPeerList(bcChannelOrgPeerList);
        boolean flag = BlockChainShellQueueUtils.add(fabricJoinChannelEventSource);

        return flag;
    }
}
