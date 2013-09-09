package com.tw.go.plugin.material.artifactrepository.deb;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProvider;
import com.tw.go.plugin.material.artifactrepository.deb.config.DebRepositoryConfiguration;
import com.tw.go.plugin.material.artifactrepository.deb.poller.DebRepositoryPoller;

@Extension
public class DebArtifactRepositoryMaterial implements PackageMaterialProvider {

    public DebRepositoryConfiguration getConfig() {
        return new DebRepositoryConfiguration();
    }

    public DebRepositoryPoller getPoller() {
        return new DebRepositoryPoller();
    }
}
