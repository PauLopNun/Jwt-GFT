package com.exampleinyection.jwtgft.auth;

import com.exampleinyection.jwtgft.security.JwtService;
import com.exampleinyection.jwtgft.user.Role;
import com.exampleinyection.jwtgft.user.User;
import com.exampleinyection.jwtgft.user.UserRepository;
import java.util.Optional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		AuthenticationManager authenticationManager,
		JwtService jwtService,
		RefreshTokenService refreshTokenService,
		RefreshTokenRepository refreshTokenRepository
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new IllegalArgumentException("Email already registered: " + request.email());
		}

		User user = new User();
		user.setEmail(request.email());
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setRole(request.role() != null ? request.role() : Role.ROLE_USER);

		User saved = userRepository.save(user);
		String token = jwtService.generateToken(saved);
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(saved);
		return new AuthResponse(token, refreshToken.getToken(), "Bearer", jwtService.getExpirationSeconds());
	}

	public AuthResponse login(LoginRequest request) {
		Authentication authRequest = new UsernamePasswordAuthenticationToken(
			request.email(),
			request.password()
		);

		Authentication authentication = authenticationManager.authenticate(authRequest);
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof UserDetails userDetails)) {
			throw new BadCredentialsException("Invalid login");
		}

		String token = jwtService.generateToken(userDetails);
		User user = userRepository.findByEmail(userDetails.getUsername())
			.orElseThrow(() -> new BadCredentialsException("Invalid login"));
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
		return new AuthResponse(token, refreshToken.getToken(), "Bearer", jwtService.getExpirationSeconds());
	}

	public AuthResponse refresh(RefreshRequest request) {
		return refreshTokenRepository.findByToken(request.refreshToken())
			.map(refreshTokenService::verifyExpiration)
			.map(RefreshToken::getUser)
			.map(user -> {
				String accessToken = jwtService.generateToken(user);
				RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
				return new AuthResponse(accessToken, newRefreshToken.getToken(), "Bearer", jwtService.getExpirationSeconds());
			})
			.orElseThrow(() -> new TokenRefreshException("Refresh token not found."));
	}

	public void logout(String email) {
		Optional<User> user = userRepository.findByEmail(email);
		user.ifPresent(refreshTokenService::revokeByUser);
	}
}

