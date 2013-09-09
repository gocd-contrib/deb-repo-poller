package com.tw.go.plugin.material.artifactrepository.util;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StringUtilTest {
    @Test
    public void shouldCheckAStringForBlank() {
        assertThat(StringUtil.isBlank(""), is(true));
        assertThat(StringUtil.isBlank("   "), is(true));
        assertThat(StringUtil.isBlank(null), is(true));
        assertThat(StringUtil.isBlank(" a "), is(false));
    }

    @Test
    public void shouldCheckIfAStringIsNotBlank() throws Exception {
        assertThat(StringUtil.isNotBlank(""), is(false));
        assertThat(StringUtil.isNotBlank("   "), is(false));
        assertThat(StringUtil.isNotBlank(null), is(false));
        assertThat(StringUtil.isNotBlank(" a "), is(true));
    }
}
