package org.bc.auto.utils;

import lombok.extern.slf4j.Slf4j;
import org.bc.auto.exception.BaseRuntimeException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class K8SUtilsTest {

    @Test
    public void testGetPdbNameError() {
        try {
            log.info(K8SUtils.getPdbName(""));
        } catch (BaseRuntimeException e) {
            log.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testGetPdbName() {
        try {
            log.info(K8SUtils.getPdbName("aaa"));
        } catch (BaseRuntimeException e) {
            log.error(e.getExceptionResult().toString());
        }
    }
}
