package com.tw.go.plugin.material.artifactrepository.deb.connection;

import com.tw.go.plugin.material.artifactrepository.deb.model.Credentials;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HttpConnectionCheckerTest {
    @Test
    public void shouldNotThrowExceptionIfCheckConnectionToTheRepoPasses() {
        new HttpConnectionChecker().checkConnection("http://in.archive.ubuntu.com/ubuntu/dists/saucy/main/binary-arm64/", new Credentials(null, null));
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenUrlIsNotReachable() {
        try {
            new HttpConnectionChecker().checkConnection("http://sifystdgobgr101.thoughtworks.com:8080/tfs/1.txt", new Credentials(null, null));
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("HTTP/1.1 401 Unauthorized"));
        }
    }
}
