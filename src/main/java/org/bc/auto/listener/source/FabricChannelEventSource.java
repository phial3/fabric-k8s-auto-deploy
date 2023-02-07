package org.bc.auto.listener.source;

import lombok.Data;
import org.bc.auto.model.entity.BCChannel;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;

import java.util.List;

@Data
public class FabricChannelEventSource implements BlockChainEventSource {

    private BCChannel bcChannel;

    private BCCluster bcCluster;

    private List<BCOrg> bcOrgs;

}
