package com.example.blog.validator;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {PasswordConstraintValidator.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.RECORD_COMPONENT})
public @interface ValidPassword {
    String message() default "Invalid Password";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
