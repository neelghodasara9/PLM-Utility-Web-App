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
	private String runOfferCampEndDateAudit;
	private String runOfferCampSiteAudit;
	private String runPlmPpAudit;
	private String offerCampEndDateIgnoreIntakes;
	private String offerCampEndDateIgnoreKeywords;
	private String offerCampSiteIgnoreIntakes;
	private String offerCampSiteIgnoreKeywords;
	private String plmPpColsToCompare;
	private String plmPpColsToView;



	public String getPlmPpColsToCompare() {
		return plmPpColsToCompare;
	}

	public void setPlmPpColsToCompare(String plmPpColsToCompare) {
		this.plmPpColsToCompare = plmPpColsToCompare;
	}

	public String getPlmPpColsToView() {
		return plmPpColsToView;
	}

	public void setPlmPpColsToView(String plmPpColsToView) {
		this.plmPpColsToView = plmPpColsToView;
	}

	public String getOfferCampEndDateIgnoreIntakes() {
		return offerCampEndDateIgnoreIntakes;
	}

	public void setOfferCampEndDateIgnoreIntakes(String offerCampEndDateIgnoreIntakes) {
		this.offerCampEndDateIgnoreIntakes = offerCampEndDateIgnoreIntakes;
	}

	public String getOfferCampEndDateIgnoreKeywords() {
		return offerCampEndDateIgnoreKeywords;
	}

	public void setOfferCampEndDateIgnoreKeywords(String offerCampEndDateIgnoreKeywords) {
		this.offerCampEndDateIgnoreKeywords = offerCampEndDateIgnoreKeywords;
	}

	public String getOfferCampSiteIgnoreIntakes() {
		return offerCampSiteIgnoreIntakes;
	}

	public void setOfferCampSiteIgnoreIntakes(String offerCampSiteIgnoreIntakes) {
		this.offerCampSiteIgnoreIntakes = offerCampSiteIgnoreIntakes;
	}

	public String getOfferCampSiteIgnoreKeywords() {
		return offerCampSiteIgnoreKeywords;
	}

	public void setOfferCampSiteIgnoreKeywords(String offerCampSiteIgnoreKeywords) {
		this.offerCampSiteIgnoreKeywords = offerCampSiteIgnoreKeywords;
	}

	public String getRunOfferCampEndDateAudit() {
		return runOfferCampEndDateAudit;
	}

	public void setRunOfferCampEndDateAudit(String runOfferCampEndDateAudit) {
		this.runOfferCampEndDateAudit = runOfferCampEndDateAudit;
	}

	public String getRunOfferCampSiteAudit() {
		return runOfferCampSiteAudit;
	}

	public void setRunOfferCampSiteAudit(String runOfferCampSiteAudit) {
		this.runOfferCampSiteAudit = runOfferCampSiteAudit;
	}

	public String getRunPlmPpAudit() {
		return runPlmPpAudit;
	}

	public void setRunPlmPpAudit(String runPlmPpAudit) {
		this.runPlmPpAudit = runPlmPpAudit;
	}

	public ConfigInput() {
		super();
		this.futureWindow="0";
		this.triggerMail = "NO";
		this.ncInputpath = null;
		this.runIcomsAudit = "NO";
		this.runMissingOffersAudit = "NO";
		this.runNormalizedCampAudit = "NO";
		this.recipientsList = null;
		this.runOfferCampEndDateAudit = "NO";
		this.runOfferCampSiteAudit = "NO";
		this.runPlmPpAudit = "NO";
		this.offerCampEndDateIgnoreIntakes = "IR_06272019_10659, IR_07082019_10673";
		this.offerCampEndDateIgnoreKeywords = "expiring,expire,duplicate,not needed,retiring,retire,test,dont need";
		this.offerCampSiteIgnoreIntakes = "IR_06272019_10659, IR_07082019_10673";
		this.offerCampSiteIgnoreKeywords = "booster";
		this.plmPpColsToCompare = "END_DATE = EXPIRATION_DATE | DISCOUNT_CODE = PROPOSITION_CAMPAIGNS";
		this.plmPpColsToView = "PLM.NAME, PLM.PROJECT_CODE, PLM.INTAKE_NAME,PLM.INTAKE_DESCRIPTION,PLM.DISCOUNT_ID, PP.CAMPAIGNCODE, PLM.IS_SYSTEM_DRIVEN, PLM.START_DATE, PP.EFFECTIVE_DATE, PLM.END_DATE, PP.EXPIRATION_DATE";
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
