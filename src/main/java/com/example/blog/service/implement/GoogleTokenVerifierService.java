package com.example.blog.service.implement;


import com.example.blog.dto.response.GoogleUserInfo;
import com.example.blog.enums.ErrorCode;
import com.example.blog.exception.GoogleTokenVerificationException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleTokenVerifierService {

    private final GoogleIdTokenVerifier verifier;


    public GoogleUserInfo verifyAndExtract(String idTokenString) {

        GoogleIdToken idToken = verifyToken(idTokenString);
        Payload payload = idToken.getPayload();

        validatePayload(payload);

        GoogleUserInfo userInfo = extractUserInfo(payload);
        log.info("✅ Verified Google user: {}", userInfo.getEmail());
        return userInfo;
    }


    private GoogleIdToken verifyToken(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new GoogleTokenVerificationException(ErrorCode.GOOGLE_TOKEN_INVALID);
            }
            return idToken;
        } catch (GeneralSecurityException | IOException e) {
            throw new GoogleTokenVerificationException(ErrorCode.GOOGLE_TOKEN_INVALID);
        }
    }

    private void validatePayload(Payload payload) {

        if (Boolean.FALSE.equals(payload.getEmailVerified())) {
            throw new GoogleTokenVerificationException(ErrorCode.GOOGLE_TOKEN_INVALID);
        }
//
//        if (!clientId.equals(payload.getAudience())) {
//            throw new GoogleTokenVerificationException(ErrorCode.GOOGLE_TOKEN_INVALID);
//        }
    }

    private GoogleUserInfo extractUserInfo(Payload payload) {
        return GoogleUserInfo.builder()
                .googleId(payload.getSubject())
                .email(payload.getEmail())
                .emailVerified(payload.getEmailVerified())
                .name((String) payload.get("name"))
                .firstName((String) payload.get("given_name"))
                .lastName((String) payload.get("family_name"))
                .picture((String) payload.get("picture"))
                .locale((String) payload.get("locale"))
                .build();
    }
}
