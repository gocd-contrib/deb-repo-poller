package com.tw.go.plugin.material.artifactrepository.deb.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.material.artifactrepository.deb.constants.Constants;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.tw.go.plugin.material.artifactrepository.deb.constants.Constants.*;
import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class DebRepositoryConfigurationTest {
    private DebRepositoryConfiguration debRepositoryConfiguration;

    @Before
    public void setUp() {
        debRepositoryConfiguration = new DebRepositoryConfiguration();
    }

    @Test
    public void shouldGetRepositoryConfiguration() {
        RepositoryConfiguration configurations = debRepositoryConfiguration.getRepositoryConfiguration();
        assertThat(configurations.get(REPO_URL), is(notNullValue()));
        assertThat(configurations.get(Constants.REPO_URL).getOption(Property.SECURE), is(false));
        assertThat(configurations.get(Constants.REPO_URL).getOption(Property.PART_OF_IDENTITY), is(true));
        assertThat(configurations.get(Constants.REPO_URL).getOption(Property.REQUIRED), is(true));
        assertThat(configurations.get(Constants.REPO_URL).getOption(Property.DISPLAY_NAME), is("Repository URL"));
        assertThat(configurations.get(Constants.REPO_URL).getOption(Property.DISPLAY_ORDER), is(0));
        /*
        assertThat(configurations.get(Constants.USERNAME), is(notNullValue()));
        assertThat(configurations.get(Constants.USERNAME).getOption(Property.SECURE), is(false));
        assertThat(configurations.get(Constants.USERNAME).getOption(Property.PART_OF_IDENTITY), is(false));
        assertThat(configurations.get(Constants.USERNAME).getOption(Property.REQUIRED), is(false));
        assertThat(configurations.get(Constants.USERNAME).getOption(Property.DISPLAY_NAME), is("User"));
        assertThat(configurations.get(Constants.USERNAME).getOption(Property.DISPLAY_ORDER), is(1));
        assertThat(configurations.get(Constants.PASSWORD), is(notNullValue()));
        assertThat(configurations.get(Constants.PASSWORD).getOption(Property.SECURE), is(true));
        assertThat(configurations.get(Constants.PASSWORD).getOption(Property.PART_OF_IDENTITY), is(false));
        assertThat(configurations.get(Constants.PASSWORD).getOption(Property.REQUIRED), is(false));
        assertThat(configurations.get(Constants.PASSWORD).getOption(Property.DISPLAY_NAME), is("Password"));
        assertThat(configurations.get(Constants.PASSWORD).getOption(Property.DISPLAY_ORDER), is(2));
        */
    }

    @Test
    public void shouldGetPackageConfiguration() {
        PackageConfiguration configurations = debRepositoryConfiguration.getPackageConfiguration();
        assertThat(configurations.get(PACKAGE_NAME), is(notNullValue()));
        assertThat(configurations.get(Constants.PACKAGE_NAME).getOption(Property.SECURE), is(false));
        assertThat(configurations.get(Constants.PACKAGE_NAME).getOption(Property.PART_OF_IDENTITY), is(true));
        assertThat(configurations.get(Constants.PACKAGE_NAME).getOption(Property.REQUIRED), is(true));
        assertThat(configurations.get(Constants.PACKAGE_NAME).getOption(Property.DISPLAY_NAME), is("Package Name"));
        assertThat(configurations.get(Constants.PACKAGE_NAME).getOption(Property.DISPLAY_ORDER), is(0));
        assertThat(configurations.get(VERSION_SPEC), is(notNullValue()));
        assertThat(configurations.get(Constants.VERSION_SPEC).getOption(Property.SECURE), is(false));
        assertThat(configurations.get(Constants.VERSION_SPEC).getOption(Property.PART_OF_IDENTITY), is(true));
        assertThat(configurations.get(Constants.VERSION_SPEC).getOption(Property.REQUIRED), is(false));
        assertThat(configurations.get(Constants.VERSION_SPEC).getOption(Property.DISPLAY_NAME), is("Version Spec"));
        assertThat(configurations.get(Constants.VERSION_SPEC).getOption(Property.DISPLAY_ORDER), is(1));
        assertThat(configurations.get(ARCHITECTURE), is(notNullValue()));
        assertThat(configurations.get(Constants.ARCHITECTURE).getOption(Property.SECURE), is(false));
        assertThat(configurations.get(Constants.ARCHITECTURE).getOption(Property.PART_OF_IDENTITY), is(true));
        assertThat(configurations.get(Constants.ARCHITECTURE).getOption(Property.REQUIRED), is(false));
        assertThat(configurations.get(Constants.ARCHITECTURE).getOption(Property.DISPLAY_NAME), is("Architecture"));
        assertThat(configurations.get(Constants.ARCHITECTURE).getOption(Property.DISPLAY_ORDER), is(2));
    }

    @Test
    public void shouldCorrectlyCheckIfRepositoryConfigurationValid() {
        assertForRepositoryConfigurationErrors(new RepositoryConfiguration(), asList(new ValidationError(REPO_URL, "Repository url not specified")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(REPO_URL, null), asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(REPO_URL, ""), asList(new ValidationError(REPO_URL, "Repository url is empty")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(REPO_URL, "incorrectUrl"), asList(new ValidationError(REPO_URL, "Invalid URL : incorrectUrl")), false);
        assertForRepositoryConfigurationErrors(repoConfigurations(REPO_URL, "http://correct.com/url"), new ArrayList<ValidationError>(), true);
    }

    @Test
    public void shouldCorrectlyCheckIfPackageConfigurationValid() {
        assertForPackageConfigurationErrors(new PackageConfiguration(), asList(new ValidationError(PACKAGE_NAME, "Package Name not specified")), false);
        assertForPackageConfigurationErrors(packageConfigurations(PACKAGE_NAME, null), asList(new ValidationError(PACKAGE_NAME, "Package Name is null")), false);
        assertForPackageConfigurationErrors(packageConfigurations(PACKAGE_NAME, ""), asList(new ValidationError(PACKAGE_NAME, "Package Name is empty")), false);
        assertForPackageConfigurationErrors(packageConfigurations(PACKAGE_NAME, "go-age?nt-*"), new ArrayList<ValidationError>(), true);
        assertForPackageConfigurationErrors(packageConfigurations(PACKAGE_NAME, "go-agent"), new ArrayList<ValidationError>(), true);
    }

    @Test
    public void shouldValidateConfig() throws Exception {
        ValidationResult validationResult = new ValidationResult();
        new DebRepositoryConfiguration().validate(new PackageConfiguration(), new RepositoryConfiguration(), validationResult);
        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getErrors().contains(new ValidationError(REPO_URL, "Repository url not specified")), is(true));
        assertThat(validationResult.getErrors().contains(new ValidationError(PACKAGE_NAME, "Package Name not specified")), is(true));
    }

    @Test
    public void shouldFailValidationIfSpuriousPropertiesAreConfigured() {
        ValidationResult validationResult = new ValidationResult();
        PackageConfiguration packageConfigurations = new PackageConfiguration();
        RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
        packageConfigurations.add(new Property("PACKAGE_NAME", "foo"));
        packageConfigurations.add(new Property("foo1", "foo"));
        packageConfigurations.add(new Property("foo2", "foo"));
        repositoryConfiguration.add(new Property("bar1", "bar"));
        repositoryConfiguration.add(new Property("bar2", "bar"));
        repositoryConfiguration.add(new Property("REPO_URL", "http://asdsa"));
        new DebRepositoryConfiguration().validate(packageConfigurations, repositoryConfiguration, validationResult);
        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getErrors().contains(new ValidationError("", "Unsupported key(s) found : bar1, bar2. Allowed key(s) are : REPO_URL")), is(true));
        assertThat(validationResult.getErrors().contains(new ValidationError("", "Unsupported key(s) found : foo1, foo2. Allowed key(s) are : PACKAGE_NAME, VERSION_SPEC, ARCHITECTURE")), is(true));
    }

    @Test
    public void shouldCorrectlyTestConnectionGivenCorrectConfiguration() {
        File sampleRepoDirectory = new File("test/repos/samplerepo");

        RepositoryConfiguration repositoryConfigurations = repoConfigurations(REPO_URL, "file://" + sampleRepoDirectory.getAbsolutePath());
        PackageConfiguration packageConfigurations = packageConfigurations(PACKAGE_NAME, "go-agent");

        try {
            debRepositoryConfiguration.testConnection(packageConfigurations, repositoryConfigurations);
        } catch (Exception e) {
            fail("");
        }
    }

    @Test
    public void shouldCorrectlyTestConnectionGivenIncorrectConfiguration() {
        RepositoryConfiguration repositoryConfigurations = repoConfigurations(REPO_URL, "file://crap-repo");
        PackageConfiguration packageConfigurations = packageConfigurations(PACKAGE_NAME, "go-agent");

        try {
            debRepositoryConfiguration.testConnection(packageConfigurations, repositoryConfigurations);
            fail("");
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), is("Test Connection failed."));
        }
    }

    private void assertForRepositoryConfigurationErrors(RepositoryConfiguration repositoryConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        ValidationResult validationResult = debRepositoryConfiguration.isRepositoryConfigurationValid(repositoryConfigurations);
        assertThat(validationResult.isSuccessful(), is(expectedValidationResult));
        assertThat(validationResult.getErrors().size(), is(expectedErrors.size()));
        assertThat(validationResult.getErrors().containsAll(expectedErrors), is(true));
    }

    private void assertForPackageConfigurationErrors(PackageConfiguration packageConfigurations, List<ValidationError> expectedErrors, boolean expectedValidationResult) {
        ValidationResult validationResult = debRepositoryConfiguration.isPackageConfigurationValid(packageConfigurations, new RepositoryConfiguration());
        assertThat(validationResult.isSuccessful(), is(expectedValidationResult));
        assertThat(validationResult.getErrors().size(), is(expectedErrors.size()));
        assertThat(validationResult.getErrors().containsAll(expectedErrors), is(true));
    }

    private PackageConfiguration packageConfigurations(String key, String value) {
        PackageConfiguration configurations = new PackageConfiguration();
        configurations.add(new Property(key, value));
        return configurations;
    }

    private RepositoryConfiguration repoConfigurations(String key, String value) {
        RepositoryConfiguration configurations = new RepositoryConfiguration();
        configurations.add(new Property(key, value));
        return configurations;
    }
}
