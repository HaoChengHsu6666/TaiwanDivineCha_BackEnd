package com.chrishsu.taiwanDivineCha.service;

public interface EmailService {

    void sendResetPasswordEmail(String toEmail, String resetLink);

    void sendEmailVerificationAndSetPasswordEmail(String toEmail, String verificationLink);
}
