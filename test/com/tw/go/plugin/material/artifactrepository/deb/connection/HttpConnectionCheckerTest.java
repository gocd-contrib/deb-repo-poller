package com.tw.go.plugin.material.artifactrepository.deb.connection;

import com.tw.go.plugin.material.artifactrepository.deb.model.Credentials;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

public class HttpConnectionCheckerTest {
    @Test
    public void shouldNotThrowExceptionIfCheckConnectionToTheRepoPasses() {
        new HttpConnectionChecker().checkConnection("http://archive.ubuntu.com/ubuntu/dists/xenial/main/binary-amd64/", new Credentials(null, null));
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenUrlIsNotReachable() {
        try {
            new HttpConnectionChecker().checkConnection("https://build.gocd.io/go/api/support", new Credentials(null, null));
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("HTTP/1.1 401"));
        }
    }
}
