package com.tw.go.plugin.material.artifactrepository.deb.connection;

import com.tw.go.plugin.material.artifactrepository.deb.model.Credentials;

import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.*;

import java.io.IOException;

public class HttpConnectionChecker implements ConnectionChecker {

    public void checkConnection(String url, Credentials credentials) {
        try (CloseableHttpClient client = getHttpClient(credentials)) {
            HttpGet method = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(method)) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new RuntimeException(response.getStatusLine().toString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    CloseableHttpClient getHttpClient(Credentials credentials) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        if (credentials.isComplete()) {
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(credentials.getUser(), credentials.getPassword()));
        }

        RequestConfig requestConfig = RequestConfig.custom().
                setConnectTimeout(getSystemProperty("deb.repo.connection.timeout", 10 * 1000)).
                setSocketTimeout(getSystemProperty("deb.repo.socket.timeout", 5 * 60 * 1000)).
                setAuthenticationEnabled(true).
                setMaxRedirects(10).
                build();

        return HttpClients.custom().
                setRedirectStrategy(new DefaultRedirectStrategy()).
                setDefaultCredentialsProvider(credentialsProvider).
                setDefaultRequestConfig(requestConfig).
                setTargetAuthenticationStrategy(new TargetAuthenticationStrategy()).
                build();
    }

    private int getSystemProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(System.getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
