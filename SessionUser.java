package com.customer.security;

import com.customer.exception.ApiException;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;

public final class SessionUser {
    public static final String KEY = "customerId";

    private SessionUser() {}

    @NonNull
    public static Long id(HttpSession session) {
        if (session == null) {
            throw new ApiException(401, "Please sign in.");
        }
        Object id = session.getAttribute(KEY);
        if (id instanceof Long longId) {
            return longId;
        }
        throw new ApiException(401, "Please sign in.");
    }
}
