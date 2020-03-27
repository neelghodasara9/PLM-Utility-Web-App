package com.java.plm.MyWebApp.controller;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;

public class PlmPinpointAudit {
	final static Logger logger = Logger.getLogger(PlmPinpointAudit.class);
	Connect_db connect_db = new Connect_db();
	Connection conPlm = connect_db.getPLM1Connection();
	Connection conPinpoint = connect_db.getPinpointConnection();
	Map<String, Map<String, Map<String,String>>> ppActiveOffers;
	Map<String, Map<String, Map<String,String>>> plmInactiveOffers;
	Map<String, Map<String, Map<String,String>>> plmActiveOffers;
	Map<String, String> compareMap;
	List<String> columnsToViewLst;


	void executePlmPpAudit(String columnsToCompare, String columnsToView, String materialImpactFutureWindow, String outFilePath) {
		ArrayList<String[]> materialImpactsForOrphansAudit = new ArrayList<String[]>();

		loadComparViewColumnsMap(columnsToCompare, columnsToView);
		fetchActivePpOffers();
		fetchPlmOffersCompare(materialImpactFutureWindow, outFilePath, materialImpactsForOrphansAudit);

		if(materialImpactsForOrphansAudit != null)
		{
			Collections.sort(materialImpactsForOrphansAudit,new Comparator<String[]>() {
				public int compare(String[] strings, String[] otherStrings) {
					return strings[0].compareTo(otherStrings[0]);

				};

			});
		}
		PlmController.materialImpactsForMail.put(this.getClass().getSimpleName(), materialImpactsForOrphansAudit);
	}

	void loadComparViewColumnsMap(String columnsToCompare, String columnsToView)
	{
		compareMap = new TreeMap<String, String>();
		columnsToViewLst = new ArrayList<String>();

		String[] cols = columnsToCompare.split("\\|");
		for(String key:cols) {
			String[] keyValue = key.split("=");
			compareMap.put(keyValue[0].trim(), keyValue[1].trim());
		}

		for(String key:columnsToView.split(",")) {
			if(key.trim().toUpperCase().startsWith("PP.") || key.trim().toUpperCase().startsWith("PLM.")) {
				columnsToViewLst.add(key.trim());
			}
			else
				logger.error("Invalid column name, should begin with either \"PLM.\" or \"PP.\" Ignoring this column name ---> " + key);
		}
	}

