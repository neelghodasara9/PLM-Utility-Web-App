package com.java.plm.MyWebApp.controller;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.java.plm.MyWebApp.Service.SendMail;
import com.java.plm.MyWebApp.model.ConfigInput;

@Controller
public class PlmController {

	final static Logger logger = Logger. getLogger(PlmController.class);
	public static Map<String,String> tagMap = new TreeMap<String,String>();
	public static Map<String,String> poNotesMap = new TreeMap<String,String>();
	public static Map<String,String> tierMap = new TreeMap<String,String>();
	public static Map<String,String[]> csvFileHeaders = new TreeMap<String,String[]>();
	public static Map<String, ArrayList<String[]>> materialImpactsForMail = new TreeMap<String, ArrayList<String[]>>();
	public static ArrayList<String[]> errorsLst = new ArrayList<String[]>();

	@GetMapping("/")
	public String index(Model model) {
		ConfigInput res=new ConfigInput();  
		//provide reservation object to the model   
		model.addAttribute("config", res);  
		return "welcome";
	}

	@PostMapping("/results")
	public String runAudits(@ModelAttribute("config") ConfigInput objConfig, Model model) {
		String timeStamp  = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
		List<String> fname = new LinkedList<String>();
		String timeStamp2  = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new java.util.Date());
		StringBuilder mailBody = new StringBuilder();
		mailBody.append("Hi, <br><br>Please find attached PLM Production Audit reports for " + timeStamp2 + ". It's set to capture all discrepancies from the past and " + objConfig.getFutureWindow() + " day/s in future. <br><br> <i>This is an auto generated email, please do not reply to this mail.</i><br><br>");
		String outFilePath = System.getProperty("user.dir");

		if(objConfig.getRunIcomsAudit().equals("YES")) {
			logger.info("\n\n------------------------------------------------------------------------------------\nICOMS End Date Compare Utility\n------------------------------------------------------------------------------------");
			if(objConfig.getNcInputpath() != null) {
				csvFileHeaders.put("IcomsPlmEndDateAudit", 	new String[] { "Site Id", "Site Code" , "Campaign Code","PLM Discount id", "PLM Start Date", "PLM End Date", "ICOMS Start Date", "ICOMS End Date", "Offer series", "Comments" } );
				IcomsPlmEndDateAudit objIcomsDateComp = new IcomsPlmEndDateAudit();

				fname.add(outFilePath + "\\Icoms-Plm-EndDate-Audit-" + timeStamp + ".csv");
				logger.info("Calling compareUtility() method...");
				objIcomsDateComp.compareUtility(objConfig.getNcInputpath(), objConfig.getFutureWindow(), "", "", outFilePath + "\\Icoms-Plm-EndDate-Audit-" + timeStamp + ".csv");
				mailBody.append("<br>Material impacts for ICOMS-PLM Audit:<br><br>" + htmlMailBodyGenerator(materialImpactsForMail.get(objIcomsDateComp.getClass().getSimpleName()), objIcomsDateComp.getClass().getSimpleName()));
				logger.info("Out of compareUtility() method.");
				model.addAttribute(objIcomsDateComp.getClass().getSimpleName(), materialImpactsForMail.get(objIcomsDateComp.getClass().getSimpleName()));

			}
			else
				logger.error("Cannot execute ICOMS-PLM Campaign End date audit, missing \"NORMALIZED_CAMPAIGN_INPUT_PATH\" key in resources/config file.");
		}	


		if(objConfig.getRunMissingOffersAudit().equals("YES")) {
			logger.info("\n\n------------------------------------------------------------------------------------\nMISSING_OFFERS_AUDIT Utility\n------------------------------------------------------------------------------------");
			if(objConfig.getNcInputpath() != null) {
				MissingOfferAudit objMissingOffers = new MissingOfferAudit();

				csvFileHeaders.put(objMissingOffers.getClass().getSimpleName(), 	new String[] { "DISCOUNT_ID", "DISCOUNT_CODE", "DESCRIPTION", "TAG", "DISCOUNT_START_DATE", "DISCOUNT_END_DATE", "needsOffers?", "CNT" });


				fname.add(outFilePath+ "\\MISSING_OFFERS_FOR_ACTIVE_CAMPAIGNS_AUDIT-" + timeStamp + ".csv");
				logger.info("Calling executeAudit() method...");
				objMissingOffers.executeAudit(objConfig.getNcInputpath(), objConfig.getFutureWindow(), outFilePath+ "\\MISSING_OFFERS_FOR_ACTIVE_CAMPAIGNS_AUDIT-" + timeStamp + ".csv", tagMap);
				mailBody.append("<br><br>Material impacts for Missing Offers Audit:<br><br>" + htmlMailBodyGenerator(materialImpactsForMail.get(objMissingOffers.getClass().getSimpleName()), objMissingOffers.getClass().getSimpleName()));
				logger.info("Out of executeAudit() method.");
				model.addAttribute(objMissingOffers.getClass().getSimpleName(), materialImpactsForMail.get(objMissingOffers.getClass().getSimpleName()));

			}
			else
				logger.error("Cannot execute Missing offers audit, missing \"NORMALIZED_CAMPAIGN_INPUT_PATH\" key in resources/config file.");
		}

		StringBuilder toEmail = new StringBuilder();
		//email audit reports
		if(objConfig.getTriggerMail() != null && !objConfig.getRecipientsList().equals("")) {

			toEmail.append(objConfig.getRecipientsList()); // can be any email id 


			logger.info("\n\n------------------------------------------------------------------------------------\nPreparing to send a mail\n------------------------------------------------------------------------------------");

			String date  = new SimpleDateFormat("yyyyMMdd").format(new java.util.Date());

			final String fromEmail = "plm.audits@gmail.com"; //requires valid gmail id
			final String password = "Cox@sep19"; // correct password for gmail id


			logger.info("SSLEmail Start");
			Properties props = new Properties();
			props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
			props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
			props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
			props.put("mail.smtp.port", "465"); //SMTP Port

			Authenticator auth = new Authenticator() {
				//override the getPasswordAuthentication method
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(fromEmail, password);
				}
			};

			Session session = Session.getDefaultInstance(props, auth);
			logger.info("Session created");

			if(!errorsLst.isEmpty())
				mailBody.append("<br><br><br>Error/s encountered:<br><br>" + htmlMailBodyGenerator(errorsLst, "Errors"));


			mailBody.append("<br><br>Regards, <br>Neel");
			//if(fname.size()>0)
			SendMail.sendAttachmentEmail(session, toEmail.toString(), "PLM-Audit-reports-" + date, mailBody.toString(), fname);

		}
		logger.info("\n\nExiting the utility now..");

		return "audit-results";
	}

	static String htmlMailBodyGenerator(ArrayList<String[]> input, String auditName) {
		StringBuilder bodyText = new StringBuilder();
		bodyText.append("<table width='100%' border='1' align='center'>"); 

		bodyText.append("<tr>");
		for (String str : csvFileHeaders.get(auditName)) {
			bodyText.append("<td><b>" + str + "<b></td>"); 
		}
		bodyText.append("</tr>");		

		for (String[] temp : input) {
			bodyText.append("<tr>");
			for (String str : temp) {
				bodyText.append("<td>" + str + "</td>"); 
			}
			bodyText.append("</tr>");		
		}
		bodyText.append("</table>"); 
		return bodyText.toString();
	} 
}

