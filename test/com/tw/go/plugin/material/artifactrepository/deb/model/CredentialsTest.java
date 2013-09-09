package com.tw.go.plugin.material.artifactrepository.deb.model;

import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.tw.go.plugin.material.artifactrepository.deb.constants.Constants;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

public class CredentialsTest {
    @Test
    public void shouldGetUserInfo() throws Exception {
        Credentials credentials = new Credentials("user", "password");
        assertThat(credentials.getUserInfo(), is("user:password"));
    }

    @Test
    public void shouldGetUserInfoWithEscapedPassword() throws Exception {
        Credentials credentials = new Credentials("user", "!password@:");
        assertThat(credentials.getUserInfo(), is("user:%21password%40%3A"));
    }

    @Test
    public void shouldFailValidationIfOnlyPasswordProvided() throws Exception {
        ValidationResult validationResult = new ValidationResult();
        new Credentials(null, "password").validate(validationResult);
        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getErrors(), hasItem(new ValidationError(Constants.USERNAME, "Both Username and password are required.")));

        validationResult = new ValidationResult();
        new Credentials("user", "").validate(validationResult);
        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getErrors(), hasItem(new ValidationError(Constants.PASSWORD, "Both Username and password are required.")));
    }
}
