package com.customer.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/** Blocks every /api call that has no logged-in session. */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull Object handler) throws Exception {
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute(SessionUser.KEY) != null) return true;
        res.setStatus(401);
        res.setContentType("application/json");
        res.getWriter().write("{\"message\":\"Please sign in.\"}");
        return false;
    }
}
