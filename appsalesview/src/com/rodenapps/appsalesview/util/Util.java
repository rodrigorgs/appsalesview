package com.rodenapps.appsalesview.util;

import java.util.Collection;
import java.util.Iterator;

public class Util {
	// http://snippets.dzone.com/posts/show/91
    public static String join(Collection s, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }

}