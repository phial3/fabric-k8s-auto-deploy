package org.bc.auto.controller;

import lombok.extern.slf4j.Slf4j;
import org.bc.auto.model.vo.NodeVo;
import org.bc.auto.model.vo.Result;
import org.bc.auto.service.NodeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/node")
public class NodeController {

    @Resource
    private NodeService nodeService;

    @PostMapping("/create")
    public Result createNodes(@RequestBody NodeVo vo) {
        log.debug("[node->create] 用户请求区块链节点create方法");
        nodeService.createNode(vo);
//        clusterService.createCluster(jsonObject);
        return Result.success();
    }

    @PostMapping("/join")
    public Result joinChannel(@RequestBody NodeVo vo) {
        log.debug("[node->join] 用户请求区块链节点加入通道");
        nodeService.joinChannel(vo);
        return Result.success();
    }

}
