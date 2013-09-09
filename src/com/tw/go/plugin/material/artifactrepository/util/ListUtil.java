package com.tw.go.plugin.material.artifactrepository.util;

import java.util.Collection;
import java.util.Iterator;

public class ListUtil {
    public static String join(Collection c) {
        return join(c, ", ");
    }

    public static String join(Collection c, String join) {
        StringBuffer sb = new StringBuffer();
        for (Iterator<Object> iterator = c.iterator(); iterator.hasNext(); ) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(join);
            }
        }
        return sb.toString();
    }
}
