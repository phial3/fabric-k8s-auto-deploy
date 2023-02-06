package org.bc.auto.model.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class BCChannel implements Serializable {
    private static final long serialVersionUID = 8557422491575196537L;

    private String id;

    private String channelName;

    private int channelStatus;

    private int isBlockListener;

    private String clusterId;

    private String clusterName;

}
