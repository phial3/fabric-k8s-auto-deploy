package org.bc.auto.service;

import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCNode;
import org.bc.auto.model.vo.NodeVo;

import java.util.List;

public interface NodeService {

    boolean createNode(NodeVo vo)throws BaseRuntimeException;

    int updateNode(BCNode bcNode)throws BaseRuntimeException;

    List<BCNode> getNodeByNodeTypeAndCluster(int nodeType, String clusterId);

    boolean joinChannel(NodeVo vo) throws BaseRuntimeException;
}
