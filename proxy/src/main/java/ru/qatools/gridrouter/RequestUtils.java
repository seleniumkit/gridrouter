package ru.qatools.gridrouter;

import javax.servlet.http.HttpServletRequest;

public final class RequestUtils {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    public static String getRemoteHost(HttpServletRequest request) {
        String remoteHost = request.getHeader(X_FORWARDED_FOR);
        if (remoteHost == null) {
            return request.getRemoteHost();
        }
        return remoteHost;
    }
    
    private RequestUtils(){}
}
