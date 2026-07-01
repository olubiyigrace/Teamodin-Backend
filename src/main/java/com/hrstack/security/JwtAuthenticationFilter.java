package com.hrstack.security;

import com.hrstack.auth.TenantContext;
import com.hrstack.exceptions.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
//    private final UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String jwt = getJwtFromRequest(request);

//            if (StringUtils.hasText(jwt) && logoutTokenRepository.existsById(jwt)) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.getWriter().write("Session ended");
//                return;
//            }
            if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {

                final String userId = jwtService.getUserIdFromToken(jwt);
                final String workspaceUrl = jwtService.getWorkspaceUrlFromToken(jwt);
                final String role = jwtService.getRoleFromToken(jwt);

                log.info("Role: {}", role);

                if (workspaceUrl != null) {
                    TenantContext.setCurrentTenant(workspaceUrl);
                }
                if (role == null || role.isBlank()) {
                    log.warn("Missing role in JWT for userId={}", userId);
                    throw new UnauthorizedException("Missing role in JWT");
                }
                final SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                final UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null,
                                Collections.singletonList(authority)
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

//                Optional<UserSession> session = userSessionRepository.findByAccessToken(jwt);
//                if (session.isPresent() && session.get().isRevoked()) {
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.getWriter().write("Session has been revoked");
//                    return;
//                }

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("User authenticated for user ID:{}, institution: {}, role: {}", userId, workspaceUrl, role);
            }
        } catch (final Exception e) {
            log.error("Error authenticating user", e);
        }
        filterChain.doFilter(request, response);
        TenantContext.clear();
    }

    private String getJwtFromRequest(final HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
