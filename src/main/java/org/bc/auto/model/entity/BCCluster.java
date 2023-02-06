package org.bc.auto.model.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class BCCluster implements Serializable {
    private static final long serialVersionUID = 4317978403273155155L;

    private String id;

    private String clusterName;

    //安装状态从1往后数,1:初始状态， 2：创建中， 3：创建成功， 4创建失败
    private Integer installStatus;

    private int ordererCount;

    private int clusterConsensusType;

    private Long createTime;

    private Long expiresTime;

    //集群的类型，1：Fabric，2：QuoRom
    private Integer clusterType;

    //安装的版本（各种版本，如：Fabric:1.4.5）
    private String clusterVersion;

    private int stateDbType;

}
