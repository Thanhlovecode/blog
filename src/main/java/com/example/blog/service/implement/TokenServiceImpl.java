package com.example.blog.service.implement;

import com.example.blog.domain.Role;
import com.example.blog.domain.User;
import com.example.blog.enums.TypeToken;
import com.example.blog.service.TokenService;
import com.example.blog.utils.PreFixUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {


    private final JwtEncoder jwtEncoder;

    @Value("${token.access-duration}")
    private long accessTokenExpiration;

    @Value("${security.jwt.public-key}")
    private RSAPublicKey publicKey;

    @Value("${token.refresh-duration}")
    private long refreshTokenExpiration;

    private static final String TYPE_TOKEN = "type_token";


    @Override
    public String createAccessToken(User user, String tokenId, TypeToken type) {
        return generateToken(user,tokenId,TypeToken.ACCESS_TOKEN,accessTokenExpiration);
    }

    @Override
    public String createRefreshToken(User user, String tokenId, TypeToken type) {
        return generateToken(user,tokenId,TypeToken.REFRESH_TOKEN,refreshTokenExpiration);
    }

    private String generateToken(User user, String tokenId, TypeToken typeToken,Long expireTimeToken){
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(user.getUsername())
                .claim(PreFixUtils.USER_ID,user.getId())
                .claim(TYPE_TOKEN,typeToken)
                .issuedAt(now)
                .subject(user.getUsername())
                .expiresAt(now.plus(Duration.ofSeconds(expireTimeToken)))
                .id(tokenId)
                .claim("scope",buildRoles(user))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header,claims))
                .getTokenValue();
    }

    @Override
    public Jwt validateToken(String token){
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder
                .withPublicKey(publicKey)
                .build();
        return nimbusJwtDecoder.decode(token);
    }


    private String buildRoles(User user){
        return user.getRoles()
                .stream()
                .filter(Objects::nonNull)
                .map(Role::getName)
                .collect(Collectors.joining(","));
    }
}
