package com.tw.go.plugin.material.artifactrepository.deb;


import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import com.tw.go.plugin.material.artifactrepository.deb.config.DebRepositoryConfiguration;
import com.tw.go.plugin.material.artifactrepository.deb.poller.DebRepositoryPoller;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class DebArtifactRepositoryMaterialTest {
    @Test
    public void shouldGetYumRepositoryConfig() {
        DebArtifactRepositoryMaterial repositoryMaterial = new DebArtifactRepositoryMaterial();
        PackageMaterialConfiguration repositoryConfiguration = repositoryMaterial.getConfig();
        assertThat(repositoryConfiguration, is(notNullValue()));
        assertThat(repositoryConfiguration, instanceOf(DebRepositoryConfiguration.class));
    }

    @Test
    public void shouldGetYumRepositoryPoller() {
        DebArtifactRepositoryMaterial repositoryMaterial = new DebArtifactRepositoryMaterial();
        PackageMaterialPoller poller = repositoryMaterial.getPoller();
        assertThat(poller, is(notNullValue()));
        assertThat(poller, instanceOf(DebRepositoryPoller.class));
    }
}
