package org.bc.auto.service.impl;

import org.bc.auto.dao.BCCertMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCCert;
import org.bc.auto.service.CertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CertServiceImpl implements CertService {

    @Resource
    private BCCertMapper bcCertMapper;

    @Override
    @Transactional
    public int insertBCCert(BCCert bcCert) throws BaseRuntimeException {
        return bcCertMapper.insertBCCert(bcCert);
    }

    @Override
    public List<BCCert> getBCCertByOrgAdmin(String orgId) throws BaseRuntimeException {
        return bcCertMapper.getBCCertByOrgAdmin(orgId);
    }
}
