package org.bc.auto.utils;

import org.junit.jupiter.api.Test;

public class ShellUtilsTest {
    private static final String CMD_FILE_PATH = "/Users/admin/Workspace/java/github.com/fabric-k8s-auto-deploy/src/main/docker/auto/script/test.sh";

    @Test
    public void testExec(){
        ShellUtils.exec(CMD_FILE_PATH);
    }

    @Test
    public void testExecEmptyPath(){
        ShellUtils.exec("");
    }
}
