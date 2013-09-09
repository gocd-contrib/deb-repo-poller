package com.tw.go.plugin.material.artifactrepository.deb.connection;

import com.tw.go.plugin.material.artifactrepository.deb.model.Credentials;

public interface ConnectionChecker {
    void checkConnection(String path, Credentials credentials);
}
