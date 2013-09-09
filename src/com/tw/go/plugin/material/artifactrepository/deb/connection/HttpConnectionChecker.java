package com.tw.go.plugin.material.artifactrepository.deb.connection;

import com.tw.go.plugin.material.artifactrepository.deb.model.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;

import java.io.IOException;

public class HttpConnectionChecker implements ConnectionChecker {

    public void checkConnection(String url, Credentials credentials) {
        HttpClient client = new HttpClient();
        client.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, getSystemProperty("deb.repo.connection.timeout", 10 * 1000));
        client.getParams().setSoTimeout(getSystemProperty("deb.repo.socket.timeout", 5 * 60 * 1000));
        if (credentials.isComplete()) {
            org.apache.commons.httpclient.Credentials usernamePasswordCredentials = new UsernamePasswordCredentials(credentials.getUser(), credentials.getPassword());
            client.getState().setCredentials(AuthScope.ANY, usernamePasswordCredentials);
        }
        GetMethod method = new GetMethod(url);
        method.setFollowRedirects(false);
        try {
            int returnCode = client.executeMethod(method);
            if (returnCode != HttpStatus.SC_OK) {
                throw new RuntimeException(method.getStatusLine().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getSystemProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(System.getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
