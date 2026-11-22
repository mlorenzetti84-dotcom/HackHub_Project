package com.hackhub.config;

import com.hackhub.dto.UserDto;
import com.hackhub.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionAuthenticationInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER_ID = "authenticatedUserId";
    private final AuthenticationService authenticationService;

    public SessionAuthenticationInterceptor(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isPublicRequest(request)) {
            return true;
        }
        UserDto user = authenticationService.currentUser(request.getHeader("X-Access-Token"));
        request.setAttribute(AUTHENTICATED_USER_ID, user.id());
        return true;
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) {
            return true;
        }
        return HttpMethod.GET.matches(request.getMethod())
                && path.matches("/api/hackathons(?:/\\d+)?");
    }
}