	void fetchActivePpOffers() 
	{
		try {
			ResultSet rsPpOfferset = null;
			ppActiveOffers = new TreeMap<String, Map<String, Map<String,String>>>();
			String sql = "select a.ADJUSTEDMRCMULTIPLIER, a.AGENTTYPELIST, a.ANYPAK, a.ANYPREMIUM, a.CAMPAIGNCODE, a.CAMPAIGNTYPE, a.CAMPAIGNTYPENUMERIC, a.CHANNELLIST, a.CHANNELSLABEL, a.CINEMAX, a.CSPP, a.CTS, a.DATAEQUIP, a.DATAEQUIPOPTIONAL, a.DATATIER, a.DATATIERNUMERIC, a.DISCOUNTTYPE, a.DVR, a.DVROPTIONAL, a.ELIGIBILITYRULES_CUSTOMER, a.ESTIMATEDDISCOUNTING, a.ESTIMATEDMRC, a.FINANCEBUCKETCODE, a.HBO, a.HOMELIFETIER, a.INSTALLATION, a.INSTALLINCLUDED, a.INSTALLNUMERIC, a.INTENTNUMERIC, a.ISACTIVE, a.LATINOPAK, a.MANDATORYFLAG, a.MANDATORYFLAGNUMERIC, a.MARGINBUCKET, a.MARGINBUCKETNUMERIC, a.MOVIEPAK, a.NAME, a.OFFERFAMILY, a.OFFERFAMILYNUMERIC, a.OFFERID, a.PHONETIER, a.PRISMCODELIST, a.PROACTIVE, a.PROGRAMNAME, a.PROMOCODES, a.MYDESCRIPTION, a.MYLABEL, a.MYLAUNCHMODE, a.MYGROUP, a.MYISSUE, a.MYNAME, a.MYTEMPLATEINPUTBOX, a.RECEIVER, a.REGIONCODELIST, a.RELEVANCYRULE, a.RETAILRATE, a.SAAMOUNT, a.SAREQUIRED, a.SHOWTIME, a.SIPAK, a.SPORTSPAK2, a.STABLEDURATION, a.STARTINGEVIDENCE, a.STARTINGPROPENSITY, a.STARZ, a.STATICACTIONURLCOMPARISONGROUP, a.STATICACTIONURLFEATURED, a.STATICACTIONURLPRIMARY, a.STATICIMAGEURLCOMPARISONGROUP, a.STATICIMAGEURLFEATURED, a.STATICIMAGEURLPRIMARY, a.STATICINTENTLIST, a.STATICMSGTXTCOMPARISONGROUP, a.STATICMSGTXTFEATURED, a.STATICMSGTXTPRIMARY, a.STATICPRODUCTLIST, a.STATICTHEMELIST, a.STATICTREATMENTLIST, a.STEPUPAMOUNT, a.TREATMENTIDENTIFIER, a.VARIETYPAK, a.VIDEOTIER, a.VIDEOTIERNUMERIC, a.PRISMRESTRICTED, a.PHONETIERPSU, a.PHONETIERPSULEVEL, a.VIDEOTIERPSU, a.VIDEOTIERVALUE, a.VIDEOTIERPSULEVEL, a.HOMELIFETIERPSULEVEL, a.HOMELIFETIERPSU, a.DATATIERPSU, a.DATATIERPSULEVEL, a.DATATIERVALUE, a.MARGINBUCKETADJUSTEDVALUE, a.CAMPAIGNTYPEVALUE, a.INSTALLVALUE, a.SAREQUIREDVALUE, a.OFFERFAMILYVALUE, a.MARGINBUCKETVALUE, a.STABLEDURATIONVALUE, a.TE_CODES, a.ADJUSTEDMRC, a.OMCID, a.ADDONTYPE, a.OFFERCATEGORY, a.PROPENSITYOVERRIDE, a.RELEVANCYRULES_WHEN, a.RELEVANCYRULES_PROPOSITION, a.RELEVANCYRULES_WHEN_POST, a.RELEVANCYRULES_PROPOSITION_POS, a.STATICSUBINTENTLIST, a.SUPPRESSACCEPTEDMAXOCCURENCES, a.SUPPRESSACCEPTEDDAYSBACK, a.DOWNWEIGHTPRESENTEDDAYSBACK, a.DOWNWEIGHTPRESENTEDMAXOCCURENC, a.BASEOFFERVIDEOTIER, a.BASEOFFERHOMELIFETIER, a.BASEOFFERDATATIER, a.BASEOFFERPHONETIER, a.CAMPAIGNCODEEXCLUDELIST, a.CAMPAIGNCODEINCLUDELIST, a.OFFERSCORE, a.QBEXTERNALID, a.ADDONSERVICE, a.ACTIONURL, a.ELIGIBILITYRULES_CUSTOMER2, a.COX_COMPLETE_CARE, a.SEGMENTID, a.STAGECODES, a.SERVICECATEGORIES, a.PROBLEMCODES, a.PRIORITYINDEX, a.ELIGIBLE_BILLING_TYPE, a.HOMELIFETIEREXCLUSION, a.DATATIERINCLUSION, a.DATATIEREXCLUSION, a.VIDEOTIERINCLUSION, a.VIDEOTIEREXCLUSION, a.PHONETIERINCLUSION, a.PHONETIEREXCLUSION, a.HOMELIFETIERINCLUSION, a.ADDONTYPELEVER, a.DATAINSTALLATION, a.EFFECTIVEFROM, a.EFFECTIVETO, a.HOMELIFEINSTALLATION, a.ISSEARCHONLY, a.ISSPECIALELIGIBILITY, a.NUMDAYSDWPRESENT, a.NUMDAYSSUPPACCEPT, a.NUMTIMESDWPRESENT, a.NUMTIMESSUPPACCEPT, a.OFFLINEPROPENSITYVALUE, a.PHONEINSTALLATION, a.SKIPOMCSYNC, a.ARCHIVALFLAG, a.ARCHIVALDELETEDATE, a.VIDEOEQUIPTYPE, a.VIDEOINSTALLATION, a.RELVN_RUL_PRIM_FRMLA, a.RELVN_RUL_SEC_FRMLA, a.ELGBL_RUL_PRIM_FRMLA, a.ELGBL_RUL_SEC_FRMLA, a.PYDESCRIPTION, a.LASTUPDATEDDATE, a.LOAD_DT, a.DATA_PLAN, a.ECONOMY_TV_PAK, a.RELEVANCYRULES_WHEN2, a.RELEVANCYRULES_WHEN_LOGIC, a.RELEVANCYRULES_WHEN2_LOGIC, a.ELIGIBILITYRULES_CUST_LOGIC, a.ELIGIBILITYRULES_CUST2_LOGIC, a.SAUPWEIGHTIDENTIFIER, a.SADOWNWEIGHTIDENTIFIER, a.DATATIERNEW, a.INSTALLATIONOPTIONAL, a.ISBASEOFFER, a.PHONETIERVALUE, a.HOMELIFETIERVALUE, a.VIDEO_EQUIPMENTS, a.VIDEO_SERVICES, a.DATA_SERVICES, a.PHONE_EQUIPMENTS, a.PHONE_SERVICES, a.HOMELIFE_EQUIPMENTS, a.HOMELIFE_SERVICES, a.DISCOUNT, a.DISCOUNTED_PRODUCTVAL, a.DISCOUNTS_EXCLUDEDVAL, a.DISCOUNTED_REQVAL, a.INSURANCE, a.PSUS, a.PREMIUMS, a.PAKS, a.INITIAL_REQUIRES, a.INITIAL_EXCLUDES, a.FINAL_REQUIRES, a.REQUIRES_ALL, a.REQUIRES_ANY, a.EXCLUDES_ALL, a.EXCLUDES_ANY, a.OFFER_VERSION, a.PROPOSITION_PRODUCTS, a.PROPOSITION_CAMPAIGNS, a.CHILD_PRODUCTS, a.SA_ID, a.FEATURE, a.INSTALLATION_TYPE, a.DEFAULT_SITE_ID, a.PRIMARY_CAMPAIGN, a.CAMPAIGNCODE_INCLUDE, a.INSTALL_PRODUCT_REQUIRESVALUE, a.INSTALL_PRODUCT_EXCLUDESVALUE, a.INSTALL_SERVICE_REQUIRES_ALL, a.INSTALL_SERVICE_REQUIRES_ANY, a.INSTALL_SERVICE_EXCLUDES_ALL, a.INSTALL_SERVICE_EXCLUDES_ANY, a.FINAL_SERVICE_REQUIRES_ALL, a.FINAL_SERVICE_REQUIRES_ANY, a.FINAL_SERVICE_EXCLUDES_ALL, a.FINAL_SERVICE_EXCLUDES_ANY, a.OFFERTYPE, a.PARENT_PRODUCTS, a.FINAL_EXCLUDES_PSU, a.FINAL_REQUIRES_PSU, a.PRIMARY_DURATION, a.SECONDARY_DURATION, a.OFFER_SERVICE_AGREEMENTS, a.DATE_BY_SITEID, a.SA_BILLINGCODE, a.SA_LEVEL, a.TERMS_AND_CONDITIONS, a.ETF_ASSESSMENT_AMOUNT, a.ETF_TRIAL_PERIOD, a.DATA_INCLUDE_PRODUCT, a.DATA_INCLUDE_BILLING_CODE, a.DATA_EXCLUDE_PRODUCT, a.DATA_EXCLUDE_BILLING_CODE, a.ALL_DATA_PRODUCTS, a.VIDEO_INCLUDE_PRODUCT, a.VIDEO_INCLUDE_BILLING_CODE, a.VIDEO_EXCLUDE_PRODUCT, a.VIDEO_EXCLUDE_BILLING_CODE, a.ALL_VIDEO_PRODUCTS, a.PHONE_INCLUDE_PRODUCT, a.PHONE_INCLUDE_BILLING_CODE, a.PHONE_EXCLUDE_PRODUCT, a.PHONE_EXCLUDE_BILLING_CODE, a.ALL_PHONE_PRODUCTS, a.HOMELIFE_INCLUDE_PRODUCT, a.HOMELIFE_INCLUDE_BILLING_CODE, a.HOMELIFE_EXCLUDE_PRODUCT, a.HOMELIFE_EXCLUDE_BILLING_CODE, a.ALL_HOMELIFE_PRODUCTS,listagg(b.SITE_ID,', ') within group (order by b.site_id) \"SITE_ID_VARIANCE\",b.EFFECTIVE_DATE,b.EXPIRATION_DATE,b.DRIVEN_BY from pega_data.oscar_proposition_nbas a left outer join pega_data.oscar_proposition_nbas_extn b on a.QBEXTERNALID=b.EXTERNAL_PROPOSITION_ID where load_dt = (select max(load_dt) from pega_data.oscar_proposition_nbas where OFFER_VERSION = 'V7') and OFFER_VERSION = 'V7' AND (trunc(EFFECTIVEFROM) <= trunc(sysdate) AND trunc(EFFECTIVETO)>= trunc(sysdate)) group by a.ADJUSTEDMRCMULTIPLIER, a.AGENTTYPELIST, a.ANYPAK, a.ANYPREMIUM, a.CAMPAIGNCODE, a.CAMPAIGNTYPE, a.CAMPAIGNTYPENUMERIC, a.CHANNELLIST, a.CHANNELSLABEL, a.CINEMAX, a.CSPP, a.CTS, a.DATAEQUIP, a.DATAEQUIPOPTIONAL, a.DATATIER, a.DATATIERNUMERIC, a.DISCOUNTTYPE, a.DVR, a.DVROPTIONAL, a.ELIGIBILITYRULES_CUSTOMER, a.ESTIMATEDDISCOUNTING, a.ESTIMATEDMRC, a.FINANCEBUCKETCODE, a.HBO, a.HOMELIFETIER, a.INSTALLATION, a.INSTALLINCLUDED, a.INSTALLNUMERIC, a.INTENTNUMERIC, a.ISACTIVE, a.LATINOPAK, a.MANDATORYFLAG, a.MANDATORYFLAGNUMERIC, a.MARGINBUCKET, a.MARGINBUCKETNUMERIC, a.MOVIEPAK, a.NAME, a.OFFERFAMILY, a.OFFERFAMILYNUMERIC, a.OFFERID, a.PHONETIER, a.PRISMCODELIST, a.PROACTIVE, a.PROGRAMNAME, a.PROMOCODES, a.MYDESCRIPTION, a.MYLABEL, a.MYLAUNCHMODE, a.MYGROUP, a.MYISSUE, a.MYNAME, a.MYTEMPLATEINPUTBOX, a.RECEIVER, a.REGIONCODELIST, a.RELEVANCYRULE, a.RETAILRATE, a.SAAMOUNT, a.SAREQUIRED, a.SHOWTIME, a.SIPAK, a.SPORTSPAK2, a.STABLEDURATION, a.STARTINGEVIDENCE, a.STARTINGPROPENSITY, a.STARZ, a.STATICACTIONURLCOMPARISONGROUP, a.STATICACTIONURLFEATURED, a.STATICACTIONURLPRIMARY, a.STATICIMAGEURLCOMPARISONGROUP, a.STATICIMAGEURLFEATURED, a.STATICIMAGEURLPRIMARY, a.STATICINTENTLIST, a.STATICMSGTXTCOMPARISONGROUP, a.STATICMSGTXTFEATURED, a.STATICMSGTXTPRIMARY, a.STATICPRODUCTLIST, a.STATICTHEMELIST, a.STATICTREATMENTLIST, a.STEPUPAMOUNT, a.TREATMENTIDENTIFIER, a.VARIETYPAK, a.VIDEOTIER, a.VIDEOTIERNUMERIC, a.PRISMRESTRICTED, a.PHONETIERPSU, a.PHONETIERPSULEVEL, a.VIDEOTIERPSU, a.VIDEOTIERVALUE, a.VIDEOTIERPSULEVEL, a.HOMELIFETIERPSULEVEL, a.HOMELIFETIERPSU, a.DATATIERPSU, a.DATATIERPSULEVEL, a.DATATIERVALUE, a.MARGINBUCKETADJUSTEDVALUE, a.CAMPAIGNTYPEVALUE, a.INSTALLVALUE, a.SAREQUIREDVALUE, a.OFFERFAMILYVALUE, a.MARGINBUCKETVALUE, a.STABLEDURATIONVALUE, a.TE_CODES, a.ADJUSTEDMRC, a.OMCID, a.ADDONTYPE, a.OFFERCATEGORY, a.PROPENSITYOVERRIDE, a.RELEVANCYRULES_WHEN, a.RELEVANCYRULES_PROPOSITION, a.RELEVANCYRULES_WHEN_POST, a.RELEVANCYRULES_PROPOSITION_POS, a.STATICSUBINTENTLIST, a.SUPPRESSACCEPTEDMAXOCCURENCES, a.SUPPRESSACCEPTEDDAYSBACK, a.DOWNWEIGHTPRESENTEDDAYSBACK, a.DOWNWEIGHTPRESENTEDMAXOCCURENC, a.BASEOFFERVIDEOTIER, a.BASEOFFERHOMELIFETIER, a.BASEOFFERDATATIER, a.BASEOFFERPHONETIER, a.CAMPAIGNCODEEXCLUDELIST, a.CAMPAIGNCODEINCLUDELIST, a.OFFERSCORE, a.QBEXTERNALID, a.ADDONSERVICE, a.ACTIONURL, a.ELIGIBILITYRULES_CUSTOMER2, a.COX_COMPLETE_CARE, a.SEGMENTID, a.STAGECODES, a.SERVICECATEGORIES, a.PROBLEMCODES, a.PRIORITYINDEX, a.ELIGIBLE_BILLING_TYPE, a.HOMELIFETIEREXCLUSION, a.DATATIERINCLUSION, a.DATATIEREXCLUSION, a.VIDEOTIERINCLUSION, a.VIDEOTIEREXCLUSION, a.PHONETIERINCLUSION, a.PHONETIEREXCLUSION, a.HOMELIFETIERINCLUSION, a.ADDONTYPELEVER, a.DATAINSTALLATION, a.EFFECTIVEFROM, a.EFFECTIVETO, a.HOMELIFEINSTALLATION, a.ISSEARCHONLY, a.ISSPECIALELIGIBILITY, a.NUMDAYSDWPRESENT, a.NUMDAYSSUPPACCEPT, a.NUMTIMESDWPRESENT, a.NUMTIMESSUPPACCEPT, a.OFFLINEPROPENSITYVALUE, a.PHONEINSTALLATION, a.SKIPOMCSYNC, a.ARCHIVALFLAG, a.ARCHIVALDELETEDATE, a.VIDEOEQUIPTYPE, a.VIDEOINSTALLATION, a.RELVN_RUL_PRIM_FRMLA, a.RELVN_RUL_SEC_FRMLA, a.ELGBL_RUL_PRIM_FRMLA, a.ELGBL_RUL_SEC_FRMLA, a.PYDESCRIPTION, a.LASTUPDATEDDATE, a.LOAD_DT, a.DATA_PLAN, a.ECONOMY_TV_PAK, a.RELEVANCYRULES_WHEN2, a.RELEVANCYRULES_WHEN_LOGIC, a.RELEVANCYRULES_WHEN2_LOGIC, a.ELIGIBILITYRULES_CUST_LOGIC, a.ELIGIBILITYRULES_CUST2_LOGIC, a.SAUPWEIGHTIDENTIFIER, a.SADOWNWEIGHTIDENTIFIER, a.DATATIERNEW, a.INSTALLATIONOPTIONAL, a.ISBASEOFFER, a.PHONETIERVALUE, a.HOMELIFETIERVALUE, a.VIDEO_EQUIPMENTS, a.VIDEO_SERVICES, a.DATA_SERVICES, a.PHONE_EQUIPMENTS, a.PHONE_SERVICES, a.HOMELIFE_EQUIPMENTS, a.HOMELIFE_SERVICES, a.DISCOUNT, a.DISCOUNTED_PRODUCTVAL, a.DISCOUNTS_EXCLUDEDVAL, a.DISCOUNTED_REQVAL, a.INSURANCE, a.PSUS, a.PREMIUMS, a.PAKS, a.INITIAL_REQUIRES, a.INITIAL_EXCLUDES, a.FINAL_REQUIRES, a.REQUIRES_ALL, a.REQUIRES_ANY, a.EXCLUDES_ALL, a.EXCLUDES_ANY, a.OFFER_VERSION, a.PROPOSITION_PRODUCTS, a.PROPOSITION_CAMPAIGNS, a.CHILD_PRODUCTS, a.SA_ID, a.FEATURE, a.INSTALLATION_TYPE, a.DEFAULT_SITE_ID, a.PRIMARY_CAMPAIGN, a.CAMPAIGNCODE_INCLUDE, a.INSTALL_PRODUCT_REQUIRESVALUE, a.INSTALL_PRODUCT_EXCLUDESVALUE, a.INSTALL_SERVICE_REQUIRES_ALL, a.INSTALL_SERVICE_REQUIRES_ANY, a.INSTALL_SERVICE_EXCLUDES_ALL, a.INSTALL_SERVICE_EXCLUDES_ANY, a.FINAL_SERVICE_REQUIRES_ALL, a.FINAL_SERVICE_REQUIRES_ANY, a.FINAL_SERVICE_EXCLUDES_ALL, a.FINAL_SERVICE_EXCLUDES_ANY, a.OFFERTYPE, a.PARENT_PRODUCTS, a.FINAL_EXCLUDES_PSU, a.FINAL_REQUIRES_PSU, a.PRIMARY_DURATION, a.SECONDARY_DURATION, a.OFFER_SERVICE_AGREEMENTS, a.DATE_BY_SITEID, a.SA_BILLINGCODE, a.SA_LEVEL, a.TERMS_AND_CONDITIONS, a.ETF_ASSESSMENT_AMOUNT, a.ETF_TRIAL_PERIOD, a.DATA_INCLUDE_PRODUCT, a.DATA_INCLUDE_BILLING_CODE, a.DATA_EXCLUDE_PRODUCT, a.DATA_EXCLUDE_BILLING_CODE, a.ALL_DATA_PRODUCTS, a.VIDEO_INCLUDE_PRODUCT, a.VIDEO_INCLUDE_BILLING_CODE, a.VIDEO_EXCLUDE_PRODUCT, a.VIDEO_EXCLUDE_BILLING_CODE, a.ALL_VIDEO_PRODUCTS, a.PHONE_INCLUDE_PRODUCT, a.PHONE_INCLUDE_BILLING_CODE, a.PHONE_EXCLUDE_PRODUCT, a.PHONE_EXCLUDE_BILLING_CODE, a.ALL_PHONE_PRODUCTS, a.HOMELIFE_INCLUDE_PRODUCT, a.HOMELIFE_INCLUDE_BILLING_CODE, a.HOMELIFE_EXCLUDE_PRODUCT, a.HOMELIFE_EXCLUDE_BILLING_CODE, a.ALL_HOMELIFE_PRODUCTS,b.EFFECTIVE_DATE,b.EXPIRATION_DATE,b.DRIVEN_BY";
			logger.info("SQL for active offers from PP ---> " + sql);
			PreparedStatement pstmt = conPinpoint.prepareStatement(sql);
			rsPpOfferset = pstmt.executeQuery();
			logger.info("Loading active offers from PP");

			if (rsPpOfferset.next() == false) {
				logger.info("No active offers found in Pinpoint DB!");
			} 
			else {
				do {
					Map<String, Map<String, String>> tempOfferMap;
					String propId = rsPpOfferset.getString("QBEXTERNALID");
					String siteIdVar = rsPpOfferset.getString("site_id_variance");
					//if(propId.equals("O1472_01"))
					//System.out.println(propId);

					if(siteIdVar == null) {
						logger.info("No offer - site variance found in PP NBA Extn table for "+ propId + " - " + siteIdVar);
						continue;
					}
					else if(ppActiveOffers.containsKey(propId)) {
						if(ppActiveOffers.containsKey(siteIdVar)) {
							logger.error("Duplicate offer-site id variance entry found in PP NBA Extn table. Ignoring this entry ---> "+ propId + " - " + siteIdVar);
							continue;
						}
						else {
							tempOfferMap = ppActiveOffers.get(propId);
						}
					}
					else {
						tempOfferMap = new TreeMap<String, Map<String, String>>();
					}

					Map<String, String> tempMap = new TreeMap<String, String>();
					for(String key:compareMap.keySet()) {
						if(rsPpOfferset.getString(compareMap.get(key)) == null)
							tempMap.put(key, "");
						else {
							String ppData = rsPpOfferset.getString(compareMap.get(key));
							if(key.toUpperCase().contains("DATE")) {
								String ppDt = dateFormatter(ppData.substring(0,4) + "-" + ppData.substring(5,7) + "-" + ppData.substring(8,10), "yyyy-MM-dd", "dd-MMM-yy");
								ppData = ppDt;
							}
							tempMap.put(key, ppData);
						}
					}

					for(String key:columnsToViewLst) {
						if(key.toUpperCase().startsWith("PP.")) {
							String colName = key.split("PP.")[1];
							if(rsPpOfferset.getString(colName) == null)
								tempMap.put(key, "");
							else {
								String ppData = rsPpOfferset.getString(colName);
								if(key.toUpperCase().contains("DATE")) {
									String ppDt = dateFormatter(ppData.substring(0,4) + "-" + ppData.substring(5,7) + "-" + ppData.substring(8,10), "yyyy-MM-dd", "dd-MMM-yy");
									ppData = ppDt;
								}
								tempMap.put(key, ppData);
							}
						}
					}
					tempOfferMap.put(siteIdVar, tempMap);
					ppActiveOffers.put(propId, tempOfferMap);
				} while (rsPpOfferset.next());
			}
		}
		catch (Exception e) {
			logger.error("Error in fetchActivePpOffers() method: "+e.getMessage());
		}
	}

