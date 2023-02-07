package org.bc.auto.service.system;

import org.bc.auto.constant.impl.ValidatorResultCode;
import org.bc.auto.dao.BCChannelMapper;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.dao.BCOrgMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.listener.source.FabricChannelEventSource;
import org.bc.auto.model.entity.BCChannel;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;
import org.bc.auto.model.vo.ChannelVo;
import org.bc.auto.utils.BlockChainShellQueueUtils;
import org.bc.auto.utils.StringUtils;
import org.bc.auto.utils.ValidatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChannelService  {
    private static final Logger logger = LoggerFactory.getLogger(ChannelService.class);

    @Resource
    private BCClusterMapper bcClusterMapper;

    @Resource
    private BCChannelMapper bcChannelMapper;

    @Resource
    private BCOrgMapper bcOrgMapper;

    private void paramCheck(ChannelVo vo) {
        ValidatorUtils.isNotNull(vo.getClusterId(), ValidatorResultCode.VALIDATOR_CLUSTER_ID_NULL);
        logger.debug("[channel->create] create channel，get cluster id is:{}", vo.getClusterId());

        ValidatorUtils.isNotNull(vo.getChannelName(), ValidatorResultCode.VALIDATOR_CHANNEL_NAME_NULL);
        //判断通道名称是否匹配
        if (!ValidatorUtils.isMatches(vo.getChannelName(), ValidatorUtils.FABRIC_CHANNEL_NAME_REGEX)) {
            logger.error("[channel->create] create channel，组织名称错误，获取到的组织名称为:{}", vo.getChannelName());
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CHANNEL_NAME_NOT_MATCH);
        }

        //组成通道的组织名称不能为空
        ValidatorUtils.isNotNull(vo.getOrgIds(), ValidatorResultCode.VALIDATOR_ORG_ID_NULL);

        //TODO
        BCChannel channel = bcChannelMapper.getByChannelName(vo.getChannelName());
        if (channel != null) {
            throw new ValidatorException("channel名称已存在！");
        }
    }

    @Transactional
    public void createChannel(ChannelVo vo) throws BaseRuntimeException {
        paramCheck(vo);

        BCCluster bcCluster = bcClusterMapper.getClusterById(vo.getClusterId());
        String clusterName = bcCluster.getClusterName();
        ValidatorUtils.isNotNull(clusterName, ValidatorResultCode.VALIDATOR_CLUSTER_NAME_NULL);
        logger.info("[channel->create] create channel，get cluster name is:{}", clusterName);

        //根据组织的主键列表查询组织集合
        List<BCOrg> bcOrgList = bcOrgMapper.getOrgByOrgIds(vo.getOrgIds());
        if (bcOrgList.size() != vo.getOrgIds().size()) {
            logger.error("[channel->create] create channel，get the org list is not match parameters, get the org list size is:{}, but expect value is:{} ", bcOrgList.size(), vo.getOrgIds().size());
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CHANNEL_ORG_LIST_ERROR);
        }


        BCChannel bcChannel = new BCChannel();
        bcChannel.setChannelName(vo.getChannelName());
        bcChannel.setClusterId(vo.getClusterId());
        bcChannel.setClusterName(clusterName);
        bcChannel.setId(StringUtils.getId());
        bcChannel.setIsBlockListener(0);
        bcChannel.setChannelStatus(1);
        bcChannelMapper.insertChannel(bcChannel);

        //添加到脚本执行的队列中
        //把对应的组织对象添加至脚本的执行队列中，等待执行
        //主要用于生成Orderer组织的MSP证书和TLS的证书
        //在同一个集群中，创建Orderer组织的脚本得确保是优先执行；理论上队列的特性只要确定该任务是先加入队列即可。
        //如果生产环境在集群、并发情况下，并且添加Peer组织无法控制；可以按照集群的状态决定是否创建peer的组织。
        FabricChannelEventSource fabricChannelEventSource = new FabricChannelEventSource();
        fabricChannelEventSource.setBcChannel(bcChannel);
        fabricChannelEventSource.setBcCluster(bcCluster);
        fabricChannelEventSource.setBcOrgs(bcOrgList);
        boolean flag = BlockChainShellQueueUtils.add(fabricChannelEventSource);
        if (!flag) {
            logger.error("[async] 通道加入任务队列错误，请确认错误信息。");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_QUEUE_ERROR);
        }
    }
}
