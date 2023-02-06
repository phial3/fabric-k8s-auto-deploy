package org.bc.auto.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.vo.ClusterVo;
import org.bc.auto.model.vo.Result;
import org.bc.auto.service.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/cluster")
public class ClusterController {

    @Resource
    private ClusterService clusterService;

    @PostMapping("/create")
    public Result createCluster(@RequestBody ClusterVo vo) {
        log.debug("[cluster->create] request is to create blockchain's cluster, create k8s namespace、pv、pvc too");

        BCCluster bcCluster = clusterService.createCluster(vo);
        log.info("[cluster->create] create blockchain's cluster end, cluster info => id is:{}, name is:{}, version is:{}",bcCluster.getId(),bcCluster.getClusterName(),bcCluster.getClusterVersion());

        JSONObject jsonObjectResult = new JSONObject();
        jsonObjectResult.put("clusterId",bcCluster.getId());
        jsonObjectResult.put("clusterName",bcCluster.getClusterName());
        return Result.success(jsonObjectResult);
    }

    @GetMapping("/all")
    public Result getClusters() {
        return Result.success(clusterService.getBCClusterList());
    }
}
