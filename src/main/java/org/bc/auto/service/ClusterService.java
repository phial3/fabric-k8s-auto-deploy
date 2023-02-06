package org.bc.auto.service;

import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCClusterInfo;
import org.bc.auto.model.vo.ClusterVo;

import java.util.List;

public interface ClusterService {

    BCCluster createCluster(ClusterVo vo)throws BaseRuntimeException;

    List<BCCluster> getBCClusterList()throws BaseRuntimeException;

    BCClusterInfo getBCClusterInfo(String clusterId)throws BaseRuntimeException;

    BCCluster getBCCluster(String clusterId)throws BaseRuntimeException;
}