	String dateFormatter(String inDate, String inPattern, String outPattern) 
	{
		String outDate = null;
		try {
			SimpleDateFormat inDateFormatter = new SimpleDateFormat(inPattern);
			Date date;
			date = inDateFormatter.parse(inDate);
			SimpleDateFormat outDateFormatter = new SimpleDateFormat(outPattern);
			outDate = outDateFormatter.format(date);
		} catch (ParseException e) {
			logger.error("Error in dateFormatter() method: "+e.getMessage());
		}
		return outDate;
	}

	Boolean lookForSortedMatch(String textToCompare1, String textToCompare2, String sep) 
	{
		if(textToCompare1.equals(textToCompare2))
			return true;

		Set<String> setTextToCompare1 = new TreeSet<String>();
		Set<String> setTextToCompare2 = new TreeSet<String>();

		String[] arrTextToCompare1 = textToCompare1.split(sep);
		String[] arrTextToCompare2 = textToCompare2.split(sep);

		if(arrTextToCompare1.length != arrTextToCompare2.length) {
			return false;	
		}

		for(String s:arrTextToCompare1)
			setTextToCompare1.add(s.trim());

		for(String s:arrTextToCompare2)
			setTextToCompare2.add(s.trim());

		if(setTextToCompare1.size() == setTextToCompare2.size()) {
			if(setTextToCompare1.toString().equals(setTextToCompare2.toString())) {
				return true;
			}
		}
		return false;
	}

