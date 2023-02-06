package org.bc.auto.dao;

import org.apache.ibatis.annotations.Param;
import org.bc.auto.model.entity.BCChannel;
import org.bc.auto.model.entity.BCCluster;
import org.springframework.stereotype.Component;

@Component
public interface BCChannelMapper {

    int insertChannel(BCChannel bcChannel);

    BCChannel getByChannelName(@Param(value = "channelName") String channelName);
}
