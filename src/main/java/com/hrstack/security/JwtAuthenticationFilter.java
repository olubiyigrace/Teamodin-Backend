package com.hrstack.security;

import com.hrstack.auth.TenantContext;
import com.hrstack.exceptions.InvalidRequestException;
import com.hrstack.exceptions.UnauthorizedException;
import com.hrstack.services.RedisSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final RedisSessionService redisSessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {

            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                jwtService.validateToken(jwt);

                if (!jwtService.isAccessToken(jwt)) {
                    throw new UnauthorizedException("Invalid access token");
                }

                String sessionId = jwtService.getSessionId(jwt);
                if (!redisSessionService.isSessionActive(sessionId)) {
                    throw new UnauthorizedException("Session expired");
                }

                String userId = jwtService.getUserIdFromToken(jwt);
                String workspaceUrl = jwtService.getWorkspaceUrlFromToken(jwt);
                String role = jwtService.getRoleFromToken(jwt);

                if (workspaceUrl != null) {
                    TenantContext.setCurrentTenant(workspaceUrl);
                }
                if (!StringUtils.hasText(role)) {
                    throw new UnauthorizedException("Role missing from token");
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (UnauthorizedException | InvalidRequestException ex) {
            SecurityContextHolder.clearContext();
            TenantContext.clear();

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    ex.getMessage()
            );
        } finally {
            TenantContext.clear();
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}