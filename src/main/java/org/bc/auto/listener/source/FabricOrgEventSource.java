package org.bc.auto.listener.source;

import lombok.Data;
import org.bc.auto.model.entity.BCOrg;

@Data
public class FabricOrgEventSource implements BlockChainEventSource {

    private BCOrg bcOrg;

}
