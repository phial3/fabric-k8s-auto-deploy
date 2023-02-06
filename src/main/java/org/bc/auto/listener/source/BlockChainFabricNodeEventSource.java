package org.bc.auto.listener.source;

import java.util.List;

public class BlockChainFabricNodeEventSource<E> implements BlockChainEventSource {

    private List<E> eList;

    public List<E> geteList() {
        return eList;
    }

    public void seteList(List<E> eList) {
        this.eList = eList;
    }
}
