package com.example.blog.service.implement;

import com.example.blog.config.JwtKeyConfig;
import com.example.blog.domain.Profile;
import com.example.blog.domain.Role;
import com.example.blog.domain.User;
import com.example.blog.dto.request.AuthenticationRequest;
import com.example.blog.dto.request.GoogleLoginRequest;
import com.example.blog.dto.request.RefreshTokenRequest;
import com.example.blog.dto.response.AuthenticationResponse;
import com.example.blog.dto.response.GoogleUserInfo;
import com.example.blog.enums.ErrorCode;
import com.example.blog.enums.TypeToken;
import com.example.blog.enums.UserStatus;
import com.example.blog.exception.AppException;
import com.example.blog.repository.RoleRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.AuthenticationService;
import com.example.blog.service.RedisService;
import com.example.blog.service.TokenService;
import com.example.blog.utils.PreFixUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j(topic = "AUTHENTICATION-SERVICE")
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RedisService redisService;
    private final RoleRepository roleRepository;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final JwtKeyConfig jwtKeyConfig;

    private static final String VALID_STATUS = "1";



    @Override
    public void logout(String authHeader) {
        String token = authHeader.substring(7);
        Jwt jwt = tokenService.validateToken(token);
        saveTokenBlackListOnRedis(jwt);
        log.info("logout success");
    }


    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        Jwt jwt = tokenService.validateToken(refreshTokenRequest.refreshToken());
        long userId = Long.parseLong(jwt.getClaimAsString(PreFixUtils.USER_ID));
        String tokenId = jwt.getId();
        validateRefreshToken(userId, tokenId);

        User user = getUserNoProfileByUsername(jwt.getSubject());
        return generatePairToken(user);
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        User user = getUserNoProfileByUsername(authenticationRequest.username());

        if(!passwordEncoder.matches(authenticationRequest.password(), user.getPassword())) {
            throw new UsernameNotFoundException("Username or password is incorrect");
        }

        return generatePairToken(user);
    }

    @Override
    @Transactional
    public AuthenticationResponse authenticateWithGoogle(GoogleLoginRequest request) {
        GoogleUserInfo googleInfo = googleTokenVerifierService.verifyAndExtract(request.idToken());
        return userRepository.findByEmail(googleInfo.getEmail())
                .map(user -> processExistingUser(user, googleInfo))
                .orElseGet(() -> registerNewGoogleUser(googleInfo));
    }

    private AuthenticationResponse processExistingUser(User user, GoogleUserInfo googleInfo) {

        if (!hasLinkedGoogleAccount(user)) {
            user.setGoogleId(googleInfo.getGoogleId());
        }

        if (!StringUtils.hasLength(user.getProfile().getThumbnailUrl())) {
            user.getProfile().setThumbnailUrl(googleInfo.getPicture());
        }

        return generatePairToken(user);
    }

    private boolean hasLinkedGoogleAccount(User user) {
        return StringUtils.hasLength(user.getGoogleId());
    }

    private AuthenticationResponse registerNewGoogleUser(GoogleUserInfo googleUserInfo) {
        Set<Role> roles = new HashSet<>();
        roleRepository.findByName(PreFixUtils.ROLE_USER).ifPresent(roles::add);

        User user = User.builder()
                .fullName(googleUserInfo.getName())
                .roles(roles)
                .googleId(googleUserInfo.getGoogleId())
                .status(UserStatus.ACTIVE)
                .email(googleUserInfo.getEmail())
                .username(generateUsernameFromGoogle(googleUserInfo.getEmail()))
                .build();

        Profile profile = Profile.builder()
                .firstName(googleUserInfo.getFirstName())
                .lastName(googleUserInfo.getLastName())
                .thumbnailUrl(googleUserInfo.getPicture())
                .user(user).build();

        user.setProfile(profile);
        userRepository.save(user);
        return generatePairToken(user);
    }

    private String generateUsernameFromGoogle(String email){
        return email.split("@")[0];
    }

    private void validateRefreshToken(long userId,String tokenId){
        String accessKey = PreFixUtils.AT_WHITE_LIST + userId;
        String refreshKey = PreFixUtils.RT_WHITE_LIST + userId;

        String refreshTokenAvailable = redisService.getString(refreshKey);
        String refreshKeyBlackList = redisService.getString(PreFixUtils.RT_BLACK_LIST + tokenId);

        // suspected hacker used token illegally
        if (!tokenId.equals(refreshTokenAvailable) || StringUtils.hasLength(refreshKeyBlackList)) {
            // vô hiệu hóa ac và rt vừa cấp
            log.warn("Suspicious refresh token usage detected for userId {}. Invalidating tokens.", userId);
            redisService.deleteKey(accessKey);
            redisService.deleteKey(refreshKey);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        log.info("Refresh token validated successfully for userId {}", userId);
    }

    private AuthenticationResponse generatePairToken(User user) {
        String tokenId = UUID.randomUUID().toString();

        String accessToken = tokenService.createAccessToken(user,tokenId, TypeToken.ACCESS_TOKEN);
        String refreshToken = tokenService.createRefreshToken(user,tokenId, TypeToken.REFRESH_TOKEN);

        saveTokenWhiteListOnRedis(user.getId(),tokenId);

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    private void saveTokenBlackListOnRedis(Jwt jwt) {
        String tokenId = jwt.getId();
        String keyAccess = PreFixUtils.AT_BLACK_LIST + tokenId;
        String keyRefresh = PreFixUtils.RT_BLACK_LIST + tokenId;
        redisService.setString(keyAccess,VALID_STATUS, Duration.between(Instant.now(), jwt.getExpiresAt()).getSeconds());
        redisService.setString(keyRefresh,VALID_STATUS, jwtKeyConfig.getRefreshDuration());

    }


    private void saveTokenWhiteListOnRedis(Long userId,String tokenId) {
        String keyAccessToken = PreFixUtils.AT_WHITE_LIST + userId;
        String keyRefreshToken = PreFixUtils.RT_WHITE_LIST + userId;
        redisService.setString(keyAccessToken,VALID_STATUS, jwtKeyConfig.getAccessDuration());
        redisService.setString(keyRefreshToken,tokenId, jwtKeyConfig.getRefreshDuration());
    }

    private User getUserNoProfileByUsername(String username){
        return userRepository.findByUsernameNoFetchProfile(username)
                .orElseThrow(()-> new UsernameNotFoundException("Username or password is incorrect"));
    }
}
