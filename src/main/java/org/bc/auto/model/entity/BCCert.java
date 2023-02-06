package org.bc.auto.model.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class BCCert implements Serializable {
    private static final long serialVersionUID = 1520423954949793297L;

    private String id;

    private String certName;

    private String certPubKey;

    private String certPriKey;

    //组织的根MSP证书
    private String certCaCert;

    private String certTlsPubKey;

    private String certTlsPriKey;

    // 1:Orderer组织的管理员证书；
    // 2:Orderer组织的用户证书；
    // 3:Org组织的管理员证书；
    // 4:Org组织的用户证书
    // 5:Org组织的节点证书；
    // 6:Orderer组织的节点证书。
    // 证书类型之间相互为互斥证书
    private Integer certType;

    //证书状态，1：正常，2：已注销，3：注销中
    private Integer certStatus;

    private String clusterId;

    private String orgId;
}
