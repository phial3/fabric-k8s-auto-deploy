package org.bc.auto.service;

import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.vo.ChannelVo;

public interface ChannelService {

    void createChannel(ChannelVo vo)throws BaseRuntimeException;

}
