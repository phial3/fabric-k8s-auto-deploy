package org.bc.auto.listener.source;

import lombok.Data;
import org.bc.auto.model.entity.BCCluster;

@Data
public class FabricClusterEventSource implements BlockChainEventSource{

    private BCCluster bcCluster;

}
