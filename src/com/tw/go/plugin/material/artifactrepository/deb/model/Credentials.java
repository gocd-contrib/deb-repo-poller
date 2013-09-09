package com.tw.go.plugin.material.artifactrepository.deb.model;

import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.material.artifactrepository.deb.constants.Constants;
import com.tw.go.plugin.material.artifactrepository.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Credentials {

    private final String user;
    private final String password;

    public Credentials(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }

    public String getUserInfo() throws UnsupportedEncodingException {
        return String.format("%s:%s", user, URLEncoder.encode(password, "UTF-8"));
    }

    public void validate(ValidationResult validationResult) {
        if (StringUtil.isBlank(user) && StringUtil.isNotBlank(password))
            validationResult.addError(new ValidationError(Constants.USERNAME, "Both Username and password are required."));
        if (StringUtil.isNotBlank(user) && StringUtil.isBlank(password))
            validationResult.addError(new ValidationError(Constants.PASSWORD, "Both Username and password are required."));
    }

    public boolean isComplete() {
        return StringUtil.isNotBlank(user) && StringUtil.isNotBlank(password);
    }

    public boolean isPresent() {
        return StringUtil.isNotBlank(user) || StringUtil.isNotBlank(password);
    }
}
