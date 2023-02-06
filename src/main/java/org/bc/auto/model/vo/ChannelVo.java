package org.bc.auto.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChannelVo {
    private String clusterId;
    private String channelName;
    private List<String> orgIds;
}
