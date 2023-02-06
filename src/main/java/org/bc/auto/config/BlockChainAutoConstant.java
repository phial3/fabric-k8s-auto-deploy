package org.bc.auto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlockChainAutoConstant {

    //pv存储挂载后缀
    @Value("${bc-auto.pv-suffix}")
    public static String PV_SUFFIX ;

    //pvc存储挂载后缀
    @Value("${bc-auto.pvc-suffix}")
    public static String PVC_SUFFIX ;

    //VOLUME_DATA后缀
    @Value("${bc-auto.volume-data-suffix}")
    public static String VOLUME_DATA_SUFFIX;

    //podDisruptionBudget后缀
    @Value("${bc-auto.pdb-suffix}")
    public static String PDB_SUFFIX;

    //nfs绑定的地址
    @Value("${bc-auto.nfs-host}")
    public static String NFS_HOST;

    //nfs绑定的路径
    @Value("${bc-auto.nfs-path}")
    public static String NFS_PATH;

    //K8S work path
    @Value("${bc-auto.k8s-work-path}")
    public static String K8S_WORK_PATH;

    //K8S work path
    @Value("${bc-auto.k8s-data-path}")
    public static String K8S_DATA_PATH;

    //设置根MSP,CA的名称
    @Value("${bc-auto.msp-ca-name}")
    public static String MSP_CA_NAME;

    //设置TLS，CA的名称
    @Value("${bc-auto.tls-ca-name}")
    public static String TLS_CA_NAME;

}
