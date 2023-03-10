package org.bc.auto.controller;

import lombok.extern.slf4j.Slf4j;
import org.bc.auto.model.vo.ChannelVo;
import org.bc.auto.model.vo.Result;
import org.bc.auto.service.system.ChannelService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/channel")
public class ChannelController {

    @Resource
    private ChannelService channelService;

    @PostMapping("/create")
    public Result createChannel(@RequestBody ChannelVo vo) {
        log.debug("[channel->create] 用户请求区块链create方法");
        channelService.createChannel(vo);
        return Result.success();
    }

}
