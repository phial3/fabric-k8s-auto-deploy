package org.bc.auto.controller;

import lombok.extern.slf4j.Slf4j;
import org.bc.auto.model.vo.OrgVo;
import org.bc.auto.model.vo.Result;
import org.bc.auto.service.system.ClusterService;
import org.bc.auto.service.system.OrgService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/org")
public class OrgController {

    @Resource
    private OrgService orgService;

    @Resource
    private ClusterService clusterService;

    @PostMapping("/create")
    public Result createOrg(@RequestBody OrgVo vo) {
        log.debug("[org->create] 用户请求区块链create方法");
        orgService.createOrg(vo);
        //clusterService.createCluster(jsonObject);
        return Result.success();
    }
}
