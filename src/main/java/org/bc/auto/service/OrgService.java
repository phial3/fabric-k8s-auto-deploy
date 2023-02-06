package org.bc.auto.service;

import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCOrg;
import org.bc.auto.model.vo.OrgVo;

public interface OrgService {

    BCOrg createOrg(OrgVo vo)throws BaseRuntimeException;
    BCOrg getOrgByOrgId(String orgId)throws BaseRuntimeException;
}
