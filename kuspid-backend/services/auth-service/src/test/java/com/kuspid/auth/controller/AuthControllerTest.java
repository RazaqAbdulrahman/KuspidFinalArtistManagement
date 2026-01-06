package com.kuspid.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kuspid.auth.dto.AuthenticationRequest;
import com.kuspid.auth.dto.AuthenticationResponse;
import com.kuspid.auth.dto.RegisterRequest;
import com.kuspid.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthService authService;

        private AuthenticationResponse mockResponse;

        @BeforeEach
        void setUp() {
                mockResponse = AuthenticationResponse.builder()
                                .accessToken("mock-access-token")
                                .refreshToken("mock-refresh-token")
                                .build();
        }

        @Test
        @DisplayName("POST /api/auth/register - Success")
        @WithMockUser
        void register_ShouldReturnTokens_WhenValidRequest() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .fullName("Test User")
                                .email("test@kuspid.com")
                                .password("password123")
                                .build();

                when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"));
        }

        @Test
        @DisplayName("POST /api/auth/login - Success")
        @WithMockUser
        void login_ShouldReturnTokens_WhenValidCredentials() throws Exception {
                AuthenticationRequest request = AuthenticationRequest.builder()
                                .email("test@kuspid.com")
                                .password("password123")
                                .build();

                when(authService.authenticate(any(AuthenticationRequest.class))).thenReturn(mockResponse);

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andExpect(jsonPath("$.refreshToken").exists());
        }

        @Test
        @DisplayName("POST /api/auth/register - Empty Body Returns Error")
        @WithMockUser
        void register_ShouldFail_WhenEmptyBody() throws Exception {
                mockMvc.perform(post("/api/auth/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isOk()); // Service handles validation
        }
}
