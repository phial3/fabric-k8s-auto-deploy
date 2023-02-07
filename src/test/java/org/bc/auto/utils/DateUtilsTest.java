package org.bc.auto.utils;

import lombok.extern.slf4j.Slf4j;
import org.bc.auto.exception.BaseRuntimeException;
import org.junit.jupiter.api.Test;

@Slf4j
public class DateUtilsTest {

    @Test
    public void testGetCurrentMillisTimeStamp(){
        DateUtils.getCurrentMillisTimeStamp();
    }

    @Test
    public void testGetCurrentSecondTimeStamp(){
        DateUtils.getCurrentSecondTimeStamp();
    }

    @Test
    public void testGetMillisTimeStampToDateDayString(){
        log.info(DateUtils.getTimeStampToDateDayString(DateUtils.getCurrentMillisTimeStamp()));
    }

    @Test
    public void testGetSecondTimeStampToDateDayString(){
        log.info(DateUtils.getTimeStampToDateDayString(DateUtils.getCurrentSecondTimeStamp()*1000));
    }

    @Test
    public void testGetNullTimeStampToDateDayString(){
        try{
        DateUtils.getTimeStampToDateDayString(null);
        }catch (BaseRuntimeException e){
            log.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testGetZeroTimeStampToDateDayString(){
        try{
            log.info(DateUtils.getTimeStampToDateDayString(0L));
        }catch (BaseRuntimeException e){
            log.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testGetSecondTimeStampToDateSecondString(){
        log.info(DateUtils.getTimeStampToDateSecondString(DateUtils.getCurrentSecondTimeStamp()*1000));
    }



}
