package com.tw.go.plugin.material.artifactrepository.deb.connection;

import com.tw.go.plugin.material.artifactrepository.deb.model.Credentials;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FileBasedConnectionChecker implements ConnectionChecker {
    public void checkConnection(String givenUrl, Credentials credentials) {
        if (credentials.isComplete()) {
            throw new RuntimeException("File protocol does not support username and/or password.");
        }
        try {
            URL url = new URL(givenUrl);
            if (!new File(url.getPath()).exists()) {
                throw new RuntimeException("Invalid file path.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
