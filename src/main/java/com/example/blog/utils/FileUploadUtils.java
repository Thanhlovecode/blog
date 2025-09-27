package com.example.blog.utils;

import com.example.blog.enums.ErrorCode;
import com.example.blog.exception.AppException;
import lombok.experimental.UtilityClass;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

@UtilityClass
public class FileUploadUtils {

    public static final int MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final Pattern IMAGE_NAME_PATTERN =
            Pattern.compile("^\\S+\\.(jpg|jpeg|png|gif|bmp|webp)$", Pattern.CASE_INSENSITIVE);


    public static void assertAllowed(MultipartFile file) {
        if(file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_SIZE_TOO_LARGE);
        }

        String fileName = file.getOriginalFilename();

        if(!isAllowedFileName(fileName)){
            throw new AppException(ErrorCode.FILE_EXTENSION_NOT_SUPPORTED);
        }
    }

    public static boolean isAllowedFileName(String fileName) {
        return IMAGE_NAME_PATTERN.matcher(fileName).matches();
    }


}
