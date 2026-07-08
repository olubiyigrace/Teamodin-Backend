package com.hrstack.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class IpAddressUtil {
    public String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp) && !"unknown".equalsIgnoreCase(realIp)) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
