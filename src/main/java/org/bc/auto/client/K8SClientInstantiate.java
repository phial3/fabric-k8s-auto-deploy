package org.bc.auto.client;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import lombok.extern.slf4j.Slf4j;
import org.bc.auto.config.BlockChainAutoConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Slf4j
@Component
public class K8SClientInstantiate implements CommandLineRunner {

    private ApiClient client;

    @Override
    public void run(String... args) {
        // 初始化K8s client
        try {
            File kubeConfigFile = new File(BlockChainAutoConstant.K8S_CONFIG_PATH);
            if (kubeConfigFile.isDirectory() || !kubeConfigFile.exists()) {
                log.error("not found connect k8s config file. please check if it is exist!!!");
                return;
            }
            client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigFile))).build();
            Configuration.setDefaultApiClient(client);
            log.info("k8s ApiClient config init finished!");
        } catch (IOException e) {
            log.error("Load kubernetes config file error: {}", e.getMessage(), e);
        }
    }
}
