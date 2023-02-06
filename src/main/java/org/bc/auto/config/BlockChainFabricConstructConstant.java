package org.bc.auto.config;

public interface BlockChainFabricConstructConstant {

    String GOLANG = "GOLANG";
    String JAVA = "JAVA";
    String NODE = "NODE";

    String HYPERLEDGER_FABRIC = "HyperLedgerFabric";
    String METHOD = "method";
    String RESULT = "result";
    String ENVELOPES = "envelopes";

    int SUCCESS = 200;
    int ERROR = 500;

    String APP_NAME = "kscbc-auto-cluster";

    String STORAGE_CLASS_NAME = "managed-nfs-storage";

    int PDB_MAX_UNAVAILABLE = 1;

    String KAFKA_NAME = "kafka";
    String KAFKA_IMAGE = "hyperledger/fabric-kafka:0.4.18";
    String KAFKA_IMAGE_SINGLE = "reg.ksbc.com/support/kafka:2.12-2.5.0";
    String KAFKA_IMAGE_CLUSTER = "reg.ksbc.com/support/kafka:2.2.0";
    int KAFKA_NUM = 4;
    int KAFKA_PORT = 9092;

    String ZOO_KEEPER_NAME = "zookeeper";
    String ZOO_KEEPER_IMAGE = "hyperledger/fabric-zookeeper:0.4.18";
    String ZOO_KEEPER_IMAGE_SINGLE = "reg.ksbc.com/support/zookeeper:3.5";
    String ZOO_KEEPER_IMAGE_CLUSTER = "reg.ksbc.com/support/zookeeper:3.4.10";
    int ZOO_KEEPER_NUM = 3;
    int ZOO_KEEPER_CLIENT_PORT = 2181;
    int ZOO_KEEPER_SERVER_PORT = 2888;
    int ZOO_KEEPER_LEADER_ELECTION_PORT = 3888;

    String MSP_CA_NAME = "msp-root-ca";
    String MSP_CA_IMAGE = "hyperledger/fabric-ca-ml:1.4.5";

    int CA_PORT = 7054;

    String TLS_CA_NAME = "tls-root-ca";
    String TLS_CA_IMAGE = "hyperledger/fabric-ca-ml:1.4.5";

    String ORDERER_ORG_NAME = "Orderer";
    String ORDERER_IMAGE = "hyperledger/fabric-orderer:1.4.5";
    int ORDERER_PORT = 7050;
    int ORDERER_MONITOR_PORT = 8443;

    String COUCH_DB_IMAGE = "hyperledger/fabric-couchdb:0.4.18";
    int COUCH_DB_PORT = 5984;
    String COUCH_DB_USERNAME = "kscbc-auto-cluster-username";
    String COUCH_DB_PASSWORD = "kscbc-auto-cluster-password";

    String PEER_IMAGE = "hyperledger/fabric-peer:1.4.5";
    int PEER_PORT = 7051;
    int PEER_CHAINCODE_PORT = 7052;

    String CLI_IMAGE = "hyperledger/fabric-tools:1.4.5";

    String ROOT_CA_LOGIN_INFO = "admin:adminpw";
    String INTERMEDIATE_CA_LOGIN_INFO = "admin1:adminpw";

    String COMMON_OPERATE_SCRIPT_NAME = "common-operate.sh";
    String GENERATE_GENESIS_SCRIPT_NAME = "generate-genesis.sh";
    String CREATE_CHANNEL_SCRIPT_NAME = "create-channel.sh";
    String JOIN_CHANNEL_SCRIPT_NAME = "join-channel.sh";
    String ADD_ORG_TO_SYS_CHANNEL = "add-org-to-syschannel.sh";
    String UPDATE_APP_CHANNEL_ANCHOR_PEER = "update-appchannel-anchor-peer.sh";
    String ORDERER_MSP_SCRIPT_NAME = "generate-orderer-msp-certs.sh";
    String ORDERER_TLS_SCRIPT_NAME = "generate-orderer-tls-certs.sh";
    String PEER_MSP_SCRIPT_NAME = "generate-peer-msp-certs.sh";
    String PEER_TLS_SCRIPT_NAME = "generate-peer-tls-certs.sh";

}
