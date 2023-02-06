package org.bc.auto.model.entity;

import lombok.Data;

@Data
public class BCClusterInfo {

    private String clusterVersion;

    private int clusterConsensusType;

    private int ordererCount;

    private String clusterId;

}
