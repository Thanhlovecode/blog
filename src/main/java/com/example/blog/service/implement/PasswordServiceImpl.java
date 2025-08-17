package com.example.blog.service.implement;

import com.example.blog.domain.User;
import com.example.blog.dto.request.EmailRequest;
import com.example.blog.dto.request.OTPRequest;
import com.example.blog.dto.request.PasswordRequest;
import com.example.blog.enums.ErrorCode;
import com.example.blog.exception.AppException;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.MailService;
import com.example.blog.service.PasswordService;
import com.example.blog.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordServiceImpl implements PasswordService {

    private static final String SUBJECT_RESET_PASSWORD = "Reset your password";
    private static final String CONTENT = "Your password reset OTP is:";
    private static final String OTP_KEY_PREFIX = "KEY_EMAIL:";
    private static final String PASSWORD_RESET_ALLOWED_KEY_PREFIX = "PASSWORD_RESET_ALLOWED:";


    @Value("${key.expiration.otp}")
    private int otpExpiration;

    @Value("${key.change-password.expire}")
    private int changePasswordExpire;

    private final MailService mailService;
    private final RedisService redisService;
    private final UserRepository userRepository;


    @Override
    public void verifyOtp(OTPRequest otpRequest) {
        String keyEmail = getPrefixKeyEmail(otpRequest.email());
        String otpFromRedis = redisService.getString(keyEmail);

        if (!otpRequest.otp().equals(otpFromRedis)) {
            throw new AppException(ErrorCode.OTP_INCORRECT);
        }

        redisService.deleteKey(keyEmail);
        redisService.setString(buildPasswordResetAllowedKey(otpRequest.email()),"1", changePasswordExpire);

        log.info("OTP verified successfully for email: {}", otpRequest.email());
    }

    @Override
    public void sendOTP(EmailRequest emailRequest) {
        String email = emailRequest.email();

        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            String otp = generateRandomOtp();
            redisService.setString(getPrefixKeyEmail(email), otp, otpExpiration);
            mailService.sendSimpleMail(email, SUBJECT_RESET_PASSWORD, CONTENT + otp);
            log.info("OTP sent successfully with email: {}", email);
        },()->log.info("OTP request ignored for non-existing email: {}", email));


    }

    @Override
    @Transactional
    public void changePassword(PasswordRequest passwordRequest) {

        String key = buildPasswordResetAllowedKey(passwordRequest.email());

        String resetPasswordAllowed = redisService.getString(key);
        checkValidEmailAndPassword(resetPasswordAllowed, passwordRequest);

        User user = getUserByEmail(passwordRequest.email());
        user.setPassword(passwordRequest.password());
        userRepository.save(user);
        redisService.deleteKey(key);

        log.info("Password changed successfully with email: {}", passwordRequest.email());
    }


    public String generateRandomOtp() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(100_000, 1_000_000);
        return String.valueOf(number);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private String getPrefixKeyEmail(String email) {
        return OTP_KEY_PREFIX + email;
    }


   private String buildPasswordResetAllowedKey(String email) {
        return PASSWORD_RESET_ALLOWED_KEY_PREFIX + email;
   }


   private void checkValidEmailAndPassword(String allowChangePassword,PasswordRequest passwordRequest) {
       if(allowChangePassword == null){
           throw new AppException(ErrorCode.EMAIL_NOT_VERIFY_OTP);
       }

       if (!passwordRequest.password().equals(passwordRequest.confirmPassword())) {
           throw new AppException(ErrorCode.PASSWORD_NOT_MATCH);
       }
   }


}
