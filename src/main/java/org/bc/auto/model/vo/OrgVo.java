package org.bc.auto.model.vo;

import lombok.Data;

@Data
public class OrgVo {
    private String clusterId;
    private String orgName;
    private Integer orgIsTls;
    private Integer orgType;
}
