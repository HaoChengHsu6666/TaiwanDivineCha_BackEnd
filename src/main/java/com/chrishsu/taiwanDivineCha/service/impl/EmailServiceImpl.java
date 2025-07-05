package com.chrishsu.taiwanDivineCha.service.impl;

import com.chrishsu.taiwanDivineCha.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  // 您可以考慮從 application.properties 中讀取這個發件人郵箱
  // @Value("${spring.mail.username}")
  // private String senderEmail;
  private final String senderEmail = "becomerocker@gmail.com"; // 使用您之前提供的發件人郵箱

  @Autowired
  public EmailServiceImpl(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * 發送重設密碼郵件。
   *
   * @param toEmail 接收者的電子郵件地址
   * @param resetLink 重設密碼連結
   */
  @Override
  public void sendResetPasswordEmail(String toEmail, String resetLink) {
    MimeMessage message = mailSender.createMimeMessage();
    try {
      // true 表示啟用 multipart/mixed (允許 HTML 和附件)，"UTF-8" 設定字符集
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(senderEmail); // 設定發件人
      helper.setTo(toEmail);
      helper.setSubject("好神茶廠：重設您的密碼"); // 郵件主題

      String htmlContent = "<html><body>"
              + "<p>您好，</p>"
              + "<p>您最近請求重設好神茶廠的密碼。請點擊以下連結來完成重設：</p>"
              + "<p><a href=\"" + resetLink + "\">點擊此處重設密碼</a></p>" // 可點擊連結
              + "<p>如果這不是您發出的請求，請忽略此郵件。</p>"
              + "<p>此連結將在 10 分鐘後失效。</p>" // 建議與後端 Token 有效期一致
              + "<p>好神茶廠 客服團隊</p>"
              + "</body></html>";
      helper.setText(htmlContent, true); // true 表示內容是 HTML

      mailSender.send(message);
      System.out.println("重設密碼郵件已發送至: " + toEmail);
    } catch (MessagingException e) {
      System.err.println("發送重設密碼郵件時發生錯誤: " + e.getMessage());
      // 在實際應用中，您可能需要記錄更詳細的錯誤或拋出自定義異常
      throw new RuntimeException("無法發送重設密碼郵件", e);
    } catch (MailException e) {
      System.err.println("郵件服務器錯誤: " + e.getMessage());
      throw new RuntimeException("郵件服務器暫時不可用", e);
    }
  }

  /**
   * 發送電子郵件驗證郵件。
   * 告知註冊者點擊連結並認證後，將被引導設定密碼。
   *
   * @param to 接收者的電子郵件地址
   * @param verificationLink 電子郵件驗證連結
   */
  @Override
  public void sendEmailVerificationAndSetPasswordEmail(String to, String verificationLink) {
    MimeMessage message = mailSender.createMimeMessage();
    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(senderEmail); // 設定發件人
      helper.setTo(to);
      helper.setSubject("好神茶廠：請驗證您的電子郵件地址"); // 郵件主題

      String htmlContent = "<html><body>"
              + "<p>您好，</p>"
              + "<p>感謝您註冊好神茶廠！</p>"
              + "<p>為了完成您的帳戶註冊，請點擊以下連結驗證您的電子郵件地址：</p>"
              + "<p><a href=\"" + verificationLink + "\">驗證我的電子郵件並設定密碼</a></p>" // 可點擊連結，包含提示
              + "<p>點擊此連結並成功驗證後，您將被引導完成您的帳戶設定，包括設定您的密碼。</p>"
              + "<p>此驗證連結將於 24 小時後過期。</p>"
              + "<p>如果這不是您發出的請求，請忽略此郵件。</p>"
              + "<p>好神茶廠 客服團隊</p>"
              + "</body></html>";
      helper.setText(htmlContent, true); // true 表示內容是 HTML

      mailSender.send(message);
      System.out.println("電子郵件驗證郵件已發送至: " + to);
    } catch (MessagingException e) {
      System.err.println("發送電子郵件驗證郵件時發生錯誤: " + e.getMessage());
      throw new RuntimeException("無法發送電子郵件驗證郵件", e);
    } catch (MailException e) {
      System.err.println("郵件服務器錯誤: " + e.getMessage());
      throw new RuntimeException("郵件服務器暫時不可用", e);
    }
  }
}