package org.bc.auto.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bc.auto.config.BlockChainAutoConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/bc")
public class BlockChainBusinessController {

    @PostMapping("/invoke")
    public void createChannel(@RequestBody JSONObject param) {

        log.info("[bc->invoke] 用户请求bc模块中的invoke方法，参数如下 :{}",param.toJSONString());

        log.info("数值为：{}", BlockChainAutoConstant.NFS_HOST);

    }


}
