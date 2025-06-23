package com.chrishsu.taiwanDivineCha.service.impl;

import com.chrishsu.taiwanDivineCha.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

  @Service
  public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetPasswordEmail(String toEmail, String resetLink) {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom("your_email@gmail.com"); // 替換為您 application.properties 中的發件人郵箱
      message.setTo(toEmail);
      message.setSubject("好神茶廠：重設您的密碼");
      message.setText("您好，\n\n您最近請求重設好神茶廠的密碼。請點擊以下連結來完成重設：\n\n"
        + resetLink + "\n\n如果您沒有請求此操作，請忽略此郵件。\n\n"
        + "此連結將在 1 小時後失效。\n\n"
        + "好神茶廠 客服團隊");
      mailSender.send(message);

      // 測試階段，可以保留打印，確保郵件內容和連結正確
      System.out.println("--- 發送重設密碼郵件 ---");
      System.out.println("收件人: " + toEmail);
      System.out.println("郵件內容: " + resetLink);
      System.out.println("-------------------------");
    }
}
