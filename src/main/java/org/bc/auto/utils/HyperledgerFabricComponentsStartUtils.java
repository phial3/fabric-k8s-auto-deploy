package org.bc.auto.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.*;
import org.bc.auto.constant.impl.K8SResultCode;
import org.bc.auto.config.BlockChainK8SConstant;
import org.bc.auto.exception.K8SException;
import org.bc.auto.model.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class HyperledgerFabricComponentsStartUtils {

    private static final String APP_NAME = "auto-cluster";
    public static final int CA_PORT = 7054;
    public static final String ORDERER_MSP_SCRIPT_NAME = "generate-orderer-msp-certs.sh";
    public static final String ROOT_CA_LOGIN_INFO = "admin:adminpw";
    public static final String ORDERER_TLS_SCRIPT_NAME = "generate-orderer-tls-certs.sh";
    public static final String PEER_MSP_SCRIPT_NAME = "generate-peer-msp-certs.sh";
    public static final String PEER_TLS_SCRIPT_NAME = "generate-peer-tls-certs.sh";
    public static final String ADD_ORG_TO_SYS_CHANNEL = "add-org-to-syschannel.sh";
    public static final String GENERATE_GENESIS_SCRIPT_NAME = "generate-genesis.sh";
    public static final String COMMON_OPERATE_SCRIPT_NAME = "common-operate.sh";
    public static final String CREATE_CHANNEL_SCRIPT_NAME = "create-channel.sh";
    public static final String JOIN_CHANNEL_SCRIPT_NAME = "join-channel.sh";

    public static final int PEER_PORT = 7051;
    public static final int PEER_CHAINCODE_PORT = 7052;
    public static final int COUCH_DB_PORT = 5984;
    public static final String COUCH_DB_USERNAME = "auto-cluster-username";
    public static final String COUCH_DB_PASSWORD = "auto-cluster-password";
    public static final int ORDERER_PORT = 7050;
    public static final int ORDERER_MONITOR_PORT = 8443;

    private static final Logger logger = LoggerFactory.getLogger(HyperledgerFabricComponentsStartUtils.class);

    public static void startHyperledgerFabricCaServer(String namespace, String name, String image, String command) {

        logger.info("[k8s->pod] start hyperledger fabric ca server pod, name is :{}, namespace is :{}, images name :{}",
                name, namespace, image);
        final Map<String, String> labels = new HashMap<String, String>(4) {{
            put("namespace", namespace);
            put("app", APP_NAME);
            put("role", "ca");
            put("ca-name", name);
        }};
        // ??????CA?????????deployment
        //??????????????????????????????????????????????????????????????????????????????????????????
        V1PodSpec podSpec = new V1PodSpec()
                .containers(new ArrayList<V1Container>() {{
                    add(new V1Container()
                                    .name(name)
                                    .image(image)
                                    .command(new ArrayList<String>() {{
                                        add("/bin/bash");
                                        add("-c");
                                        add("fabric-ca-server start -b admin:adminpw");
                                    }})
                                    .env(new ArrayList<V1EnvVar>() {{
                                        add(new V1EnvVar().name("TZ").value("Asia/Shanghai"));
                                        add(new V1EnvVar().name("FABRIC_CA_HOME").value("/etc/hyperledger/fabric-ca-server"));
                                    }})
                                    .ports(new ArrayList<V1ContainerPort>() {{
                                        add(new V1ContainerPort().containerPort(CA_PORT));
                                    }})
                                    .volumeMounts(new ArrayList<V1VolumeMount>() {{
                                        add(new V1VolumeMount().name("ca-data").mountPath("/etc/hyperledger/fabric-ca-server").subPath(namespace + "/" + name));
//                                        add(new V1VolumeMount().name("ca-config").mountPath("/opt/fabric-ca-server-config-rca.yaml"));
//                                        add(new V1VolumeMount().name("ca-start").mountPath("/opt/start-rca.sh"));
                                    }})
                    );
                }})
                .volumes(new ArrayList<V1Volume>() {{
                    add(new V1Volume()
                            .name("ca-data")
                            .persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName(BlockChainK8SConstant.getK8sPvcName(namespace)))
                    );
//                    add(new V1Volume()
//                            .name("ca-config")
//                            .hostPath(new V1HostPathVolumeSource().path(BlockChainK8SConstant.getWorkPath()+"/bin/1.4.5/ca/fabric-ca-server-config-rca.yaml"))
//                    );
//                    add(new V1Volume()
//                            .name("ca-start")
//                            .hostPath(new V1HostPathVolumeSource().path(BlockChainK8SConstant.getWorkPath()+"/bin/1.4.5/ca/start-rca.sh"))
//                    );
                }});
        K8SUtils.createDeployment(namespace, name, labels, podSpec);
        logger.debug("[k8s->pod] start hyperledger fabric ca server pod, create ca server pod deployment success, name is :{}", name);

        // ?????????ca service
        V1ServiceSpec svcSpec = new V1ServiceSpec()
                .selector(labels)
                .ports(new ArrayList<V1ServicePort>() {{
                    add(new V1ServicePort().name("listen").port(CA_PORT).targetPort(new IntOrString(CA_PORT)));
                }});
        K8SUtils.createService(namespace, name, labels, svcSpec);
        logger.debug("[k8s->pod] start hyperledger fabric ca server pod, create ca server pod svc success, name is :{}", name);
        logger.info("[k8s->pod] start hyperledger fabric ca server pod success, name is :{}", name);
    }

    public static void generateNodeCerts(BCCluster bcCluster, BCNode bcNode) {
        String clusterName = bcCluster.getClusterName();
        // ???k8s?????????????????? svc.namespace ??????????????????????????????
        String mspCaUrl = BlockChainK8SConstant.getFabricCaMspServerUrl(clusterName, CA_PORT);
        String tlsCaUrl = BlockChainK8SConstant.getFabricCaTlsServerUrl(clusterName, CA_PORT);

        String scriptsPath = BlockChainK8SConstant.getFabricOperateScriptsPath();
        String mspScriptPath = BlockChainK8SConstant.getFabricCaClientMspConfigFilePath(bcCluster.getClusterVersion());
        String tlsScriptPath = BlockChainK8SConstant.getFabricCaClientTlsConfigFilePath(bcCluster.getClusterVersion());
        String certsRootPath = BlockChainK8SConstant.getWorkPath();
        String saveCertsRootPath = BlockChainK8SConstant.getSavePath();

        logger.info("[shell->cert] shell to get node cert, script path is '{}', msp script path is '{}', tls script path is '{}', product cert path is '{}', save cert path is '{}'",
                scriptsPath, mspScriptPath, tlsScriptPath, certsRootPath, saveCertsRootPath);
        switch (bcNode.getNodeType()) {
            case 1: {
                if (!ShellUtils.exec(scriptsPath + ORDERER_MSP_SCRIPT_NAME, "orderer", clusterName, bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to get orderer node cert, generate node msp certs error. node name is :{}", bcNode.getNodeName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + ORDERER_TLS_SCRIPT_NAME, "orderer", clusterName, bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to get orderer node cert, generate node tls certs error. node name is :{}", bcNode.getNodeName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                break;
            }
            case 2: {
                if (!ShellUtils.exec(scriptsPath + PEER_MSP_SCRIPT_NAME, "register_peer", clusterName, bcNode.getOrgName(), bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to register node msp cert, generate node certs error. node name is :{}, org name is :{}", bcNode.getNodeName(), bcNode.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + PEER_MSP_SCRIPT_NAME, "enroll_peer", clusterName, bcNode.getOrgName(), bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to enroll node msp cert, generate node certs error. node name is :{}, org name is :{}", bcNode.getNodeName(), bcNode.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + PEER_TLS_SCRIPT_NAME, "register_peer", clusterName, bcNode.getOrgName(), bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to register node tls cert, generate node certs error. node name is :{}, org name is :{}", bcNode.getNodeName(), bcNode.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + PEER_TLS_SCRIPT_NAME, "enroll_peer", clusterName, bcNode.getOrgName(), bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to enroll node tls cert, generate node certs error. node name is :{}, org name is :{}", bcNode.getNodeName(), bcNode.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                break;
            }
            default: {
                logger.error("[shell->cert] shell to get node cert, maybe get error type value from parameter, error value is :{}", bcNode.getNodeType());
                throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
            }
        }

    }

    public static BCCert generateOrgCerts(BCCluster bcCluster, BCOrg bcOrg) {
        // ???k8s?????????????????? svc.namespace ??????????????????????????????
        String clusterName = bcCluster.getClusterName();
        String mspCaUrl = BlockChainK8SConstant.getFabricCaMspServerUrl(clusterName, CA_PORT);
        String tlsCaUrl = BlockChainK8SConstant.getFabricCaTlsServerUrl(clusterName, CA_PORT);
        BCCert bcCert = new BCCert();

        //????????????????????????
        //script path ==> /work/share/script/
        String scriptsPath = BlockChainK8SConstant.getFabricOperateScriptsPath();
        //msp????????????????????????????????????????????????ca????????????
        //script path ==> /work/share/bin/1.4.5/msp/
        String mspScriptPath = BlockChainK8SConstant.getFabricCaClientMspConfigFilePath(bcCluster.getClusterVersion());
        //tls????????????????????????????????????????????????ca????????????
        //script path ==> /work/share/bin/1.4.5/tls/
        String tlsScriptPath = BlockChainK8SConstant.getFabricCaClientTlsConfigFilePath(bcCluster.getClusterVersion());
        //???????????????????????????????????????????????????????????????????????????????????????????????????(??????????????????)
        //work path ==> /work/share
        String certsRootPath = BlockChainK8SConstant.getWorkPath();
        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        //data path ==> /data/auto
        String saveCertsRootPath = BlockChainK8SConstant.getSavePath();
        logger.info("[shell->cert] shell to get org cert, script path is '{}', msp script path is '{}', tls script path is '{}', product cert path is '{}', save cert path is '{}'"
                , scriptsPath, mspScriptPath, tlsScriptPath, certsRootPath, saveCertsRootPath);

        String certUserCaCert;
        String certUserPubKey;
        String certUserPriKey;
        String certTlsPubKey;
        String certTlsPriKey;

        //?????????Orderer??????????????????????????????????????????????????????Orderer????????????????????????Orderer??????????????????
        //??????Orderer?????????????????????????????????????????????????????????????????????
        //??????????????????????????????MSP?????????????????????TLS????????????
        //?????????Peer????????????????????????????????????????????????
        //Orderer??????????????????orderer?????????
        switch (bcOrg.getOrgType()) {
            case 1: {
                //??????orderer???????????????????????????  ==> /data/auto/mycluster/crypto-config/ordererOrganizations/mycluster/users/Admin@mycluster
                String ordererCertPath = saveCertsRootPath + File.separator + clusterName + "/crypto-config/ordererOrganizations/" + clusterName + "/users/Admin@" + clusterName;
                logger.debug("[shell->cert] shell to get orderer admin cert, save cert file path is '{}'", ordererCertPath);
                // ??????orderer msp??????
                if (!ShellUtils.exec(scriptsPath + ORDERER_MSP_SCRIPT_NAME, "ordererOrg", clusterName, "orderer0", ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to get orderer org cert, generate orderer admin msp certs error.");
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }

                // ??????orderer tls??????
                if (!ShellUtils.exec(scriptsPath + ORDERER_TLS_SCRIPT_NAME, "ordererOrg", clusterName, "orderer0", ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to get orderer org cert, generate orderer admin tls certs error.");
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                certUserCaCert = ordererCertPath + "/msp/cacerts/ca." + clusterName + "-cert.pem";
                certUserPubKey = ordererCertPath + "/msp/signcerts/Admin@" + clusterName + "-cert.pem";
                certUserPriKey = new File(ordererCertPath + "/msp/keystore").listFiles()[0].getPath();
                certTlsPubKey = ordererCertPath + "/tls/client.crt";
                certTlsPriKey = ordererCertPath + "/tls/client.key";
                logger.info("[shell->cert] shell to get orderer admin cert success: {}user ca certs path is '{}',{}user msp certs public key path is '{}',{}user msp certs private key path is '{}',{}user tls certs public key path is '{}',{}user tls certs private key path is '{}'{}."
                        , System.lineSeparator(), certUserCaCert, System.lineSeparator(), certUserPubKey, System.lineSeparator(), certUserPriKey, System.lineSeparator(), certTlsPubKey, System.lineSeparator(), certTlsPriKey, System.lineSeparator());
                bcCert.setCertType(1);
                break;
            }
            case 2: {
                //???????????????????????????
                String adminCertPath = saveCertsRootPath + File.separator + clusterName + "/crypto-config/peerOrganizations/" + bcOrg.getOrgName().toLowerCase() + "-" + clusterName + "/users/Admin@" + bcOrg.getOrgName().toLowerCase() + "-" + clusterName;

                if (!ShellUtils.exec(scriptsPath + PEER_MSP_SCRIPT_NAME, "register_admin_user", clusterName, bcOrg.getOrgName(), "peer0", ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to register org admin msp cert, generate org admin msp certs error. org name is :{}", bcOrg.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_PEER_ORG_CERT_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + PEER_MSP_SCRIPT_NAME, "enroll_admin_user", clusterName, bcOrg.getOrgName(), "peer0", ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to enroll org admin msp cert, generate org admin msp certs error. org name is :{}", bcOrg.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_PEER_ORG_CERT_ERROR);
                }

                logger.info("[shell->cert] shell to get org admin msp cert, generate org admin msp certs success. org name is :{}", bcOrg.getOrgName());

                // ??????org tls??????
                if (!ShellUtils.exec(scriptsPath + PEER_TLS_SCRIPT_NAME, "register_admin_user", clusterName, bcOrg.getOrgName(), "peer0", ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to register org admin tls cert, generate org admin tls certs error. org name is :{}", bcOrg.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_PEER_ORG_CERT_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + PEER_TLS_SCRIPT_NAME, "enroll_admin_user", clusterName, bcOrg.getOrgName(), "peer0", ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("[shell->cert] shell to enroll org admin tls cert, generate org admin tls certs error. org name is :{}", bcOrg.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_PEER_ORG_CERT_ERROR);
                }
                certUserCaCert = adminCertPath + "/msp/cacerts/ca." + clusterName + "-cert.pem";
                certUserPubKey = adminCertPath + "/msp/signcerts/Admin@" + bcOrg.getOrgName().toLowerCase() + "-" + clusterName + "-cert.pem";
                certUserPriKey = new File(adminCertPath + "/msp/keystore").listFiles()[0].getPath();
                certTlsPubKey = adminCertPath + "/tls/client.crt";
                certTlsPriKey = adminCertPath + "/tls/client.key";
                bcCert.setCertType(3);
                logger.info("[shell->cert] shell to get org cert success: {}user ca certs path is '{}',{}user msp certs public key path is '{}',{}user msp certs private key path is '{}',{}user tls certs public key path is '{}',{}user tls certs private key path is '{}'{}."
                        , System.lineSeparator(), certUserCaCert, System.lineSeparator(), certUserPubKey, System.lineSeparator(), certUserPriKey, System.lineSeparator(), certTlsPubKey, System.lineSeparator(), certTlsPriKey, System.lineSeparator());

                // ?????????????????????configtx.yaml
                String orgMspDir = "../../../crypto-config/peerOrganizations/" + bcOrg.getOrgName().toLowerCase() + "-" + clusterName + "/msp";
                String yamlFilePath = BlockChainK8SConstant.getSavePath() + File.separator + clusterName + File.separator + "channels" + File.separator + clusterName + File.separator + bcOrg.getOrgName().toLowerCase() + File.separator + "configtx.yaml";
                if (!ConfigTxUtils.generateOrgConfigTxYaml(bcOrg.getOrgName(), bcOrg.getOrgMspId(), orgMspDir, yamlFilePath)) {
                    throw new K8SException();
                }

                // ?????????MSP??????????????????????????????
                if (!ShellUtils.exec(scriptsPath + ADD_ORG_TO_SYS_CHANNEL, clusterName, bcOrg.getOrgName(), "Orderer", "orderer0", BlockChainK8SConstant.getFabricToolsPath(bcCluster.getClusterVersion()), BlockChainK8SConstant.getFabricOperateScriptsPath(), BlockChainK8SConstant.getSavePath())) {
                    logger.error("[shell->cert] shell to add org in fabric system channel, add org error, org name is :{}, cluster name is :{}", bcOrg.getOrgName(), clusterName);
                    throw new K8SException();
                }
                break;
            }
            default: {
                logger.error("[shell->cert] shell to get org cert, maybe get error type value from parameter, error value is :{}", bcOrg.getOrgType());
                throw new K8SException(K8SResultCode.SHELL_EXEC_TYPE_ERROR);
            }
        }

        bcCert.setId(StringUtils.getId());
        bcCert.setCertName("Admin");
        bcCert.setCertStatus(1);
        bcCert.setCertPubKey(certUserPubKey);
        bcCert.setCertPriKey(certUserPriKey);
        bcCert.setCertCaCert(certUserCaCert);
        bcCert.setCertTlsPriKey(certTlsPriKey);
        bcCert.setCertPubKey(certTlsPubKey);
        bcCert.setOrgId(bcOrg.getId());

        return bcCert;
    }

    private static Map<String, Integer> createOrdererService(String clusterName, int ordererNum) {
        LinkedHashMap<String, Integer> ordererNodePorts = new LinkedHashMap<>();
        for (int i = 0; i < ordererNum; i++) {
            final String ordererName = "orderer" + i;
            final Map<String, String> labels = new HashMap<String, String>(4) {{
                put("namespace", clusterName);
                put("app", APP_NAME);
                put("role", "orderer");
                put("orderer-name", ordererName);
            }};

            // ??????service
            V1ServiceSpec svcSpec = new V1ServiceSpec()
                    .type("NodePort")
                    .selector(labels)
                    .ports(new ArrayList<V1ServicePort>() {{
                        add(new V1ServicePort().name("listen").protocol("TCP").port(ORDERER_PORT).targetPort(new IntOrString(ORDERER_PORT)));
                        add(new V1ServicePort().name("monitor").protocol("TCP").port(ORDERER_MONITOR_PORT).targetPort(new IntOrString(ORDERER_MONITOR_PORT)));
                    }});
            K8SUtils.createService(clusterName, ordererName, labels, svcSpec);
            logger.info("create service {} successful.", ordererName);

//            Integer nodePort = K8SUtils.queryNodePort(ordererName, clusterName);
//            if (nodePort <= 0) {
//                logger.error("query service -> {}'s node port from namespace -> {} error. it must be greater than 30000.", ordererName, clusterName);
//                throw new K8SException();
//            }
//            ordererNodePorts.put(ordererName, nodePort);
            ordererNodePorts.put(ordererName, ORDERER_PORT);
        }
        return ordererNodePorts;
    }

    public static void buildFabricChain(BCCluster bcCluster, BCOrg bcOrg) {
        // ???config???????????????pv???????????????
        if (!ShellUtils.exec(BlockChainK8SConstant.getFabricOperateScriptsPath() + COMMON_OPERATE_SCRIPT_NAME, "copy_config", bcCluster.getClusterName(), BlockChainK8SConstant.getFabricConfigPath(), BlockChainK8SConstant.getFabricOperateScriptsPath(), BlockChainK8SConstant.getSavePath(), "")) {
            //??????????????????
            logger.error("copy yaml config file [orderer.yaml & core.yaml] error.");
            throw new K8SException();
        }

        // ?????????????????????orderer service????????????????????????orderer serivce???NodePort
        Map<String, Integer> ordererNodePorts = createOrdererService(bcCluster.getClusterName(), bcCluster.getOrdererCount());

        // ????????????????????????orderer?????????????????????kafka???raft???orderer?????????????????????
        List<String> ordererAddressList = new ArrayList<>();
        List<String> kafkaAddressList = new ArrayList<>();
        List<Map<String, Object>> raftConsensus = new ArrayList<>();
        switch (bcCluster.getClusterConsensusType()) {
            case 1:
                for (int i = 0; i < bcCluster.getOrdererCount(); i++) {
                    // ??????ordererAddressList
                    ordererAddressList.add("orderer" + i + "." + bcCluster.getClusterName() + ":" + ordererNodePorts.get("orderer" + i));
                }
                break;
            case 3:
                for (int i = 0; i < bcCluster.getOrdererCount(); i++) {
                    // ??????ordererAddressList
                    ordererAddressList.add("orderer" + i + "." + bcCluster.getClusterName() + ":" + 7050);
                    // ??????raftConsensus
                    Map<String, Object> consensus = new LinkedHashMap<>();
                    consensus.put("Host", "orderer" + i + "." + bcCluster.getClusterName());
                    consensus.put("Port", ordererNodePorts.get("orderer" + i));
                    consensus.put("ClientTLSCert", "../../crypto-config/ordererOrganizations/" + bcCluster.getClusterName() + "/orderers/orderer" + i + "." + bcCluster.getClusterName() + "/tls/server.crt");
                    consensus.put("ServerTLSCert", "../../crypto-config/ordererOrganizations/" + bcCluster.getClusterName() + "/orderers/orderer" + i + "." + bcCluster.getClusterName() + "/tls/server.crt");
                    raftConsensus.add(consensus);
                }
                break;
            default:
                logger.error("Invalid consensus strategy. currently auto only support 'kafka' or 'raft' consenter type.");
                throw new K8SException();
        }

        // ??????configtx.yaml
        String ordererOrgMspDir = "../../crypto-config/ordererOrganizations/" + bcCluster.getClusterName().toLowerCase() + "/msp";
        String yamlFilePath = BlockChainK8SConstant.getSavePath() + File.separator + bcCluster.getClusterName().toLowerCase() + File.separator + "channels" + File.separator + bcCluster.getClusterName().toLowerCase() + File.separator + "configtx.yaml";
        if (!ConfigTxUtils.generateSysConfigTxYaml(bcCluster.getClusterConsensusType(), bcOrg.getOrgName(), bcOrg.getOrgMspId(), ordererOrgMspDir, ordererAddressList, kafkaAddressList, raftConsensus, yamlFilePath)) {
            throw new K8SException();
        }

        // ???????????????????????????genesis.block
        if (!ShellUtils.exec(BlockChainK8SConstant.getFabricOperateScriptsPath() + GENERATE_GENESIS_SCRIPT_NAME, bcCluster.getClusterName().toLowerCase(), bcCluster.getClusterName().toLowerCase(), BlockChainK8SConstant.getFabricToolsPath(bcCluster.getClusterVersion()), BlockChainK8SConstant.getFabricOperateScriptsPath(), BlockChainK8SConstant.getSavePath())) {
            logger.error("generate orderer genesis block by system channel -> {} error.", bcCluster.getClusterName());
            throw new K8SException();
        }
    }

    public static BCChannel buildFabricChannel(BCCluster bcCluster, List<String> orgNameList, BCNode ordererNode, BCChannel bcChannel) {

        // ??????configtx.yaml
        JSONArray arr = new JSONArray();
        for (int i = 0; i < orgNameList.size(); i++) {
            String orgPeerName = orgNameList.get(i);
            JSONObject jo = new JSONObject();
            jo.put("orgName", orgPeerName);
            jo.put("orgMspId", orgPeerName + "MSP");
            jo.put("orgMspDir", "../../crypto-config/peerOrganizations/" + orgPeerName.toLowerCase() + "-" + bcCluster.getClusterName() + "/msp");
            arr.add(jo);
        }

        // ??????????????????????????????????????????????????????????????????
        String createOrgNameRandom = orgNameList.get(new Random().nextInt(orgNameList.size()));

        // ??????configtx.yaml
        String yamlFilePath = BlockChainK8SConstant.getSavePath() + File.separator + bcCluster.getClusterName().toLowerCase() + File.separator + "channels" + File.separator + bcChannel.getChannelName() + File.separator + "configtx.yaml";
        if (!ConfigTxUtils.generateAppConfigTxYaml(arr, yamlFilePath)) {
            throw new K8SException();
        }

        // ??????channel.tx ???????????????
        if (ShellUtils.exec(BlockChainK8SConstant.getFabricOperateScriptsPath() + CREATE_CHANNEL_SCRIPT_NAME, bcCluster.getClusterName(), bcChannel.getChannelName(), createOrgNameRandom, ordererNode.getNodeName(), BlockChainK8SConstant.getFabricToolsPath(bcCluster.getClusterVersion()), BlockChainK8SConstant.getFabricOperateScriptsPath(), BlockChainK8SConstant.getSavePath())) {
            //??????channel??????
            bcChannel.setChannelStatus(2);
            return bcChannel;
        }

        return null;
    }


    public static void nodeJoinFabricChannel(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String clusterName = jsonObject.getString("clusterName");
            String peerName = jsonObject.getString("peerName");
            String channelName = jsonObject.getString("channelName");
            String orgName = jsonObject.getString("orgName");
            String ordererOrgName = jsonObject.getString("ordererOrgName");
            String ordererName = jsonObject.getString("ordererName");
            String clusterVersion = jsonObject.getString("clusterVersion");

            if (!ShellUtils.exec(BlockChainK8SConstant.getFabricOperateScriptsPath() + JOIN_CHANNEL_SCRIPT_NAME, clusterName, channelName, orgName, peerName, PEER_PORT + "", ordererOrgName, ordererName, BlockChainK8SConstant.getFabricToolsPath(clusterVersion), BlockChainK8SConstant.getFabricOperateScriptsPath(), BlockChainK8SConstant.getSavePath())) {
                logger.error("peer -> {} join channel -> {} error.", peerName, channelName);
                throw new K8SException();
            }
        }
    }
}