	void fetchPlmOffersCompare(String materialImpactFutureWindow, String outFilePath, ArrayList<String[]> materialImpactsForOrphansAudit) {
		try {
			Map<String, Map<String,String>> offerDistroHistory = new TreeMap<String, Map<String,String>>();
			Set<String> discrepancyOfferList = new LinkedHashSet<>();
			List<String[]> discrepancyList = new LinkedList<String[]>();
			Map<String, Map<String, String>> tempOfferMap;
			plmInactiveOffers = new TreeMap<String, Map<String, Map<String,String>>>();
			plmActiveOffers = new TreeMap<String, Map<String, Map<String,String>>>();
			Date date = new Date();
			SimpleDateFormat outDateFormatter = new SimpleDateFormat("dd-MMM-yyyy");
			String today = outDateFormatter.format(date);
			FileWriter outputfile;

			outputfile = new FileWriter(new File(outFilePath));
			CSVWriter writer = new CSVWriter(outputfile); 
			Map<String, String> tempMapPP = new TreeMap<String, String>();
			Map<String, Map<String, String>> tempOfferMapPP;
			ResultSet rsOffers = null;

			Integer intMaterialImapctFutureWindow = 0;
			try {
				intMaterialImapctFutureWindow = Math.abs((Integer.parseInt(materialImpactFutureWindow.trim())));
			}
			catch(Exception e){
			}

			List<String> tempLst = new ArrayList<String>();
			tempLst.add("EXTERNAL_ID");
			tempLst.add("PLM_SITE_ID_VARIANCE");
			tempLst.add("DISCREPANCY");
			tempLst.addAll(columnsToViewLst);
			tempLst.add("isMaterialImpact?");
			tempLst.add("DISTRIBUTION_ID");
			tempLst.add("DISTRIBUTION_ENVIRONMENT");
			tempLst.add("DISTRIBUTION_DESC");
			tempLst.add("DATE/TIME");
			tempLst.add("CEC_5.X");

			writer.writeNext(tempLst.stream().toArray(String[]::new)); 
			PlmController.csvFileHeaders.put(this.getClass().getSimpleName(), tempLst.stream().toArray(String[]::new));

			//load inactive plm offers into map
			String sqlInactiveOffers = "select c.OFFER_ID, c.NAME, c.TYPE, c.DESCRIPTION, c.BUNDLE, c.STATUS, c.VERSION, c.CREATED_DATE, c.SALES_ADVISE, c.PRICE, c.PSU, c.RELEASE_ID, c.IS_INACTIVE, c.IS_DEFAULT_OFFER, c.STATUS_COMMENTS, c.PROJECT_CODE, x.INTAKE_REQ_NAME \"INTAKE_NAME\",x.DESCRIPTION \"INTAKE_DESCRIPTION\", c.RELEASE_DATE, c.CATAGORY, c.WEB_DISPLAY_NAME, c.MODIFIED_BY, c.CREATED_BY, c.REL_RULE_PRI_FORMULA, c.REL_RULE_SEC_FORMULA, c.ELIG_RUL_PRI_FORMULA, c.ELIG_RUL_SEC_FORMULA, c.ERROR_DESC, c.UAT_COMMENT, c.CHANGE_REMARKS, c.COPIED_FROM_OFFER_ID, c.OMC_TEST_STATUS, c.OMC_PROD_STATUS, c.PINPNT_TEST_STATUS, c.PINPNT_PROD_STATUS, c.ECOM_OFFER_ID, c.OFFER_RACK_RATE, c.PRPOSTN_ID, c.DISTRO_PREFERENCE, c.PROPOSITION_OFFER_NAME, c.EXTERNAL_ID, c.BILLING_ELIG, c.REL_RULE_PRI_FORMULA_V5, c.REL_RULE_SEC_FORMULA_V5, c.ELIG_RUL_PRI_FORMULA_V5, c.ELIG_RUL_SEC_FORMULA_V5, c.PROPOSITION5_ID, c.SERVICE_AGREEMENTS, c.IS_EXPIRED, c.IS_PRPOSTN_SITE_VARIATION, c.IS_SYSTEM_DRIVEN,listagg(d.site_id,', ') within group (order by d.site_id) \"site_id_variance\",d.start_date,d.end_date,a.discount_id,b.discount_code from plm_dbo.offer_master_txn_det c left outer join plm_dbo.offer_site_map d on c.offer_id=d.offer_id left outer join plm_dbo.OFFER_DISC_MAP_TXN_DET a on c.offer_id=a.offer_id left outer join plm_dbo.DISCOUNT_TXN_MASTER b on a.discount_id=b.discount_id and a.PRIMARY_FLAG='Y' left outer join plm_dbo.project_master_txn_det x on c.PROJECT_CODE=x.PROJECT_CODE where c.DISTRO_PREFERENCE ='5.0' and c.prpostn_id is not null and c.offer_id not in(select distinct c.offer_id from plm_dbo.offer_site_map c where c.start_date <= 'TODAY' and c.end_date >= 'TODAY') group by c.OFFER_ID, c.NAME, c.TYPE, c.DESCRIPTION, c.BUNDLE, c.STATUS, c.VERSION, c.CREATED_DATE, c.SALES_ADVISE, c.PRICE, c.PSU, c.RELEASE_ID, c.IS_INACTIVE, c.IS_DEFAULT_OFFER, c.STATUS_COMMENTS, c.PROJECT_CODE,x.INTAKE_REQ_NAME,x.DESCRIPTION, c.RELEASE_DATE, c.CATAGORY, c.WEB_DISPLAY_NAME, c.MODIFIED_BY, c.CREATED_BY, c.REL_RULE_PRI_FORMULA, c.REL_RULE_SEC_FORMULA, c.ELIG_RUL_PRI_FORMULA, c.ELIG_RUL_SEC_FORMULA, c.ERROR_DESC, c.UAT_COMMENT, c.CHANGE_REMARKS, c.COPIED_FROM_OFFER_ID, c.OMC_TEST_STATUS, c.OMC_PROD_STATUS, c.PINPNT_TEST_STATUS, c.PINPNT_PROD_STATUS, c.ECOM_OFFER_ID, c.OFFER_RACK_RATE, c.PRPOSTN_ID, c.DISTRO_PREFERENCE, c.PROPOSITION_OFFER_NAME, c.EXTERNAL_ID, c.BILLING_ELIG, c.REL_RULE_PRI_FORMULA_V5, c.REL_RULE_SEC_FORMULA_V5, c.ELIG_RUL_PRI_FORMULA_V5, c.ELIG_RUL_SEC_FORMULA_V5, c.PROPOSITION5_ID, c.SERVICE_AGREEMENTS, c.IS_EXPIRED, c.IS_PRPOSTN_SITE_VARIATION, c.IS_SYSTEM_DRIVEN,d.start_date,d.end_date,a.discount_id,b.discount_code";
			sqlInactiveOffers = sqlInactiveOffers.replace("TODAY", today);
			logger.info("SQL for inactive offers from PLM ---> " + sqlInactiveOffers.toString());
			PreparedStatement stmtOffers = conPlm.prepareStatement(sqlInactiveOffers);
			rsOffers = stmtOffers.executeQuery();
			logger.info("Loading inactive offers from PLM");

			if (rsOffers.next() == false) {
				logger.info("No inactive offers present in PLM DB!");
			} 
			else {
				do {
					String propId = rsOffers.getString("PRPOSTN_ID");
					String siteIdVar = rsOffers.getString("site_id_variance");
					//if(propId.equals("O1472_01"))
					//System.out.println(propId);

					if(siteIdVar == null) {
						logger.info("No offer - site variance found in PLM offer_site_map table for "+ propId + " - " + siteIdVar);
						continue;
					}
					else if(plmInactiveOffers.containsKey(propId)) {
						if(plmInactiveOffers.containsKey(siteIdVar)) {
							logger.error("Duplicate offer-site id variance entry found in PLM offer_site_map table. Ignoring this entry ---> "+ propId + " - " + siteIdVar);
							continue;
						}
						else {
							tempOfferMap = plmInactiveOffers.get(propId);
						}
					}
					else {
						tempOfferMap = new TreeMap<String, Map<String, String>>();
					}

					Map<String, String> tempMap = new TreeMap<String, String>();
					for(String key:compareMap.keySet()) {
						if(rsOffers.getString(key) == null)
							tempMap.put(key, "");
						else {
							String plmData = rsOffers.getString(key);
							if(key.toUpperCase().contains("DATE")) {
								String plmDt = dateFormatter(plmData.substring(0,4) + "-" + plmData.substring(5,7) + "-" + plmData.substring(8,10), "yyyy-MM-dd", "dd-MMM-yy");
								plmData = plmDt;
							}
							tempMap.put(key, plmData);
						}
					}

					for(String key:columnsToViewLst) {
						if(key.toUpperCase().startsWith("PLM.")) {
							String colName = key.split("PLM.")[1];
							if(rsOffers.getString(colName) == null)
								tempMap.put(key, "");
							else {
								String plmData = rsOffers.getString(colName);
								if(key.toUpperCase().contains("DATE")) {
									String plmDt = dateFormatter(plmData.substring(0,4) + "-" + plmData.substring(5,7) + "-" + plmData.substring(8,10), "yyyy-MM-dd", "dd-MMM-yy");
									plmData = plmDt;
								}
								tempMap.put(key, plmData);
							}
						}
					}
					tempOfferMap.put(siteIdVar, tempMap);
					plmInactiveOffers.put(propId, tempOfferMap);
				} while (rsOffers.next());
			}

			//load active plm offers into map

			rsOffers = null;
			String sql = "select c.OFFER_ID, c.NAME, c.TYPE, c.DESCRIPTION, c.BUNDLE, c.STATUS, c.VERSION, c.CREATED_DATE, c.SALES_ADVISE, c.PRICE, c.PSU, c.RELEASE_ID, c.IS_INACTIVE, c.IS_DEFAULT_OFFER, c.STATUS_COMMENTS, c.PROJECT_CODE, x.INTAKE_REQ_NAME \"INTAKE_NAME\",x.DESCRIPTION \"INTAKE_DESCRIPTION\", c.RELEASE_DATE, c.CATAGORY, c.WEB_DISPLAY_NAME, c.MODIFIED_BY, c.CREATED_BY, c.REL_RULE_PRI_FORMULA, c.REL_RULE_SEC_FORMULA, c.ELIG_RUL_PRI_FORMULA, c.ELIG_RUL_SEC_FORMULA, c.ERROR_DESC, c.UAT_COMMENT, c.CHANGE_REMARKS, c.COPIED_FROM_OFFER_ID, c.OMC_TEST_STATUS, c.OMC_PROD_STATUS, c.PINPNT_TEST_STATUS, c.PINPNT_PROD_STATUS, c.ECOM_OFFER_ID, c.OFFER_RACK_RATE, c.PRPOSTN_ID, c.DISTRO_PREFERENCE, c.PROPOSITION_OFFER_NAME, c.EXTERNAL_ID, c.BILLING_ELIG, c.REL_RULE_PRI_FORMULA_V5, c.REL_RULE_SEC_FORMULA_V5, c.ELIG_RUL_PRI_FORMULA_V5, c.ELIG_RUL_SEC_FORMULA_V5, c.PROPOSITION5_ID, c.SERVICE_AGREEMENTS, c.IS_EXPIRED, c.IS_PRPOSTN_SITE_VARIATION, c.IS_SYSTEM_DRIVEN,( select LISTAGG(a.site_id ,', ')  WITHIN GROUP(ORDER BY a.site_id) AS site_codeid FROM plm_dbo.offer_site_map a WHERE a.offer_id = c.offer_id and a.start_date=d.start_date and a.end_date=d.end_date GROUP BY a.offer_id ) as \"site_id_variance\",d.start_date,d.end_date,( select LISTAGG(discount_id ,',')  WITHIN GROUP(ORDER BY discount_id) AS discount_id FROM plm_dbo.OFFER_DISC_MAP_TXN_DET a WHERE a.offer_id = c.offer_id GROUP BY a.offer_id ) as discount_id,( select LISTAGG(discount_code ,',')  WITHIN GROUP(ORDER BY discount_code) AS discount_code FROM plm_dbo.OFFER_DISC_MAP_TXN_DET ac,plm_dbo.discount_txn_master b WHERE ac.discount_id = b.discount_id and c.offer_id = ac.offer_id GROUP BY ac.offer_id ) as discount_code from plm_dbo.offer_master_txn_det c left outer join plm_dbo.offer_site_map d on c.offer_id=d.offer_id left outer join plm_dbo.OFFER_DISC_MAP_TXN_DET a on c.offer_id=a.offer_id left outer join plm_dbo.project_master_txn_det x on c.PROJECT_CODE=x.PROJECT_CODE where c.DISTRO_PREFERENCE ='5.0' and c.prpostn_id is not null and c.offer_id in(select distinct c.offer_id from plm_dbo.offer_site_map c where c.start_date <= 'TODAY' and c.end_date >= 'TODAY') group by c.OFFER_ID, c.NAME, c.TYPE, c.DESCRIPTION, c.BUNDLE, c.STATUS, c.VERSION, c.CREATED_DATE, c.SALES_ADVISE, c.PRICE, c.PSU, c.RELEASE_ID, c.IS_INACTIVE, c.IS_DEFAULT_OFFER, c.STATUS_COMMENTS, c.PROJECT_CODE,x.INTAKE_REQ_NAME,x.DESCRIPTION, c.RELEASE_DATE, c.CATAGORY, c.WEB_DISPLAY_NAME, c.MODIFIED_BY, c.CREATED_BY, c.REL_RULE_PRI_FORMULA, c.REL_RULE_SEC_FORMULA, c.ELIG_RUL_PRI_FORMULA, c.ELIG_RUL_SEC_FORMULA, c.ERROR_DESC, c.UAT_COMMENT, c.CHANGE_REMARKS, c.COPIED_FROM_OFFER_ID, c.OMC_TEST_STATUS, c.OMC_PROD_STATUS, c.PINPNT_TEST_STATUS, c.PINPNT_PROD_STATUS, c.ECOM_OFFER_ID, c.OFFER_RACK_RATE, c.PRPOSTN_ID, c.DISTRO_PREFERENCE, c.PROPOSITION_OFFER_NAME, c.EXTERNAL_ID, c.BILLING_ELIG, c.REL_RULE_PRI_FORMULA_V5, c.REL_RULE_SEC_FORMULA_V5, c.ELIG_RUL_PRI_FORMULA_V5, c.ELIG_RUL_SEC_FORMULA_V5, c.PROPOSITION5_ID, c.SERVICE_AGREEMENTS, c.IS_EXPIRED, c.IS_PRPOSTN_SITE_VARIATION, c.IS_SYSTEM_DRIVEN,d.start_date,d.end_date";
			sql = sql.replace("TODAY", today);

			logger.info("SQL for active offers from PLM ---> " + sql.toString());
			stmtOffers = conPlm.prepareStatement(sql);
			rsOffers = stmtOffers.executeQuery();
			logger.info("Loading active offers from PLM");

			if (rsOffers.next() == false) {
				logger.info("No active offers present in PLM DB!");
			} 
			else {
				do {
					String siteIdVar = rsOffers.getString("site_id_variance");
					String propId = rsOffers.getString("PRPOSTN_ID");
					Map<String, String> tempMap = new TreeMap<String, String>();

					//double check if this prop/ prop-site id combo isnt already existing, if so load tempOfferMap accordingly
					if(siteIdVar == null) {
						logger.info("No offer - site variance found in PLM offer_site_map table for "+ propId + " - " + siteIdVar);
						continue;
					}
					else if(plmActiveOffers.containsKey(propId)) {
						if(plmActiveOffers.containsKey(siteIdVar)) {
							logger.error("Duplicate offer-site id variance entry found in PLM offer_site_map table. Ignoring this entry ---> "+ propId + " - " + siteIdVar);
							continue;
						}
						else {
							tempOfferMap = plmActiveOffers.get(propId);
						}
					}
					else {
						tempOfferMap = new TreeMap<String, Map<String, String>>();
					}

					for(String key:compareMap.keySet()) {
						String plmData = null;
						if(key.contains("DISCOUNT_CODE")){
							StringBuilder tempData = new StringBuilder();
							String temp = rsOffers.getString(key);
							if(temp!=null)
								tempData.append(temp);

							if(tempData.length()>0 && rsOffers.getString("DISCOUNT_id") != null)
								tempData.append(",");

							temp = rsOffers.getString("DISCOUNT_id");
							if(temp!=null)
								tempData.append(temp);
							plmData = tempData.toString();
						}
						else
							plmData = rsOffers.getString(key);

						if(plmData == null) {
							tempMap.put(key, "");
							plmData = "";
						}

						else {
							if(key.toUpperCase().contains("DATE")) {
								String plmDt = dateFormatter(plmData.substring(0,4) + "-" + plmData.substring(5,7) + "-" + plmData.substring(8,10), "yyyy-MM-dd", "dd-MMM-yy");
								plmData = plmDt;
							}
							tempMap.put(key, plmData);
						}
					}

					for(String key:columnsToViewLst) {
						if(key.toUpperCase().startsWith("PLM.")) {
							String colName = key.split("PLM.")[1];
							if(rsOffers.getString(colName) == null)
								tempMap.put(key, "");
							else {
								String plmData = rsOffers.getString(colName);
								if(key.toUpperCase().contains("DATE")) {
									String plmDt = dateFormatter(plmData.substring(0,4) + "-" + plmData.substring(5,7) + "-" + plmData.substring(8,10), "yyyy-MM-dd", "dd-MMM-yy");
									plmData = plmDt;
								}
								tempMap.put(key, plmData);
							}
						}
					}
					tempOfferMap.put(siteIdVar, tempMap);
					plmActiveOffers.put(propId, tempOfferMap);
				} while (rsOffers.next());
			}


			/////////////////////////////////////////////////////////////////////////////////////////////////
			//comparison begins; iterating plmActiveOffers followed by ppActiveOffers map
			logger.info("Comparison begins; iterating plmActiveOffers");

			for (String propId : plmActiveOffers.keySet()) {
				for (String siteIdVar : plmActiveOffers.get(propId).keySet()) {

					if(ppActiveOffers.containsKey(propId)) {
						StringBuilder correctionStr = new StringBuilder();
						tempOfferMapPP = ppActiveOffers.get(propId);

						if(tempOfferMapPP.containsKey(siteIdVar)) {
							tempMapPP = tempOfferMapPP.get(siteIdVar);
							Map<String, String> tempMapPlm = plmActiveOffers.get(propId).get(siteIdVar);

							Boolean isMaterialImpact = false;
							for(String key:compareMap.keySet()) {
								String plmData = tempMapPlm.get(key);

								//actual comparing data key values
								if(!tempMapPP.get(key).equals(plmData)) {
									if(plmData.contains(",") || tempMapPP.get(key).contains(","))
										if(lookForSortedMatch(plmData, tempMapPP.get(key), ",")) 
											continue;	

									String discrepancy = key + "- PLM=" + plmData + "; PP=" + tempMapPP.get(key) + "| ";
									if(correctionStr.length()==0)
										correctionStr.append(discrepancy);
									else
										correctionStr.append(", "+discrepancy);

									if(key.toUpperCase().contains("DATE")) {
										if(materialImpactDateRange(plmData, intMaterialImapctFutureWindow) || materialImpactDateRange(tempMapPP.get(key), intMaterialImapctFutureWindow)) {
											isMaterialImpact = true ;
										}
									}
									else
										isMaterialImpact = true ;
								}
							}

							if(correctionStr.length()>0)	{

								//create dynamic discrepancy row by columns
								tempLst = new ArrayList<String>();
								tempLst.add(propId);
								tempLst.add(siteIdVar);
								tempLst.add(correctionStr.toString());

								for(String key:columnsToViewLst) {
									if(key.toUpperCase().startsWith("PP.")) {
										tempLst.add(tempMapPP.get(key));
									}
									else if(key.toUpperCase().startsWith("PLM.")) {
										tempLst.add(tempMapPlm.get(key));
									}
								}
								//writer.writeNext(tempLst.stream().toArray(String[]::new));
								tempLst.add(isMaterialImpact.toString().toUpperCase());

								discrepancyList.add(tempLst.stream().toArray(String[]::new));
								discrepancyOfferList.add("'" + propId + "'");
							}
						}
						else {
							tempLst = new ArrayList<String>();
							tempLst.add(propId);
							tempLst.add(siteIdVar);
							tempLst.add("Proposition active in PLM but misaligned or not found in Pinpoint for this site group");

							for(String key:columnsToViewLst) {
								if(key.toUpperCase().startsWith("PP.")) {
									tempLst.add("");
								}
								else if(key.toUpperCase().startsWith("PLM.")) {
									tempLst.add(plmActiveOffers.get(propId).get(siteIdVar).get(key));
								}
							}
							//writer.writeNext(tempLst.stream().toArray(String[]::new)); 
							tempLst.add("TRUE");

							discrepancyList.add(tempLst.stream().toArray(String[]::new));
							discrepancyOfferList.add("'" + propId + "'");
						}
					}
					else
					{
						tempLst = new ArrayList<String>();

						tempLst.add(propId);
						tempLst.add(siteIdVar);
						tempLst.add("Proposition active in PLM but expired or not found in Pinpoint");

						for(String key:columnsToViewLst) {
							if(key.toUpperCase().startsWith("PP.")) {
								tempLst.add("");
							}
							else if(key.toUpperCase().startsWith("PLM.")) {
								tempLst.add(plmActiveOffers.get(propId).get(siteIdVar).get(key));
							}
						}
						//writer.writeNext(tempLst.stream().toArray(String[]::new)); 
						tempLst.add("TRUE");

						discrepancyList.add(tempLst.stream().toArray(String[]::new));
						discrepancyOfferList.add("'" + propId + "'");
					}
				}
			}

			/////////////////////////////////////////////////////////////////////////////////////////
			//comparison continues; iterating ppActiveOffers map

			logger.info("Comparison continues; iterating ppActiveOffers map");
			for (String keyOffer : ppActiveOffers.keySet()) {
				tempLst = new ArrayList<String>();
				tempLst.add(keyOffer);
				//logger.info("keyOffer ---> " + keyOffer);

				if(plmActiveOffers.containsKey(keyOffer)) {

					for(String siteIdVar : ppActiveOffers.get(keyOffer).keySet()) {
						if(!plmActiveOffers.get(keyOffer).containsKey(siteIdVar)) {
							tempLst.add(siteIdVar);
							tempLst.add("Proposition active in Pinpoint but misaligned or not found in PLM for this site group");

							for(String col:columnsToViewLst) {
								if(col.toUpperCase().startsWith("PP.")) {
									tempLst.add(ppActiveOffers.get(keyOffer).get(siteIdVar).get(col));
								}
								else if(col.toUpperCase().startsWith("PLM.")) {
									tempLst.add("");
								}
							}
							//writer.writeNext(tempLst.stream().toArray(String[]::new));
							tempLst.add("TRUE");

							discrepancyList.add(tempLst.stream().toArray(String[]::new));
							discrepancyOfferList.add("'" + keyOffer + "'");
							tempLst = new ArrayList<String>();
							tempLst.add(keyOffer);
						}						
					}
				}
				else if(plmInactiveOffers.containsKey(keyOffer)) {

					for(String siteIdVar : ppActiveOffers.get(keyOffer).keySet()) {

						tempLst.add(siteIdVar);
						tempLst.add("Proposition active in Pinpoint but expired in PLM");

						for(String col:columnsToViewLst) {
							if(col.toUpperCase().startsWith("PP.")) {
								tempLst.add(ppActiveOffers.get(keyOffer).get(siteIdVar).get(col));
							}
							else if(col.toUpperCase().startsWith("PLM.")) {
								if(plmInactiveOffers.get(keyOffer).containsKey(siteIdVar))
									if(plmInactiveOffers.get(keyOffer).get(siteIdVar).containsKey(col))
										tempLst.add(plmInactiveOffers.get(keyOffer).get(siteIdVar).get(col));
									else
										tempLst.add("");
								else
									tempLst.add("");
							}
						}
						//writer.writeNext(tempLst.stream().toArray(String[]::new));
						tempLst.add("TRUE");

						discrepancyList.add(tempLst.stream().toArray(String[]::new));
						discrepancyOfferList.add("'" + keyOffer + "'");
						tempLst = new ArrayList<String>();
						tempLst.add(keyOffer);
					}
				}
				else {
					for(String siteIdVar : ppActiveOffers.get(keyOffer).keySet()) {
						tempLst.add(siteIdVar);
						tempLst.add("Proposition active in Pinpoint but not found in PLM");

						for(String col:columnsToViewLst) {
							if(col.toUpperCase().startsWith("PP.")) {
								tempLst.add(ppActiveOffers.get(keyOffer).get(siteIdVar).get(col));
							}
							else if(col.toUpperCase().startsWith("PLM.")) {
								tempLst.add("");
							}
						}
						//writer.writeNext(tempLst.stream().toArray(String[]::new));
						tempLst.add("TRUE");

						discrepancyList.add(tempLst.stream().toArray(String[]::new));
						discrepancyOfferList.add("'" + keyOffer + "'");
						tempLst = new ArrayList<String>();
						tempLst.add(keyOffer);
					}
				}

				/*if(tempLst.size()>1) {
					//writer.writeNext(tempLst.stream().toArray(String[]::new));
					tempLst.add("TRUE");

					discrepancyList.add(tempLst.stream().toArray(String[]::new));
					discrepancyOfferList.add("'" + keyOffer + "'");
				}*/
			}

			if(discrepancyOfferList.size()<1000) {
				Map<String, String> tempMap;
				ResultSet rsLastDistro = null;
				sql = "select distinct DISTRIBUTION_ID, DISTRIBUTION_ENVIRONMENT,DISTRIBUTION_DESC,\"DISTRIBUTION DATE/TIME\", (select LAST_ERROR from plm_dbo.self_dist_offer_txn_det g  where t.\"DISTRIBUTION DATE/TIME\" = g.CREATED_DATE and t.offer_id=g.offer_id and g.endpoint='CEC_4.X') \"CEC_4.X\", (select LAST_ERROR from plm_dbo.self_dist_offer_txn_det g  where t.\"DISTRIBUTION DATE/TIME\" = g.CREATED_DATE and t.offer_id=g.offer_id  and g.endpoint='CEC_5.X') \"CEC_5.X\"	, EXTERNAL_ID from(select s1.DISTRIBUTION_ID,s1.LAST_ERROR, c.ENV_NAME \"DISTRIBUTION_ENVIRONMENT\", b.DESCRIPTION \"DISTRIBUTION_DESC\",s1.MODIFIED_DATE  \"DISTRIBUTION DATE/TIME\",d.external_id,d.offer_id from plm_dbo.self_dist_offer_txn_det s1 inner join (select max(x.MODIFIED_DATE) MODIFIED_DATE, external_id,offer_id from plm_dbo.self_dist_offer_txn_det x, plm_dbo.SELF_DISTRUBUTION_TXN_DET y where x.DISTRIBUTION_ID = y.DISTRIBUTION_ID and y.env_id=2 and  x.offer_id IN (select distinct offer_id from plm_dbo.OFFER_MASTER_TXN_DET where external_id in(OFFER_DISCREPANCY_LIST)) group by offer_id,external_id ) s2 on  s1.MODIFIED_DATE = s2.MODIFIED_DATE  and s2.offer_id =s1.offer_id inner join plm_dbo.SELF_DISTRUBUTION_TXN_DET b on s1.DISTRIBUTION_ID = b.DISTRIBUTION_ID inner join plm_dbo.ENV_MGMT_TXN_MASTER c on b.ENV_ID=c.ENV_ID inner join plm_dbo.offer_master_txn_det d on s2.offer_id = d.offer_id order by s1.external_id,s1.modified_date desc)t order by external_id";
				sql = sql.replace("OFFER_DISCREPANCY_LIST", discrepancyOfferList.toString().replace("[","").replace("]", ""));

				logger.info("SQL for pulling out last distributed PLM history ---> " + sql.toString());
				PreparedStatement stmtLastDistributed = conPlm.prepareStatement(sql);
				rsLastDistro = stmtLastDistributed.executeQuery();

				if (rsLastDistro.next() == false) {
					logger.info("No offer distribution history found for the discrepancies.");
				} 
				else {
					do {
						tempMap = new TreeMap<String, String>();
						tempMap.put("DISTRIBUTION_ID", rsLastDistro.getString("DISTRIBUTION_ID"));
						tempMap.put("DISTRIBUTION_ENVIRONMENT", rsLastDistro.getString("DISTRIBUTION_ENVIRONMENT"));
						tempMap.put("DISTRIBUTION_DESC", rsLastDistro.getString("DISTRIBUTION_DESC"));
						tempMap.put("DATE/TIME", rsLastDistro.getString("DISTRIBUTION DATE/TIME"));
						tempMap.put("CEC_5.X", rsLastDistro.getString("CEC_5.X"));
						offerDistroHistory.put(rsLastDistro.getString("EXTERNAL_ID"),tempMap);
					} while (rsLastDistro.next());
				}
			}

			for(String[] row : discrepancyList) {
				if(offerDistroHistory.containsKey(row[0])) {

					String[] data = {offerDistroHistory.get(row[0]).get("DISTRIBUTION_ID") , offerDistroHistory.get(row[0]).get("DISTRIBUTION_ENVIRONMENT") , offerDistroHistory.get(row[0]).get("DISTRIBUTION_DESC") , offerDistroHistory.get(row[0]).get("DATE/TIME") , offerDistroHistory.get(row[0]).get("CEC_5.X")};
					String[] comb = ArrayUtils.addAll(row, data);
					writer.writeNext(comb);

					if(row[3+columnsToViewLst.size()].equals("TRUE"))
						materialImpactsForOrphansAudit.add(comb);
				}
				else {
					//String[] data = {"" , "" , "" , "" , ""};
					//String[] comb = ArrayUtils.addAll(row, data);

					writer.writeNext(row);
					if(row[3+columnsToViewLst.size()].equals("TRUE"))
						materialImpactsForOrphansAudit.add(row);
				}
			}

			writer.flush();
			writer.close();
		}
		catch (Exception e) {
			logger.error("Error in fetchPlmOffersCompare() method: "+ e.getMessage());
		}
	}

	Boolean materialImpactDateRange(String inDate, Integer intMaterialImapctFutureWindow) 
	{
		try {
			SimpleDateFormat inDateFormatter = new SimpleDateFormat("dd-MMM-yy");
			Date date;
			Date today = new Date();
			date = inDateFormatter.parse(inDate);
			if(date.compareTo(today) <= 0 || Math.abs((date.getTime()-today.getTime())/86400000) <= intMaterialImapctFutureWindow)
				return true;
		} catch (ParseException e) {
			PlmController.errorsLst.add(new String[] {"Error in materialImpactDateRange() method: ", e.getMessage()});
			logger.error("Error in materialImpactDateRange() method: "+e.getMessage());
		}
		return false;
	}
}
