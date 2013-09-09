package com.tw.go.plugin.material.artifactrepository.deb.connection;

import com.tw.go.plugin.material.artifactrepository.deb.model.Credentials;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FileBasedConnectionCheckerTest {

    @Test
    public void shouldNotThrowExceptionIfFileExistsPasses() {
        String absolutePath = new File("").getAbsolutePath();
        new FileBasedConnectionChecker().checkConnection("file://" + absolutePath, new Credentials(null, null));
    }

    @Test
    public void shouldThrowExceptionIfUserNameAndPasswordIsProvided() {
        String absolutePath = new File("").getAbsolutePath();
        try {
            new FileBasedConnectionChecker().checkConnection("file://" + absolutePath, new Credentials("user", "pwd"));
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("File protocol does not support username and/or password."));
        }
    }

    @Test
    public void shouldFailCheckConnectionIfFileDoesNotExist() {
        try {
            new FileBasedConnectionChecker().checkConnection("file://foo", new Credentials(null, null));
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Invalid file path."));
        }
    }
}
