package com.sebastientr.workflow.configuration;

import com.sebastientr.workflow.dto.constant.WorkflowEngineConstant;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

@Configuration
@EnableAsync
@EnableJpaAuditing
public class WorkflowEngineConfiguration {
    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }

    static class AuditorAwareImpl implements AuditorAware<String> {
        @Override
        public @NonNull Optional<String> getCurrentAuditor() {
            var auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.getPrincipal() != null && auth.getPrincipal() instanceof User user) {
                return Optional.of(user.getUsername());
            }

            return Optional.of(WorkflowEngineConstant.SYSTEM_USER);
        }
    }
}
