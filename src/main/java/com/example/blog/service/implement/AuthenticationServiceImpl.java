package com.example.blog.service.implement;

import com.example.blog.domain.User;
import com.example.blog.dto.request.AuthenticationRequest;
import com.example.blog.dto.request.RefreshTokenRequest;
import com.example.blog.dto.response.AuthenticationResponse;
import com.example.blog.enums.ErrorCode;
import com.example.blog.enums.TypeToken;
import com.example.blog.exception.AppException;
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
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RedisService redisService;

    private static final String VALID_STATUS = "1";

    @Value("${token.access-duration}")
    private long accessTokenExpiration;

    @Value("${token.refresh-duration}")
    private long refreshTokenExpiration;

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
        redisService.setString(keyRefresh,VALID_STATUS,refreshTokenExpiration);

    }


    private void saveTokenWhiteListOnRedis(Long userId,String tokenId) {
        String keyAccessToken = PreFixUtils.AT_WHITE_LIST + userId;
        String keyRefreshToken = PreFixUtils.RT_WHITE_LIST + userId;
        redisService.setString(keyAccessToken,VALID_STATUS,accessTokenExpiration);
        redisService.setString(keyRefreshToken,tokenId,refreshTokenExpiration);
    }

    private User getUserNoProfileByUsername(String username){
        return userRepository.findByUsernameNoFetchProfile(username)
                .orElseThrow(()-> new UsernameNotFoundException("Username or password is incorrect"));
    }
}
