package com.tw.go.plugin.material.artifactrepository.deb.poller;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.Result;
import com.tw.go.plugin.material.artifactrepository.deb.constants.Constants;
import com.tw.go.plugin.material.artifactrepository.util.ReflectionUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.Date;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DebRepositoryPollerTest {
    private DebRepositoryPoller poller;
    private RepositoryConfiguration repositoryPackageConfigurations;
    private PackageConfiguration packagePackageConfigurations;
    private File sampleRepoDirectory;
    private String repoUrl;

    @Before
    public void setup() {
        repositoryPackageConfigurations = new RepositoryConfiguration();

        sampleRepoDirectory = new File("test/repos/samplerepo");
        repoUrl = "file://" + sampleRepoDirectory.getAbsolutePath();
        repositoryPackageConfigurations.add(new Property(Constants.REPO_URL, repoUrl));

        packagePackageConfigurations = new PackageConfiguration();
        packagePackageConfigurations.add(new Property(Constants.PACKAGE_NAME, "go-agent"));

        poller = new DebRepositoryPoller();
    }

    @Test
    public void shouldGetLatestModificationGivenPackageAndRepoConfigurations_getLatestRevision() {
        PackageRevision latestRevision = poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
        assertThat(latestRevision.getRevision(), is("go-agent.13.3.0-17921.all"));

        assertThat(latestRevision.getDataFor(Constants.PACKAGE_LOCATION), is("file://" + sampleRepoDirectory.getAbsolutePath() + "/contrib/go-agent-13.3.0-17921.deb"));
    }

    @Test
    public void shouldThrowExceptionWhileGettingLatestRevisionIfCheckConnectionFails_getLatestRevision() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        repositoryPackageConfigurations.add(new Property(Constants.REPO_URL, "file://foo/bar"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Invalid file path."));
        }
    }

    @Test
    public void shouldGetTheRightLocationForAnyPackage_getLatestRevision() {
        PackageConfiguration ppc = new PackageConfiguration();
        ppc.add(new Property(Constants.PACKAGE_NAME, "go-server"));
        PackageRevision latestRevision = poller.getLatestRevision(ppc, repositoryPackageConfigurations);

        assertThat(latestRevision.getRevision(), is("go-server.13.3.0-17921.all"));
        assertThat(latestRevision.getDataFor(Constants.PACKAGE_LOCATION), is("file://" + sampleRepoDirectory.getAbsolutePath() + "/contrib/go-server-13.3.0-17921.deb"));
    }

    @Test
    public void shouldThrowExceptionGivenNonExistingRepo_getLatestRevision() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        repositoryPackageConfigurations.add(new Property(Constants.REPO_URL, "file://crap-repo"));
        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        packagePackageConfigurations.add(new Property(Constants.PACKAGE_NAME, "crap-artifact"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e.getMessage().startsWith("Invalid file path."), is(true));
        }
    }

    @Test
    public void shouldThrowExceptionGivenNonExistingPackageInExistingRepo_getLatestRevision() {
        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        packagePackageConfigurations.add(new Property(Constants.PACKAGE_NAME, "crap-artifact"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            String expectedMessage = String.format("Error while querying repository with path '%s' and package '%s'.", repositoryPackageConfigurations.get(Constants.REPO_URL).getValue(), "crap-artifact");
            String actualMessage = e.getMessage();
            assertThat(actualMessage.startsWith(expectedMessage), is((true)));
        }
    }

    @Test
    public void shouldThrowExceptionGivenEmptyRepo_getLatestRevision() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        File emptyRepo = new File("test/repos/emptyrepo");
        repositoryPackageConfigurations.add(new Property(Constants.REPO_URL, "file://" + emptyRepo.getAbsolutePath()));
        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        packagePackageConfigurations.add(new Property(Constants.PACKAGE_NAME, "crap-artifact"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            String expectedMessage = "Invalid file path.";
            String actualMessage = e.getMessage();
            assertThat(actualMessage.startsWith(expectedMessage), is((true)));
        }
    }

    @Test
    public void shouldPerformRepositoryConfigurationBeforeModificationCheck_getLatestRevision() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();

        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        packagePackageConfigurations.add(new Property(Constants.PACKAGE_NAME, "crap-artifact"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is(("Repository URL not specified")));
        }
    }

    @Test
    public void shouldPerformPackageConfigurationBeforeModificationCheck() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        File emptyRepo = new File("test/repos/emptyrepo");
        repositoryPackageConfigurations.add(new Property(Constants.REPO_URL, "file://" + emptyRepo.getAbsolutePath()));

        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is(("Debian Package Name not specified")));
        }
    }

    @Test
    public void testShouldConcatenateErrorsWhenModificationCheckFails() {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        PackageConfiguration packagePackageConfigurations = new PackageConfiguration();
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is(("Repository URL not specified; Debian Package Name not specified")));
        }
    }

    @Test
    public void shouldGetLatestModificationSinceGivenPackageAndRepoConfigurationsAndPreviouslyKnownRevision() {
        PackageRevision latestRevision = poller.latestModificationSince(packagePackageConfigurations, repositoryPackageConfigurations, new PackageRevision("symlinks-1.2-24.2.2.i386", new Date(fromEpochTime(1263710418L)), null));
        assertThat(latestRevision.getRevision(), is("go-agent.13.3.0-17921.all"));
    }

    @Test
    public void shouldReturnNullGivenPackageAndRepoConfigurationsAndPreviouslyKnownRevision() {
        PackageRevision latestRevision = poller.latestModificationSince(packagePackageConfigurations, repositoryPackageConfigurations, new PackageRevision("go-agent.13.3.0-17921.all", new Date(fromEpochTime(1365054258L)), null));
        assertThat(latestRevision, is(nullValue()));
    }

    @Test
    public void shouldReturnNullWhenPreviouslyKnownPackageRevisionIsSameAsCurrent() {
        DebRepositoryPoller spy = spy(poller);
        when(spy.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations)).thenReturn(new PackageRevision("go-agent-13.1.1-16714-noarch", new Date(fromEpochTime(1365054258L)), null));
        PackageRevision latestRevision = poller.latestModificationSince(packagePackageConfigurations, repositoryPackageConfigurations, new PackageRevision("go-agent.13.3.0-17921.all", new Date(fromEpochTime(1365054258L)), null));
        assertThat(latestRevision, is(nullValue()));
    }

    @Ignore
    @Test
    public void shouldNotThrowUpWhenDataKeyIsInvalid() throws Exception {
        String invalidKey = "!INVALID";
        ReflectionUtil.setStaticField(Constants.class, "PACKAGE_LOCATION", invalidKey);
        try {
            PackageRevision latestRevision = null;
            try {
                latestRevision = poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            } catch (Exception e) {
                e.printStackTrace();
                fail("should not throw exception");
            }
            assertThat(latestRevision.getDataFor(invalidKey), is(nullValue()));
        } finally {
            ReflectionUtil.setStaticField(Constants.class, "PACKAGE_LOCATION", "LOCATION");
        }
    }

    @Ignore
    @Test
    public void shouldThrowExceptionIfCredentialsHaveBeenProvidedAlongWithFileProtocol() {
        repositoryPackageConfigurations.add(new Property(Constants.USERNAME, "loser"));
        repositoryPackageConfigurations.add(new Property(Constants.PASSWORD, "pwd"));
        try {
            poller.getLatestRevision(packagePackageConfigurations, repositoryPackageConfigurations);
            fail("Should have failed");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("File protocol does not support username and/or password."));
        }
    }

    @Test
    public void shouldCheckRepoConnection() throws Exception {
        assertThat(poller.checkConnectionToRepository(repositoryPackageConfigurations).isSuccessful(), is(true));
        assertThat(poller.checkConnectionToRepository(repositoryPackageConfigurations).getMessages().size(), is(1));
        assertThat(poller.checkConnectionToRepository(repositoryPackageConfigurations).getMessages().get(0), is(String.format("Successfully accessed repository metadata at %s", repoUrl + "/Packages.gz")));
    }

    @Test
    public void shouldReturnErrorsWhenConnectionToRepoFails() throws Exception {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        repositoryPackageConfigurations.add(new Property(Constants.REPO_URL, "file://invalid_path"));

        Result result = poller.checkConnectionToRepository(repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessages().get(0), is("Could not access file - file://invalid_path/Packages.gz. Invalid file path."));
    }

    @Test
    public void shouldPerformRepoValidationsBeforeCheckConnection() throws Exception {
        RepositoryConfiguration repositoryPackageConfigurations = new RepositoryConfiguration();
        repositoryPackageConfigurations.add(new Property(Constants.REPO_URL, "ftp://username:password@invalid_path"));

        Result result = poller.checkConnectionToRepository(repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessages().size(), is(2));
        assertThat(result.getMessages().get(0), is("Invalid URL: Only 'file' and 'http' protocols are supported."));
        assertThat(result.getMessages().get(1), is("User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys."));
    }

    @Test
    public void shouldCheckConnectionToPackageAndRespondWithLatestPackageFound() {
        Result result = poller.checkConnectionToPackage(packagePackageConfigurations, repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(true));
        assertThat(result.getMessagesForDisplay(), is("Found package 'go-agent.13.3.0-17921.all'."));
    }

    @Test
    public void shouldFailConnectionToPackageRepositoryIfPackageIsNotFound() {
        PackageConfiguration packageConfigurations = new PackageConfiguration();
        packageConfigurations.add(new Property(Constants.PACKAGE_NAME, "go-a"));
        Result result = poller.checkConnectionToPackage(packageConfigurations, repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessagesForDisplay(), is("Could not find any package that matched requirements."));
    }

    @Ignore
    @Test
    public void shouldFailConnectionToPackageRepositoryIfMultiplePackageIsFound() {
        PackageConfiguration packageConfigurations = new PackageConfiguration();
        packageConfigurations.add(new Property(Constants.PACKAGE_NAME, "go*"));
        Result result = poller.checkConnectionToPackage(packageConfigurations, repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(false));
        String messagesForDisplay = result.getMessagesForDisplay();
        System.out.println(messagesForDisplay);
        assertThat(messagesForDisplay.startsWith("Given Package Spec (go*) resolves to more than one file on the repository: "), is(true));
        assertThat(messagesForDisplay.contains("go-agent-13.1.1-16714.noarch.rpm"), is(true));
        assertThat(messagesForDisplay.contains("go-server-13.1.1-16714.noarch.rpm"), is(true));
    }

    @Test
    public void shouldFailConnectionToPackageRepositoryIfRepositoryIsNotReachable() {
        RepositoryConfiguration repositoryConfigurations = new RepositoryConfiguration();
        repositoryConfigurations.add(new Property(Constants.REPO_URL, "file://invalid_random_2q342340"));
        Result result = poller.checkConnectionToPackage(packagePackageConfigurations, repositoryConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessagesForDisplay(), is("Could not access file - file://invalid_random_2q342340/Packages.gz. Invalid file path."));
    }

    @Test
    public void shouldValidatePackageDataWhileTestingConnection() {
        Result result = poller.checkConnectionToPackage(new PackageConfiguration(), repositoryPackageConfigurations);
        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessagesForDisplay(), is("Debian Package Name not specified"));
    }

    private long fromEpochTime(long timeInSeconds) {
        return timeInSeconds * 1000;
    }

    @After
    public void tearDown() throws Exception {
        //FileUtils.cleanDirectory(new File("/var/tmp"));
    }
}
