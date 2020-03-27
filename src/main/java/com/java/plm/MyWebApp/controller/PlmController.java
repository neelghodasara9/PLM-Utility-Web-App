package com.java.plm.MyWebApp.controller;


import java.io.IOException;
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
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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


		if(objConfig.getRunIcomsAudit() != null) {
			logger.info("\n\n------------------------------------------------------------------------------------\nICOMS End Date Compare Utility\n------------------------------------------------------------------------------------");
			if(objConfig.getNcInputpath() != null) {
				IcomsPlmEndDateAudit objIcomsDateComp = new IcomsPlmEndDateAudit();
				csvFileHeaders.put(objIcomsDateComp.getClass().getSimpleName(), 	new String[] { "Site Id", "Site Code" , "Campaign Code","PLM Discount id", "PLM Start Date", "PLM End Date", "ICOMS Start Date", "ICOMS End Date", "Offer series", "Comments" } );

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


		if(objConfig.getRunMissingOffersAudit() != null) {
			logger.info("\n\n------------------------------------------------------------------------------------\nMISSING_OFFERS_AUDIT Utility\n------------------------------------------------------------------------------------");
			if(objConfig.getNcInputpath() != null) {
				MissingOfferAudit objMissingOffers = new MissingOfferAudit();

				csvFileHeaders.put(objMissingOffers.getClass().getSimpleName(), 	new String[] { "DISCOUNT_ID", "DISCOUNT_CODE", "DESCRIPTION", "TAG", "DISCOUNT_START_DATE", "DISCOUNT_END_DATE", "needsOffers?", "CNT" });

				fname.add(outFilePath+ "\\MISSING_OFFERS_FOR_ACTIVE_CAMPAIGNS_AUDIT-" + timeStamp + ".csv");
				logger.info("Calling executeAudit() method...");
				objMissingOffers.executeAudit(objConfig.getNcInputpath(), objConfig.getFutureWindow(), outFilePath+ "\\MISSING_OFFERS_FOR_ACTIVE_CAMPAIGNS_AUDIT-" + timeStamp + ".csv", tagMap);
				mailBody.append("<br><br>Material impacts for Missing Offers Audit:<br><br>" + htmlMailBodyGenerator(materialImpactsForMail.get(objMissingOffers.getClass().getSimpleName()), objMissingOffers.getClass().getSimpleName()));
				logger.info("Out of executeAudit() method.");


			}
			else
				logger.error("Cannot execute Missing offers audit, missing \"NORMALIZED_CAMPAIGN_INPUT_PATH\" key in resources/config file.");
		}


		if(objConfig.getRunNormalizedCampAudit() != null) {
			logger.info("\n\n------------------------------------------------------------------------------------\nNormalized Campaign Audit Utility\n------------------------------------------------------------------------------------");
			if(objConfig.getNcInputpath() != null) {
				NormalizedCampaignAudit objNormalizedCamp = new NormalizedCampaignAudit();
				csvFileHeaders.put(objNormalizedCamp.getClass().getSimpleName(), new String[] { "DISCOUNT_ID", "DISCOUNT_CODE", "NEEDS_OFFER?", "INTENT-csv", "INTENT-db", "AGENTTYPE-csv", "AGENTTYPE-db","CHANNELS-csv", "CHANNELS-db", "ADDONTYPE-csv", "ADDONTYPE-db", "DISCREPANCY" }); 

				fname.add(outFilePath);
				logger.info("Calling executeNormCompare() method...");
				objNormalizedCamp.executeNormCompare(objConfig.getNcInputpath(), outFilePath + "\\Nomalized-campaign-audit-" + timeStamp + ".csv", "|");
				mailBody.append("<br><br>Material impacts for Normalized campaign Audit:<br><br>" + htmlMailBodyGenerator(materialImpactsForMail.get(objNormalizedCamp.getClass().getSimpleName()), objNormalizedCamp.getClass().getSimpleName()));
				logger.info("Out of executeNormCompare() method.");
			}
			else
				logger.error("Cannot execute Normalized campaign audit, missing \"NORMALIZED_CAMPAIGN_INPUT_PATH\" key in resources/config file.");

		}


		if(objConfig.getRunOfferCampEndDateAudit() != null) {

			logger.info("\n\n------------------------------------------------------------------------------------\nACTIVE_OFFER_CAMPAIGN_END_DATE_CHECK_AUDIT Utility\n------------------------------------------------------------------------------------");


			PlmActiveOfferCampEndDateAudit objOfferCampEndDateAudit = new PlmActiveOfferCampEndDateAudit();
			csvFileHeaders.put(objOfferCampEndDateAudit.getClass().getSimpleName(), new String[] { "EXTERNAL_ID", "OFFER_ID", "SITE_ID", "OFFER_NAME", "SALES_ADVICE", "OFFER_START_DATE", "DISCOUNT_START_DATE", "OFFER_END_DATE", "DISCOUNT_END_DATE", "DISCOUNT_ID","DISCOUNT_CODE", "DISTRO_PREFERENCE", "STATUS", "CREATED_DATE", "PROJECT_CODE", "INTAKE_NAME", "INTAKE_DESCRIPTION" });

			fname.add(outFilePath);
			logger.info("Calling compareEndDates() method...");
			objOfferCampEndDateAudit.compareEndDates(objConfig.getOfferCampEndDateIgnoreIntakes(), outFilePath + "\\ACTIVE_OFFER_CAMPAIGN_END_DATE_AUDIT-" + timeStamp + ".csv" , objConfig.getOfferCampEndDateIgnoreKeywords(), objConfig.getFutureWindow());
			mailBody.append("<br><br>Material impacts for Offer-campaign End Date Audit:<br><br>" + htmlMailBodyGenerator(materialImpactsForMail.get(objOfferCampEndDateAudit.getClass().getSimpleName()), objOfferCampEndDateAudit.getClass().getSimpleName()));
			logger.info("Out of compareEndDates() method.");

		}

		if(objConfig.getRunOfferCampSiteAudit() != null) {

			logger.info("\n\n------------------------------------------------------------------------------------\nACTIVE_OFFER_CAMPAIGN_SITE_CHECK_AUDIT Utility\n------------------------------------------------------------------------------------");

			PlmActiveOfferCampSiteAudit objOfferCampSiteAudit = new PlmActiveOfferCampSiteAudit();
			csvFileHeaders.put(objOfferCampSiteAudit.getClass().getSimpleName(), new String[] { "INTAKE_REQUEST", "OFFER_ID", "OFFER_NAME", "SALES_ADVICE", "DISTRO_PREFERENCE", "DISCOUNT_ID","DISCOUNT_CODE", "DISCOUNT_SITE_ID", "OFFER_SITE_ID", "CORRECTIVE_ACTION" }); 


			fname.add(outFilePath);
			logger.info("Calling compareSites() method...");
			objOfferCampSiteAudit.compareSites(objConfig.getOfferCampSiteIgnoreIntakes(), "", outFilePath + "\\ACTIVE_OFFER_CAMPAIGN_SITE_CHECK_AUDIT-" + timeStamp + ".csv", objConfig.getOfferCampSiteIgnoreKeywords());
			mailBody.append("<br><br>Material impacts for Offer-campaign Site Audit:<br><br>" + htmlMailBodyGenerator(materialImpactsForMail.get(objOfferCampSiteAudit.getClass().getSimpleName()), objOfferCampSiteAudit.getClass().getSimpleName()));
			logger.info("Out of compareSites() method.");

		}



		if(objConfig.getRunPlmPpAudit() != null) {

			logger.info("\n\n------------------------------------------------------------------------------------\nACTIVE_OFFER_PLM_PINPOINT_AUDIT Utility\n------------------------------------------------------------------------------------");
			PlmPinpointAudit objPlmPpAudit = new PlmPinpointAudit();

			fname.add(outFilePath);
			logger.info("Calling executeAudit() method...");
			objPlmPpAudit.executePlmPpAudit(objConfig.getPlmPpColsToCompare(), objConfig.getPlmPpColsToView() , objConfig.getFutureWindow(), outFilePath + "\\Plm_Pinpoint_Offer_Audit-" + timeStamp + ".csv");
			mailBody.append("<br><br>Material impacts for PLM-PP Orphans Audit:<br><br>" + htmlMailBodyGenerator(materialImpactsForMail.get(objPlmPpAudit.getClass().getSimpleName()), objPlmPpAudit.getClass().getSimpleName()));
			logger.info("Out of executeAudit() method.");

		}



		//model.addAttribute("MaterialImpacts", materialImpactsForMail);
		//model.addAttribute("Headers", csvFileHeaders);

		if(!errorsLst.isEmpty())
			mailBody.append("<br><br><br>Error/s encountered:<br><br>" + htmlMailBodyGenerator(errorsLst, "Errors"));

		StringBuilder toEmail = new StringBuilder();
		//email audit reports
		if(objConfig.getTriggerMail() != null && objConfig.getRecipientsList() != null) {

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


			mailBody.append("<br><br>Regards, <br>Neel");
			//if(fname.size()>0)
			SendMail.sendAttachmentEmail(session, toEmail.toString(), "PLM-Audit-reports-" + date, mailBody.toString(), fname);

		}
		logger.info("\n\nExiting the utility now..");
		model.addAttribute("MailBody", mailBody);

		return "audit-results";
	}

	static String htmlMailBodyGenerator(ArrayList<String[]> input, String auditName) {
		StringBuilder bodyText = new StringBuilder();
		bodyText.append("<table width='100%' border='1' align='center'>"); 

		bodyText.append("<tr>");
		for (String str : csvFileHeaders.get(auditName)) {
			bodyText.append("<th><b>" + str + "<b></th>"); 
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

	@RequestMapping(value = "/downloadCSV")
	public void downloadCSV(HttpServletResponse response) throws IOException {

		response.setContentType("text/csv");
		String reportName = "C:\\Users\\B45752\\git\\PLM-Utility-Web-App\\MISSING_OFFERS_FOR_ACTIVE_CAMPAIGNS_AUDIT-20200326093243.csv";
		response.setHeader("Content-disposition", "attachment;filename="+reportName);
	}

}

