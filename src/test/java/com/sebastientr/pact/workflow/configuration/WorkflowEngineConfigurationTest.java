package com.sebastientr.workflow.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowEngineConfigurationTest {
    @Test
    void testAuditorAwareImpl() {
        // Mocking SecurityContextHolder
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // Mocking authentication and setting it in SecurityContextHolder
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new User("testUser", "password", Collections.emptyList()));
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Creating an instance of AuditorAwareImpl
        WorkflowEngineConfiguration.AuditorAwareImpl auditorAware = new WorkflowEngineConfiguration.AuditorAwareImpl();

        // Testing getCurrentAuditor() method
        Optional<String> auditor = auditorAware.getCurrentAuditor();
        Assertions.assertEquals("testUser", auditor.orElse(""));

        // Clean up after the test
        SecurityContextHolder.clearContext();
    }
}