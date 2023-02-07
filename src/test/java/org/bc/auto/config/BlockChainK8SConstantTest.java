package org.bc.auto.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class BlockChainK8SConstantTest {

    @Test
    public void testGetK8sConfigPath() {
        log.info("k8s config file path is {}", BlockChainK8SConstant.getK8sConfigPath());
    }
}
