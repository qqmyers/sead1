package edu.illinois.ncsa.mmdb.web.server;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class TestMail {
    public static void main(String[] args) throws MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", "smtp.ncsa.uiuc.edu");
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("\"Luigi Marini\" <lmarini@ncsa.uiuc.edu>"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("\"Luigi Marini\" <omonimo@gmail.com>"));
        message.setSubject("Hello JavaMail");
        message.setText("It works!  It works!  Tell Luigi!");
        Transport.send(message);
    }
}