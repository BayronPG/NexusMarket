package com.nexusmarket.infrastructure.web.error;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.BusinessRuleException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.error.ValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tests of {@link GlobalExceptionHandler}.
 *
 * <p>Fase 2.4 of docs/PLAN_API_WEB.md. A standalone MockMvc with a throwing
 * controller isolates the advice from the application services.</p>
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void notFoundIsReturnedAs404() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Seller not found: 99"))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }

    @Test
    void authorizationFailureIsReturnedAs403() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Role BUYER is not allowed for this operation (RG-03)"))
                .andExpect(jsonPath("$.path").value("/test/forbidden"))
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }

    @Test
    void businessRuleViolationIsReturnedAs409() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("The email is already registered (RN-15)"))
                .andExpect(jsonPath("$.path").value("/test/conflict"))
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }

    @Test
    void invalidDomainStateIsReturnedAs409() throws Exception {
        mockMvc.perform(get("/test/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("A finalized order cannot be modified (RN-13)"))
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }

    @Test
    void applicationValidationIsReturnedAs400() throws Exception {
        mockMvc.perform(get("/test/application-validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("fullName is required and cannot be blank"))
                .andExpect(jsonPath("$.path").value("/test/application-validation"))
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }

    @Test
    void illegalArgumentIsReturnedAs400() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Stock can never be negative (RN-10)"))
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }

    @Test
    void invalidBodyIsReturnedAs400WithTheRejectedFields() throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("The request body is not valid"))
                .andExpect(jsonPath("$.path").value("/test/validated"))
                .andExpect(jsonPath("$.validationErrors.length()").value(2))
                .andExpect(jsonPath("$.validationErrors[*].field").value(hasItems("name", "id")));
    }

    @Test
    void malformedJsonIsReturnedAs400() throws Exception {
        mockMvc.perform(post("/test/validated")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("The request body or a parameter has an invalid format"))
                .andExpect(jsonPath("$.validationErrors").doesNotExist());
    }

    @Test
    void unexpectedExceptionIsReturnedAs500WithoutLeakingDetails() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error occurred. Please contact the administrator."))
                .andExpect(jsonPath("$.path").value("/test/unexpected"))
                .andExpect(jsonPath("$.validationErrors").doesNotExist())
                .andExpect(content().string(not(containsString("hunter2"))))
                .andExpect(content().string(not(containsString("RuntimeException"))));
    }

    @RestController
    @RequestMapping("/test")
    static class ThrowingController {

        @GetMapping("/not-found")
        void notFound() {
            throw new NotFoundException("Seller not found: 99");
        }

        @GetMapping("/forbidden")
        void forbidden() {
            throw new AuthorizationException("Role BUYER is not allowed for this operation (RG-03)");
        }

        @GetMapping("/conflict")
        void conflict() {
            throw new BusinessRuleException("The email is already registered (RN-15)");
        }

        @GetMapping("/illegal-state")
        void illegalState() {
            throw new IllegalStateException("A finalized order cannot be modified (RN-13)");
        }

        @GetMapping("/application-validation")
        void applicationValidation() {
            throw new ValidationException("fullName is required and cannot be blank");
        }

        @GetMapping("/illegal-argument")
        void illegalArgument() {
            throw new IllegalArgumentException("Stock can never be negative (RN-10)");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new RuntimeException("database password is hunter2");
        }

        @PostMapping("/validated")
        void validated(@Valid @RequestBody SampleRequest body) {
        }
    }

    record SampleRequest(@NotBlank String name, @NotNull Long id) {
    }
}
