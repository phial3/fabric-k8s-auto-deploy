package org.bc.auto.listener.source;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import org.bc.auto.model.entity.BCChannelOrgPeer;

import java.util.List;

@Data
public class FabricJoinChannelEventSource implements BlockChainEventSource {

    private JSONArray jsonArray;

    private List<BCChannelOrgPeer> bcChannelOrgPeerList;

}
