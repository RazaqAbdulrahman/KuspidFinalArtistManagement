package com.kuspid.auth.service;

import com.kuspid.auth.dto.AuthenticationRequest;
import com.kuspid.auth.dto.AuthenticationResponse;
import com.kuspid.auth.dto.RegisterRequest;
import com.kuspid.auth.model.User;
import com.kuspid.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
        private final UserRepository repository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;

        public AuthenticationResponse register(RegisterRequest request) {
                if (repository.findByEmail(request.getEmail()).isPresent()) {
                        throw new RuntimeException("User with email " + request.getEmail() + " already exists");
                }

                var user = User.builder()
                                .fullName(request.getFullName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(User.Role.USER)
                                .build();
                repository.save(user);
                var jwtToken = jwtService.generateToken(user);
                var refreshToken = jwtService.generateRefreshToken(user);
                return AuthenticationResponse.builder()
                                .accessToken(jwtToken)
                                .refreshToken(refreshToken)
                                .build();
        }

        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmail(),
                                                        request.getPassword()));
                } catch (Exception e) {
                        throw new RuntimeException("Authentication failed: Invalid email or password");
                }

                var user = repository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

                var jwtToken = jwtService.generateToken(user);
                var refreshToken = jwtService.generateRefreshToken(user);
                return AuthenticationResponse.builder()
                                .accessToken(jwtToken)
                                .refreshToken(refreshToken)
                                .build();
        }
}
