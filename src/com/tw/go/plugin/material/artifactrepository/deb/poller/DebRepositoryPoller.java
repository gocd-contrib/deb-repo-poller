package com.tw.go.plugin.material.artifactrepository.deb.poller;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.*;
import com.thoughtworks.go.plugin.api.response.Result;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.material.artifactrepository.deb.constants.Constants;
import com.tw.go.plugin.material.artifactrepository.deb.model.RepoUrl;
import com.tw.go.plugin.material.artifactrepository.deb.config.DebRepositoryConfiguration;
import com.tw.pkg.deb.repo.DebianPackage;
import com.tw.pkg.deb.repo.DebianRepoQuery;

import java.util.Date;
import java.util.List;

public class DebRepositoryPoller implements PackageMaterialPoller {

    private static Logger LOGGER = Logger.getLoggerFor(DebRepositoryPoller.class);

    public PackageRevision getLatestRevision(PackageConfiguration packagePluginConfiguration, RepositoryConfiguration repositoryPluginConfiguration) {
        validateData(repositoryPluginConfiguration, packagePluginConfiguration);
        Property packageName = packagePluginConfiguration.get(Constants.PACKAGE_NAME);
        Property versionSpec = packagePluginConfiguration.get(Constants.VERSION_SPEC);
        Property architecture = packagePluginConfiguration.get(Constants.ARCHITECTURE);
        RepoUrl url = repoUrl(repositoryPluginConfiguration);
        url.checkConnection();
        PackageRevision packageRevision = null;
        try {
            String versionSpecValue = null;
            if (versionSpec != null)
                versionSpecValue = versionSpec.getValue();
            String architectureValue = null;
            if (architecture != null)
                architectureValue = architecture.getValue();
            DebianRepoQuery debianRepoQuery = new DebianRepoQuery(url.getURL());
            debianRepoQuery.updateCacheIfRequired();
            List<DebianPackage> debianPackages = debianRepoQuery.getDebianPackagesFor(packageName.getValue(), versionSpecValue, architectureValue);
            if (debianPackages == null && debianPackages.isEmpty()) {
                throw new RuntimeException("did not find any package matching requirements.");
            }

            DebianPackage debianPackage = debianPackages.get(0);
            String revision = debianPackage.getName() + "." + debianPackage.getVersion() + "." + debianPackage.getArchitecture();
            packageRevision = new PackageRevision(revision, new Date(), null);
            packageRevision.addData("FILE_DOWNLOAD", url.getURL() + debianPackage.getFilename());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("", e);
        }
        return packageRevision;
    }

    public PackageRevision latestModificationSince(PackageConfiguration packagePluginConfiguration, RepositoryConfiguration repositoryPluginConfiguration, PackageRevision previouslyKnownRevision) {
        PackageRevision latestRevision = getLatestRevision(packagePluginConfiguration, repositoryPluginConfiguration);

        if (latestRevision.getRevision() != previouslyKnownRevision.getRevision())
            return latestRevision;
        return null;
    }

    public Result checkConnectionToRepository(RepositoryConfiguration repositoryPackageConfiguration) {
        Result repositoryValidationResult = repositoryValidation(repositoryPackageConfiguration);
        if (!repositoryValidationResult.isSuccessful()) {
            return repositoryValidationResult;
        }
        RepoUrl url = repoUrl(repositoryPackageConfiguration);
        try {
            url.checkConnection();
            return new Result().withSuccessMessages(String.format("Successfully accessed repository metadata at %s", url.getRepoMetadataUrl()));
        } catch (Exception e) {
            LOGGER.warn(String.format("[Yum Repo Check Connection] Check connection for %s failed with exception - %s", url.getRepoMetadataUrl(), e));
            return new Result().withErrorMessages(String.format("Could not access file - %s. %s", url.getRepoMetadataUrl(), e.getMessage()));
        }
    }

    public Result checkConnectionToPackage(PackageConfiguration packageConfigurations, RepositoryConfiguration repositoryPackageConfigurations) {
        Result checkConnectionResult = checkConnectionToRepository(repositoryPackageConfigurations);
        if (!checkConnectionResult.isSuccessful()) {
            return checkConnectionResult;
        }
        try {
            Result packageConfigurationValidationResult = packageValidation(packageConfigurations, repositoryPackageConfigurations);
            if (!packageConfigurationValidationResult.isSuccessful()) {
                return packageConfigurationValidationResult;
            }
            PackageRevision latestRevision = getLatestRevision(packageConfigurations, repositoryPackageConfigurations);
            return new Result().withSuccessMessages(String.format("Found package '%s'.", latestRevision.getRevision()));
        } catch (Exception e) {
            String message = String.format("Could not find any package that matched requirements.");
            LOGGER.warn(message);
            return new Result().withErrorMessages(message);
        }
    }

    private Result repositoryValidation(RepositoryConfiguration repositoryPackageConfigurations) {
        ValidationResult validationResult = new DebRepositoryConfiguration().isRepositoryConfigurationValid(repositoryPackageConfigurations);
        if (!validationResult.isSuccessful()) {
            return new Result().withErrorMessages(validationResult.getMessages());
        }
        return new Result();
    }

    private Result packageValidation(PackageConfiguration packageConfigurations, RepositoryConfiguration repositoryPackageConfiguration) {
        ValidationResult validationResult = new DebRepositoryConfiguration().isPackageConfigurationValid(packageConfigurations, repositoryPackageConfiguration);
        if (!validationResult.isSuccessful()) {
            return new Result().withErrorMessages(validationResult.getMessages());
        }
        return new Result();
    }

    private void validateData(RepositoryConfiguration repositoryConfigurations, PackageConfiguration packageConfigurations) {
        ValidationResult validationResult = new ValidationResult();
        new DebRepositoryConfiguration().validate(packageConfigurations, repositoryConfigurations, validationResult);
        if (!validationResult.isSuccessful()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ValidationError validationError : validationResult.getErrors()) {
                stringBuilder.append(validationError.getMessage()).append("; ");
            }
            String errorString = stringBuilder.toString();
            String message = errorString.substring(0, errorString.length() - 2);
            LOGGER.warn(String.format("Data validation failed: %s", message));
            throw new RuntimeException(message);
        }
    }


    private RepoUrl repoUrl(RepositoryConfiguration repositoryPluginConfigurations) {
        Property repoUrl = repositoryPluginConfigurations.get(Constants.REPO_URL);
        //Property username = repositoryPluginConfigurations.get(Constants.USERNAME);
        //Property password = repositoryPluginConfigurations.get(Constants.PASSWORD);
        //String usernameValue = username == null ? null : username.getValue();
        //String passwordValue = password == null ? null : password.getValue();
        return new RepoUrl(repoUrl.getValue(), null, null);
    }
}
