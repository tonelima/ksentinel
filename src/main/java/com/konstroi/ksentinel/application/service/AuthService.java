package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.application.port.out.CredentialEncryptPort;
import com.konstroi.ksentinel.domain.model.AppUser;
import com.konstroi.ksentinel.domain.repository.AppUserRepository;
import com.konstroi.ksentinel.exception.DuplicateUserException;
import com.konstroi.ksentinel.exception.InvalidCredentialsException;
import com.konstroi.ksentinel.infrastructure.security.CurrentUserService;
import com.konstroi.ksentinel.infrastructure.security.JwtService;
import com.konstroi.ksentinel.interfaces.rest.dto.LoginRequest;
import com.konstroi.ksentinel.interfaces.rest.dto.LoginResponse;
import com.konstroi.ksentinel.interfaces.rest.dto.RegisterRequest;
import com.konstroi.ksentinel.interfaces.rest.dto.SmtpSettingsRequest;
import com.konstroi.ksentinel.interfaces.rest.dto.SmtpSettingsResponse;
import com.konstroi.ksentinel.interfaces.rest.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;
    private final CredentialEncryptPort encryptPort;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateUserException(email);
        }

        AppUser user = AppUser.builder()
                .name(request.getName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        applySmtpSettings(user, request.getSmtpSettings());

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse(jwtService.generateToken(user), toResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        Long userId = currentUserService.currentUserId();
        return userRepository.findById(userId)
                .map(this::toResponse)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                new SmtpSettingsResponse(
                        user.getSmtpHost(),
                        user.getSmtpPort(),
                        user.getSmtpUsername(),
                        user.getSmtpFromEmail(),
                        user.getSmtpAuth(),
                        user.getSmtpStarttls(),
                        isSmtpConfigured(user)
                )
        );
    }

    private void applySmtpSettings(AppUser user, SmtpSettingsRequest settings) {
        if (settings == null || settings.getHost() == null || settings.getHost().isBlank()) {
            return;
        }
        user.setSmtpHost(settings.getHost().trim());
        user.setSmtpPort(settings.getPort() != null ? settings.getPort() : 587);
        user.setSmtpUsername(trimToNull(settings.getUsername()));
        user.setSmtpPassword(encryptPort.encrypt(trimToNull(settings.getPassword())));
        user.setSmtpFromEmail(trimToNull(settings.getFromEmail()));
        user.setSmtpAuth(settings.getAuth() == null || settings.getAuth());
        user.setSmtpStarttls(settings.getStarttls() == null || settings.getStarttls());
    }

    private boolean isSmtpConfigured(AppUser user) {
        return user.getSmtpHost() != null
                && !user.getSmtpHost().isBlank()
                && user.getSmtpPort() != null;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
