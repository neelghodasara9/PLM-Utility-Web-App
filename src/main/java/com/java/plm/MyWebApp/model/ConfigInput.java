package com.java.plm.MyWebApp.model;

import org.springframework.stereotype.Component;

@Component
public class ConfigInput {
	private String futureWindow;
	private String triggerMail;
	private String ncInputpath;
	private String runIcomsAudit;
	private String runMissingOffersAudit;
	private String runNormalizedCampAudit;
	private String recipientsList;
	
	public ConfigInput() {
		super();
		this.futureWindow="0";
		this.triggerMail = "NO";
		this.ncInputpath = null;
		this.runIcomsAudit = "NO";
		this.runMissingOffersAudit = "NO";
		this.runNormalizedCampAudit = "NO";
		this.recipientsList = null;
	}
	
	public String getRecipientsList() {
		return recipientsList;
	}

	public void setRecipientsList(String recipientsList) {
		this.recipientsList = recipientsList;
	}

	public String getFutureWindow() {
		return futureWindow;
	}

	public String getTriggerMail() {
		return triggerMail;
	}

	public void setTriggerMail(String triggerMail) {
		this.triggerMail = triggerMail;
	}

	public void setFutureWindow(String futureWindow) {
		this.futureWindow = futureWindow;
	}
	
	public String getNcInputpath() {
		return ncInputpath;
	}

	public void setNcInputpath(String ncInputpath) {
		this.ncInputpath = ncInputpath;
	}

	public String getRunIcomsAudit() {
		return runIcomsAudit;
	}

	public void setRunIcomsAudit(String runIcomsAudit) {
		this.runIcomsAudit = runIcomsAudit;
	}

	public String getRunMissingOffersAudit() {
		return runMissingOffersAudit;
	}

	public void setRunMissingOffersAudit(String runMissingOffersAudit) {
		this.runMissingOffersAudit = runMissingOffersAudit;
	}

	public String getRunNormalizedCampAudit() {
		return runNormalizedCampAudit;
	}

	public void setRunNormalizedCampAudit(String runNormalizedCampAudit) {
		this.runNormalizedCampAudit = runNormalizedCampAudit;
	}

}
