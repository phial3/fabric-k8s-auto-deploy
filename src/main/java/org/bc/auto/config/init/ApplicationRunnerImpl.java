package org.bc.auto.config.init;

import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.bc.auto.service.BlockChainQueueService;
import org.bc.auto.utils.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRunnerImpl.class);

    @Resource
    private BlockChainQueueService blockChainQueueService;

    public void run(ApplicationArguments args) throws Exception {
        logger.info("[application->init]服务启动完成，开始加载线程执行监听脚本队列");
        ThreadPoolManager.newInstance().addExecuteTask(() ->
                blockChainQueueService.run()
        );
        logger.info("[application->init]服务启动完成，完成加载线程执行监听脚本队列");
    }
}
