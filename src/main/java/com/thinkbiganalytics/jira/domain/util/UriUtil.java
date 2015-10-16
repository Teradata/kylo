package com.thinkbiganalytics.jira.domain.util;

import java.net.URI;

/**
 * Created by sr186054 on 10/16/15.
 */
public class UriUtil {

    public static URI path(final URI uri, final String path) {
        final String uriString = uri.toString();
        final StringBuilder sb = new StringBuilder(uriString);
        if (!uriString.endsWith("/")) {
            sb.append('/');
        }
        sb.append(path.startsWith("/") ? path.substring(1) : path);
        return URI.create(sb.toString());
    }
}