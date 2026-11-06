package com.securewallet.backend.auth;

import com.securewallet.backend.auth.dto.AuthResponse;
import com.securewallet.backend.auth.dto.LoginRequest;
import com.securewallet.backend.auth.dto.RegisterRequest;
import com.securewallet.backend.auth.dto.UserResponse;
import com.securewallet.backend.config.JwtService;
import com.securewallet.backend.n8n.LoginGuardClient;
import com.securewallet.backend.user.Role;
import com.securewallet.backend.user.User;
import com.securewallet.backend.user.UserRepository;
import com.securewallet.backend.wallet.Wallet;
import com.securewallet.backend.wallet.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class AuthService {

    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginGuardClient loginGuardClient;

    public AuthService(UserRepository userRepository,
                       WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       LoginGuardClient loginGuardClient) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginGuardClient = loginGuardClient;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        User saved = userRepository.save(user);
        walletRepository.save(new Wallet(saved, STARTING_BALANCE));
        return toResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        // null means "no such email" or "wrong password" (same error for both: prevents user enumeration)
        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElse(null);

        if (user == null) {
            loginGuardClient.report(email, false);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        loginGuardClient.report(email, true);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, jwtService.getExpirationMinutes(), toResponse(user));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}