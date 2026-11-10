package com.nexusmarket.infrastructure.web.controller;

import com.nexusmarket.domain.user.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class SecurityTestSupport {

    private SecurityTestSupport() {
    }

    public static RequestPostProcessor jwt(String subject, Role role) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.subject(subject).claim("role", role.name()))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
