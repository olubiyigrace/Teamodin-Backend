package com.hrstack.utils;

import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Component;

@Component
public class UserAgentUtil {
    private final UserAgentAnalyzer analyzer = UserAgentAnalyzer.newBuilder().hideMatcherLoadStats().build();

    public String getUserAgent(HttpServletRequest request) {
        String userAgentString = request.getHeader("User-Agent");
        if (userAgentString == null || userAgentString.isBlank()) {
            return "Unknown Device";
        }
        UserAgent userAgent = analyzer.parse(userAgentString);
        String browser = userAgent.getValue("AgentName");
        String os = userAgent.getValue("OperatingSystemName");
        return browser + " on " + os;
    }
}