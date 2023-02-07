package org.bc.auto.listener.source;

import lombok.Data;

import java.util.List;

@Data
public class FabricNodeEventSource<E> implements BlockChainEventSource {

    private List<E> eList;

}
