package com.tw.go.plugin.material.artifactrepository.deb.model;

import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.material.artifactrepository.deb.connection.ConnectionChecker;
import com.tw.go.plugin.material.artifactrepository.deb.connection.FileBasedConnectionChecker;
import com.tw.go.plugin.material.artifactrepository.deb.connection.HttpConnectionChecker;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.tw.go.plugin.material.artifactrepository.deb.constants.Constants.REPO_URL;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RepoUrlTest {
    @Test
    public void shouldCorrectlyCheckIfRepositoryConfigurationValid() {
        assertRepositoryUrlValidation("", null, null, asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertRepositoryUrlValidation(null, null, null, asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertRepositoryUrlValidation("  ", null, null, asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertRepositoryUrlValidation("h://localhost", null, null, asList(new ValidationError(REPO_URL, "Invalid URL : h://localhost")), false);
        assertRepositoryUrlValidation("ftp:///foo.bar", null, null, asList(new ValidationError(REPO_URL, "Invalid URL: Only 'file' and 'http' protocols are supported.")), false);
        assertRepositoryUrlValidation("incorrectUrl", null, null, asList(new ValidationError(REPO_URL, "Invalid URL : incorrectUrl")), false);
        assertRepositoryUrlValidation("http://user:password@localhost", null, null, asList(new ValidationError(REPO_URL, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys.")), false);
        assertRepositoryUrlValidation("http://correct.com/url", null, null, new ArrayList<ValidationError>(), true);
        assertRepositoryUrlValidation("file:///foo.bar", null, null, new ArrayList<ValidationError>(), true);
        assertRepositoryUrlValidation("file:///foo.bar", "user", "password", asList(new ValidationError(REPO_URL, "File protocol does not support username and/or password.")), false);
        assertRepositoryUrlValidation("https://correct.com/url", null, null, new ArrayList<ValidationError>(), true);
    }

    @Test
    public void shouldThrowUpWhenFileProtocolAndCredentialsAreUsed() throws Exception {
        RepoUrl repoUrl = new RepoUrl("file://foo.bar", null, "password");
        ValidationResult errors = new ValidationResult();

        repoUrl.validate(errors);

        assertThat(errors.isSuccessful(), is(false));
        assertThat(errors.getErrors().size(), is(1));
        assertThat(errors.getErrors().get(0).getMessage(), is("File protocol does not support username and/or password."));
    }

    @Test
    public void shouldReturnURLWithBasicAuth() {
        RepoUrl repoUrl = new RepoUrl("http://localhost", "user", "password");
        assertThat(repoUrl.getUrlWithBasicAuth(), is("http://user:password@localhost"));
    }

    @Test
    public void shouldReturnTheRightConnectionCheckerBasedOnUrlScheme() {
        ConnectionChecker checker = new RepoUrl("http://foobar.com", null, null).getChecker();
        assertThat(checker instanceof HttpConnectionChecker, is(true));

        checker = new RepoUrl("file://foo/bar", null, null).getChecker();
        assertThat(checker instanceof FileBasedConnectionChecker, is(true));
    }

    @Test
    public void shouldThrowExceptionIfURIIsInvalid_checkConnection() {
        try {
            new RepoUrl("://foobar.com", null, null).checkConnection();
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("Invalid URL: java.net.MalformedURLException: no protocol: ://foobar.com"));
        }
    }

    @Test
    public void shouldThrowExceptionIfSchemeIsInvalid_checkConnection() {
        try {
            new RepoUrl("httph://foobar.com", null, null).checkConnection();
            fail("should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Invalid URL: java.net.MalformedURLException: unknown protocol: httph"));
        }
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenHttpUrlIsNotReachable() {
        try {
            new RepoUrl("https://build.gocd.io/go/api/support", null, null).checkConnection();
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("HTTP/1.1 401"));
        }
    }

    @Test
    public void shouldFailCheckConnectionToTheRepoWhenRepoFileSystemPathIsNotReachable() {
        try {
            new RepoUrl("file:///foo/bar", null, null).checkConnection();
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Invalid file path."));
        }
    }

    @Test
    public void shouldNotThrowExceptionIfCheckConnectionToTheRepoPasses() {
        new RepoUrl("http://archive.ubuntu.com/ubuntu/dists/xenial/main/binary-amd64/", null, null).checkConnection();
        new RepoUrl("http://archive.ubuntu.com/ubuntu/dists/xenial/main/binary-amd64", null, null).checkConnection();
    }

    @Test
    public void shouldGetUrlForDisplay() throws Exception {
        assertThat(new RepoUrl("file:///foo/bar", null, null).forDisplay(), is("file:///foo/bar"));
    }

    @Test
    public void shouldGetRepoMetadataUrl() throws Exception {
        assertThat(new RepoUrl("file:///foo/bar", null, null).getRepoMetadataUrl(), is("file:///foo/bar/Packages.gz"));
        assertThat(new RepoUrl("file:///foo/bar/", null, null).getRepoMetadataUrl(), is("file:///foo/bar/Packages.gz"));
        assertThat(new RepoUrl("file:///foo/bar//", null, null).getRepoMetadataUrl(), is("file:///foo/bar/Packages.gz"));
    }

    private void assertRepositoryUrlValidation(String url, String username, String password, List<ValidationError> expectedErrors, boolean isSuccessful) {
        ValidationResult errors = new ValidationResult();
        new RepoUrl(url, username, password).validate(errors);
        assertThat(errors.isSuccessful(), is(isSuccessful));
        assertThat(errors.getErrors().size(), is(expectedErrors.size()));
        assertThat(errors.getErrors().containsAll(expectedErrors), is(true));
    }
}
