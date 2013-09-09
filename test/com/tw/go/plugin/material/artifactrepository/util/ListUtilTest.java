package com.tw.go.plugin.material.artifactrepository.util;

import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ListUtilTest {
    @Test
    public void shouldJoinAListUsingComma(){
        ArrayList<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        assertThat(ListUtil.join(list), is("a, b, c"));
    }

    @Test
    public void shouldJoinAListUsingSpecifiedSeparator(){
        ArrayList<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        assertThat(ListUtil.join(list, " "), is("a b c"));
    }
}
