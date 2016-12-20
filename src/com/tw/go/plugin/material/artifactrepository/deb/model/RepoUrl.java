package com.tw.go.plugin.material.artifactrepository.deb.model;

import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.material.artifactrepository.deb.connection.ConnectionChecker;
import com.tw.go.plugin.material.artifactrepository.deb.connection.FileBasedConnectionChecker;
import com.tw.go.plugin.material.artifactrepository.deb.connection.HttpConnectionChecker;
import com.tw.go.plugin.material.artifactrepository.deb.constants.Constants;
import com.tw.go.plugin.material.artifactrepository.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;

public class RepoUrl {
    private final String url;
    private Credentials credentials;
    private static HashMap<String, ConnectionChecker> map = new HashMap<String, ConnectionChecker>();
    private static FileBasedConnectionChecker fileBasedConnectionChecker = new FileBasedConnectionChecker();
    private static HttpConnectionChecker httpConnectionChecker = new HttpConnectionChecker();

    static {
        map.put("file", fileBasedConnectionChecker);
        map.put("http", httpConnectionChecker);
        map.put("https", httpConnectionChecker);
    }

    public RepoUrl(String url, String user, String password) {
        this.url = url;
        this.credentials = new Credentials(user, password);
    }

    public void validate(ValidationResult validationResult) {
        try {
            if (StringUtil.isBlank(url)) {
                validationResult.addError(new ValidationError(Constants.REPO_URL, "Repository url is empty"));
                return;
            }
            URL validatedUrl = new URL(this.url);
            if (!map.containsKey(validatedUrl.getProtocol())) {
                validationResult.addError(new ValidationError(Constants.REPO_URL, "Invalid URL: Only 'file' and 'http' protocols are supported."));
            }

            if (StringUtil.isNotBlank(validatedUrl.getUserInfo())) {
                validationResult.addError(new ValidationError(Constants.REPO_URL, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys."));
            }
            if (credentials.isPresent()) {
                if (validatedUrl.getProtocol().equals("file")) {
                    validationResult.addError(new ValidationError(Constants.REPO_URL, "File protocol does not support username and/or password."));
                } else {
                    credentials.validate(validationResult);
                }
            }
        } catch (MalformedURLException e) {
            validationResult.addError(new ValidationError(Constants.REPO_URL, "Invalid URL : " + url));
        }
    }

    ConnectionChecker getChecker() {
        try {
            return map.get(new URL(url).getProtocol());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL: " + e);
        }
    }

    public String getUrlWithBasicAuth() {
        String localUrl = this.url;
        try {
            new URL(localUrl);
            if (credentials.isComplete()) {
                String[] split = localUrl.split("//");
                if (split.length != 2) throw new RuntimeException(String.format("Invalid uri format %s", this.url));
                localUrl = split[0] + "//" + credentials.getUserInfo() + "@" + split[1];
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return localUrl;
    }

    public void checkConnection() {
        getChecker().checkConnection(getRepoMetadataUrl(), credentials);
    }

    public String getRepoMetadataUrl() {
        Pattern pattern = Pattern.compile("(.*?)(/+)$");
        String urlStrippedOfTrailingSlashes = pattern.matcher(url).replaceAll("$1");
        return urlStrippedOfTrailingSlashes + "/Packages.gz";
    }

    public String getURL() {
        return url;
    }

    public String forDisplay() {
        return url;
    }

    public String getPackageLocation(String filePath) {
        String packageLocation = null;
        if (url.endsWith("/"))
            packageLocation = url.substring(0, url.length() - 1);
        else
            packageLocation = url;
        String[] parts = filePath.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (!packageLocation.contains(parts[i])) {
                packageLocation = merge(packageLocation, parts, i);
                break;
            }
        }
        return packageLocation;
    }

    private String merge(String packageLocation, String[] parts, int index) {
        for (int i = index; i < parts.length; i++) {
            packageLocation += "/" + parts[i];
        }
        return packageLocation;
    }
}
