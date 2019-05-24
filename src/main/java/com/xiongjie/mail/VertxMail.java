package com.xiongjie.mail;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.mail.*;

public class VertxMail {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        MailConfig config = new MailConfig();
        config.setHostname("smtp.163.com");
        config.setPort(25);
        config.setStarttls(StartTLSOptions.REQUIRED);
        config.setUsername("xj2711992339@163.com");
        config.setPassword("xj233550");
        MailClient mailClient = MailClient.createNonShared(vertx, config);

        MailMessage message = new MailMessage();
        message.setFrom("xj2711992339@163.com");
        message.setTo("2711992339@qq.com");
        message.setText("这是vertx mail的测试邮件");

        MailAttachment attachment = new MailAttachment();
        attachment.setContentType("text/plain");
        attachment.setData(Buffer.buffer("attachment file"));
        message.setAttachment(attachment);

        mailClient.sendMail(message, result -> {
            if (result.succeeded()) {
                System.out.println(result.result());
            } else {
                result.cause().printStackTrace();
            }
            vertx.close();
        });
    }

}
