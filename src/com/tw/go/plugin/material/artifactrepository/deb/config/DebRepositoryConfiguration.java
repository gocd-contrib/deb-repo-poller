package com.tw.go.plugin.material.artifactrepository.deb.config;

import com.thoughtworks.go.plugin.api.material.packagerepository.*;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.material.artifactrepository.deb.constants.Constants;
import com.tw.go.plugin.material.artifactrepository.deb.model.RepoUrl;
import com.tw.go.plugin.material.artifactrepository.deb.poller.DebRepositoryPoller;
import com.tw.go.plugin.material.artifactrepository.util.ListUtil;

import java.util.ArrayList;
import java.util.List;

import static com.thoughtworks.go.plugin.api.material.packagerepository.Property.*;
import static org.apache.commons.lang.StringUtils.isBlank;

public class DebRepositoryConfiguration implements PackageMaterialConfiguration {

    public RepositoryConfiguration getRepositoryConfiguration() {
        RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
        repositoryConfiguration.add(new Property(Constants.REPO_URL).with(DISPLAY_NAME, "Repository URL").with(DISPLAY_ORDER, 0));
        //repositoryConfiguration.addConfiguration(new Property(Constants.USERNAME).with(REQUIRED, false).with(PART_OF_IDENTITY, false).with(DISPLAY_NAME, "User").with(DISPLAY_ORDER, 1));
        //repositoryConfiguration.addConfiguration(new Property(Constants.PASSWORD).with(REQUIRED, false).with(PART_OF_IDENTITY, false).with(SECURE, true).with(DISPLAY_NAME, "Password").with(DISPLAY_ORDER, 2));
        return repositoryConfiguration;
    }

    public PackageConfiguration getPackageConfiguration() {
        PackageConfiguration packageConfiguration = new PackageConfiguration();
        packageConfiguration.add(new Property(Constants.PACKAGE_NAME).with(DISPLAY_NAME, "Package Name").with(DISPLAY_ORDER, 0));
        packageConfiguration.add(new Property(Constants.VERSION_SPEC).with(DISPLAY_NAME, "Version Spec").with(REQUIRED, false).with(DISPLAY_ORDER, 1));
        packageConfiguration.add(new Property(Constants.ARCHITECTURE).with(DISPLAY_NAME, "Architecture").with(REQUIRED, false).with(DISPLAY_ORDER, 2));
        return packageConfiguration;
    }

    public ValidationResult isRepositoryConfigurationValid(RepositoryConfiguration repositoryConfiguration) {
        ValidationResult validationResult = new ValidationResult();
        validateKeys(getRepositoryConfiguration(), repositoryConfiguration, validationResult);

        Property repositoryUrl = repositoryConfiguration.get(Constants.REPO_URL);
        //Property username = repositoryConfiguration.get(Constants.USERNAME);
        //Property password = repositoryConfiguration.get(Constants.PASSWORD);

        if (repositoryUrl == null) {
            validationResult.addError(new ValidationError(Constants.REPO_URL, "Repository url not specified"));
            return validationResult;
        }
        //String usernameValue = username == null ? null : username.getValue();
        //String passwordValue = password == null ? null : password.getValue();

        new RepoUrl(repositoryUrl.getValue(), null, null).validate(validationResult);
        return validationResult;
    }

    public ValidationResult isPackageConfigurationValid(PackageConfiguration packageConfiguration, RepositoryConfiguration repositoryConfiguration) {
        ValidationResult validationResult = new ValidationResult();
        validateKeys(getPackageConfiguration(), packageConfiguration, validationResult);
        Property artifactIdConfiguration = packageConfiguration.get(Constants.PACKAGE_NAME);
        if (artifactIdConfiguration == null) {
            validationResult.addError(new ValidationError(Constants.PACKAGE_NAME, "Package Name not specified"));
            return validationResult;
        }
        String packageSpec = artifactIdConfiguration.getValue();
        if (packageSpec == null) {
            validationResult.addError(new ValidationError(Constants.PACKAGE_NAME, "Package Name is null"));
            return validationResult;
        }
        if (isBlank(packageSpec.trim())) {
            validationResult.addError(new ValidationError(Constants.PACKAGE_NAME, "Package Name is empty"));
            return validationResult;
        }
        return validationResult;
    }

    private void validateKeys(Configuration configDefinedByPlugin, Configuration configDefinedByUser, ValidationResult validationResult) {
        List<String> validKeys = new ArrayList<String>();
        List<String> invalidKeys = new ArrayList<String>();
        for (Property configuration : configDefinedByPlugin.list()) {
            validKeys.add(configuration.getKey());
        }

        for (Property configuration : configDefinedByUser.list()) {
            if (!validKeys.contains(configuration.getKey())) {
                invalidKeys.add(configuration.getKey());
            }
        }
        if (!invalidKeys.isEmpty()) {
            validationResult.addError(new ValidationError("", String.format("Unsupported key(s) found : %s. Allowed key(s) are : %s", ListUtil.join(invalidKeys), ListUtil.join(validKeys))));
        }
    }

    public void validate(PackageConfiguration packageConfiguration, RepositoryConfiguration repositoryConfiguration, ValidationResult validationResult) {
        ValidationResult repositoryConfigurationValidationResult = isRepositoryConfigurationValid(repositoryConfiguration);
        validationResult.addErrors(repositoryConfigurationValidationResult.getErrors());
        ValidationResult packageConfigurationValidationResult = isPackageConfigurationValid(packageConfiguration, repositoryConfiguration);
        validationResult.addErrors(packageConfigurationValidationResult.getErrors());
    }

    public void testConnection(PackageConfiguration packageConfiguration, RepositoryConfiguration repositoryConfiguration) {
        try {
            new DebRepositoryPoller().getLatestRevision(packageConfiguration, repositoryConfiguration);
        } catch (Exception e) {
            throw new RuntimeException("Test Connection failed.", e);
        }
    }
}
