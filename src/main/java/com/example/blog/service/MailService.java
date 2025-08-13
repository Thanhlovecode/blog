package com.example.blog.service;

public interface MailService {
    void sendSimpleMail(String to, String subject, String content);
}
