package org.bc.auto.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class NodeVo {

    List<NodeParam> nodeList;
    private String clusterId;
    private String channelName;
    private String channelId;
    private List<String> peerIds;

    @Data
    public static class NodeParam {
        private String clusterId;
        private String nodeName;
        private Integer nodeType;
        private String orgId;
        private String orgName;

    }
}
