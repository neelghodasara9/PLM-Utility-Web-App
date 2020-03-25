package com.java.plm.MyWebApp.Service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

public class SendMail {

	final static Logger logger = Logger.getLogger(SendMail.class);

	public static void sendAttachmentEmail(Session session, String toEmail, String subject, String body, List<String> arrFilename){
		try{
			MimeMessage msg = new MimeMessage(session);
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress("no_reply@PLM-Audit-Report.com", "PLM-Audit-Reports"));

			msg.setReplyTo(InternetAddress.parse("no_reply@PLM-Audit-Report.com", false));

			msg.setSubject(subject, "UTF-8");

			msg.setSentDate(new Date());

			for(String mailid : toEmail.split(";")) {
				if(!mailid.trim().equals(""))
					msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(mailid.trim(), false));
			}

			msg.addRecipient(RecipientType.BCC, new InternetAddress("plm.audits@gmail.com"));
		
			// Create the message body part
			BodyPart messageBodyPart = new MimeBodyPart();

			// Fill the message
			//msg.setContent(body, "text/html");
			//msg.setContent(body, "text/html; charset=utf-8");
			messageBodyPart.setContent(body, "text/html; charset=utf-8");

			// Create a multipart message for attachment
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Second part is attachment
			for(String filename : arrFilename) {
				messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(filename);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(filename.split("\\\\")[filename.split("\\\\").length-1]);
				multipart.addBodyPart(messageBodyPart);
				msg.setContent(multipart);
			}

			// Send message
			Transport.send(msg);
			logger.info("Email sent successfully with the audit reports!!");
		}catch (MessagingException e) {
			logger.error("Error in sendAttachmentEmail() method: "+ e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("Error in sendAttachmentEmail() method: "+ e.getMessage());
		}
	}

}
