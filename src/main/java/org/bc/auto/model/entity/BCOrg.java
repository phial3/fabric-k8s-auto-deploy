package org.bc.auto.model.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class BCOrg implements Serializable {

    private static final long serialVersionUID = 5376058759633491230L;

    private String id;

    private String orgName;

    private String orgMspId;

    //是否开启TLS 1:开启，2：未开启
    private Integer orgIsTls;

    //组织的运行状态，1：使用中，2：已注销，3：添加中，4：记录成功（添加数据库），5：添加失败
    private Integer orgStatus;

    //组织类型，1：Orderer组织，2：Org组织
    private Integer orgType;

    private Long createTime;

    private String certId;

    private String clusterId;

    private String clusterName;

}
