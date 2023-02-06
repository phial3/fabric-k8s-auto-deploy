package org.bc.auto.service;

import org.bc.auto.dao.BCCertMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCCert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CertService {

    @Resource
    private BCCertMapper bcCertMapper;

    @Transactional
    public int insertBCCert(BCCert bcCert) throws BaseRuntimeException {
        return bcCertMapper.insertBCCert(bcCert);
    }

    public List<BCCert> getBCCertByOrgAdmin(String orgId) throws BaseRuntimeException {
        return bcCertMapper.getBCCertByOrgAdmin(orgId);
    }
}
