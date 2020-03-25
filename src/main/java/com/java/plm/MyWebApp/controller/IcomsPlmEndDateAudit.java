package com.java.plm.MyWebApp.controller;

import java.io.File;
import java.io.FileReader;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

public class IcomsPlmEndDateAudit {
	final static Logger logger = Logger.getLogger(IcomsPlmEndDateAudit.class);
	/*Map<String,String> icomsCampaignDateMap = new LinkedHashMap<String,String>();
	Map<String,String> plmCampaignDateMap = new LinkedHashMap<String,String>();
	Map<String,String> campaignDateDiscrepancies = new LinkedHashMap<String,String>();*/

	StringBuilder activeCampaigns = new StringBuilder();
	Map<String, Map<String,String>> icomsCampaignDateMap;
	Map<String, Map<String,String>> plmCampaignDateMap;
	Map<String, Map<String,String>> campaignDateDiscrepancies;

	Map<String,String> siteCodeMap = new LinkedHashMap<String,String>();
	public static ResourceBundle configBundle = null;
	Connect_db connect_db = new Connect_db();
	Connection conPLM3 = connect_db.getPLM1Connection();
	Connection conPStage = connect_db.getPStageConnection();
	Set<String> dbCampaignSite = new LinkedHashSet<>();
	CSVWriter writer;

	void compareUtility(String inFilePath, String materialImpactFutureWindow, String snoozeCamps, String strSnoozeDt, String outFilePath) 
	{
		try {
			ArrayList<String[]> materialImpactsForPlmIcomsAudit = new ArrayList<String[]>();
			List<String> ignoreList = new ArrayList<String>();

			if(!snoozeCamps.equals("") && !strSnoozeDt.equals("")) {
				if(strSnoozeDt.contains("-")) {
					String[] arrIgnoreList = strSnoozeDt.split("-");
					String snoozeDt = dateFormatter(arrIgnoreList[0] + "-" + arrIgnoreList[1] + "-" + arrIgnoreList[2], "MM-dd-yyyy", "dd-MMM-yy");
					if(materialImpactDateRange(snoozeDt)) {
						for(String value : snoozeCamps.split(",")) 
							ignoreList.add(value.trim());
					}
				} 
				if(strSnoozeDt.contains("/")) {
					String[] arrIgnoreList = strSnoozeDt.split("/");
					String snoozeDt = dateFormatter(arrIgnoreList[0] + "-" + arrIgnoreList[1] + "-" + arrIgnoreList[2], "MM-dd-yyyy", "dd-MMM-yy");
					if(materialImpactDateRange(snoozeDt)) {
						for(String value : snoozeCamps.split(",")) 
							ignoreList.add(value.trim());
					}
				} 
			}

			FileReader filereader = new FileReader(inFilePath); 
			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build(); 
			List<String[]> allData = csvReader.readAll(); 

			for (String[] row : allData) {
				if(row[5].trim().toUpperCase().equals("IGNORE"))
					ignoreList.add(row[0].trim());
			}

			FileWriter outputfile = new FileWriter(new File(outFilePath));
			writer = new CSVWriter(outputfile); 
			writer.writeNext(PlmController.csvFileHeaders.get(this.getClass().getSimpleName())); 
			//materialImpactsForPlmIcomsAudit.add(header);

			fetchTotalActiveCampaigns(ignoreList);
			loadSiteMap();
			fetchActiveICOMSDiscountData();
			fetchActivePlmDiscountData(materialImpactsForPlmIcomsAudit);
			compareDates(materialImpactsForPlmIcomsAudit, materialImpactFutureWindow);

			Collections.sort(materialImpactsForPlmIcomsAudit,new Comparator<String[]>() {
				public int compare(String[] strings, String[] otherStrings) {
					int discountCodeComp = strings[2].compareTo(otherStrings[2]);
					int discountIdComp =  ((discountCodeComp == 0) ? strings[3].compareTo(otherStrings[3]) : discountCodeComp);
					return ((discountIdComp == 0) ? strings[0].compareTo(otherStrings[0]) : discountIdComp);
				}
			});

			PlmController.materialImpactsForMail.put(this.getClass().getSimpleName(), materialImpactsForPlmIcomsAudit);
			writer.flush();
			writer.close();

		} catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in compareUtility() method: ", e.getMessage()});
			logger.error("Error in compareUtility() method: "+e.getMessage());
		}
	}

	/*void parseIcomsCsv(List<String> ignoreList,String filePath) 
	{
		try {
			FileReader filereader = new FileReader(filePath); 
			CSVReader csvReader = new CSVReaderBuilder(filereader) 
					.withSkipLines(1) 
					.build(); 
			List<String[]> allData = csvReader.readAll(); 
			for (String[] row : allData) {
				if(ignoreList.contains(row[2]))
					continue;
				icomsCampaignDateMap.put(row[0] + "_" + row[2], dateFormatter(row[5].substring(1,3) + "-" + row[5].substring(3,5) + "-" + row[5].substring(5,7), "yy-MM-dd", "dd-MMM-yy"));
			}
		}
		catch (Exception e) {
			logger.error("Error in parseIcomsCsv() method: "+e.getMessage());
		}
	}*/

	void fetchTotalActiveCampaigns(List<String> ignoreList)
	{
		try {
			Set<String> totalActiveCampaigns = new LinkedHashSet<>();
			ResultSet rsActiveDiscountsIcoms;
			ResultSet rsActiveDiscountsPlm;

			StringBuilder sql = new StringBuilder();
			StringBuilder discountIgnoreList = new StringBuilder();
			Date now = new Date();
			SimpleDateFormat outDateFormatter = new SimpleDateFormat("yyMMdd");
			sql.append("select distinct promotion_code from PSTAGE.ALL_CAMPAIGN_MASTER where sales_start_date <= 1" + outDateFormatter.format(now) + " and sales_end_date > 1" + outDateFormatter.format(now) + " and promotion_type = 'C' ");
			if(!ignoreList.isEmpty()) {
				for(String value : ignoreList) {
					if(discountIgnoreList.length()==0)
						discountIgnoreList.append("'" + value.trim() + "'");
					else
						discountIgnoreList.append("," + "'"+value.trim() + "'");
				}
				sql.append("and promotion_code not in(" + discountIgnoreList +") ");
			}
			logger.info("SQL ---> " + sql.toString());
			PreparedStatement pstmt = conPStage.prepareStatement(sql.toString());
			rsActiveDiscountsIcoms = pstmt.executeQuery();
			while(rsActiveDiscountsIcoms.next())
				totalActiveCampaigns.add("'" + rsActiveDiscountsIcoms.getString("PROMOTION_CODE") + "'" );


			sql = new StringBuilder();
			sql.append("select distinct discount_code from PLM_DBO.DISCOUNT_TXN_MASTER where start_date<=sysdate and end_date>sysdate ");
			if(!ignoreList.isEmpty()) 
				sql.append("and discount_code not in("+discountIgnoreList +") ");

			logger.info("SQL ---> " + sql.toString());
			pstmt = conPLM3.prepareStatement(sql.toString());
			rsActiveDiscountsPlm = pstmt.executeQuery();
			while(rsActiveDiscountsPlm.next()) 
				totalActiveCampaigns.add("'" + rsActiveDiscountsPlm.getString("discount_code") + "'");

			activeCampaigns.append(totalActiveCampaigns.toString().replace("[","").replace("]", ""));
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in fetchTotalActiveCampaigns() method: ", e.getMessage()});
			logger.error("Error in fetchTotalActiveCampaigns() method: "+e.getMessage());
		}
	}

	void fetchActiveICOMSDiscountData()
	{
		try {
			icomsCampaignDateMap = new TreeMap<String, Map<String,String>>();
			Map<String, String> tempMap;
			ResultSet rsDiscountInfo;
			StringBuilder sql = new StringBuilder();
			//logger.info(activeCampaigns.toString());
			sql.append("select site_id,PROMOTION_CODE,SALES_START_DATE,SALES_END_DATE from PSTAGE.ALL_CAMPAIGN_MASTER where promotion_code in (" + activeCampaigns.toString() + ") order by promotion_code,site_id");
			logger.info("SQL ---> " + sql.toString());

			PreparedStatement pstmt = conPStage.prepareStatement(sql.toString());
			rsDiscountInfo = pstmt.executeQuery();
			while(rsDiscountInfo.next()) {
				tempMap = new TreeMap<String, String>();
				String siteId = rsDiscountInfo.getString("SITE_ID");
				String discountCode = rsDiscountInfo.getString("PROMOTION_CODE");
				String endDt = rsDiscountInfo.getString("SALES_END_DATE");
				String startDt = rsDiscountInfo.getString("SALES_START_DATE");

				//exception since this camp has hypen in camp code;
				if(discountCode.equals("EBR-SCH-BD"))
					discountCode = discountCode.replace("-", "");

				endDt = dateFormatter(endDt.substring(1,3) + "-" + endDt.substring(3,5) + "-" + endDt.substring(5,7), "yy-MM-dd", "dd-MMM-yy");
				startDt = dateFormatter(startDt.substring(1,3) + "-" + startDt.substring(3,5) + "-" + startDt.substring(5,7), "yy-MM-dd", "dd-MMM-yy");

				tempMap.put("START_DATE", startDt);
				tempMap.put("END_DATE", endDt);

				String key = siteId+"_"+discountCode;
				if(icomsCampaignDateMap.containsKey(key)) {
					if(materialImpactDateRange(icomsCampaignDateMap.get(key).get("END_DATE"), 0))
						icomsCampaignDateMap.put(key, tempMap);
				}
				else
					icomsCampaignDateMap.put(key, tempMap);

			}
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in fetchActiveICOMSDiscountData() method: ", e.getMessage()});
			logger.error("Error in fetchActiveICOMSDiscountData() method: "+e.getMessage());
		}
	}

	void loadSiteMap()
	{
		try {
			ResultSet rsSiteInfo;
			String sql = "select site_id, site_code from plm_dbo.site_code_master";
			PreparedStatement pstmt = conPLM3.prepareStatement(sql);
			rsSiteInfo = pstmt.executeQuery();
			while(rsSiteInfo.next()) 
				siteCodeMap.put(rsSiteInfo.getString("SITE_ID") , rsSiteInfo.getString("site_code"));
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in loadSiteMap() method: ", e.getMessage()});
			logger.error("Error in loadSiteMap() method: "+e.getMessage());
		}
	}

	void fetchActivePlmDiscountData(ArrayList<String[]> materialImpactsForPlmIcomsAudit)
	{
		try {
			plmCampaignDateMap = new TreeMap<String, Map<String,String>>();
			Map<String, String> tempMap;
			ResultSet rsDiscountInfo;
			StringBuilder sql = new StringBuilder();
			sql.append("select y.site_id,x.discount_code, x.discount_id, y.START_DATE \"START DATE\", y.END_DATE \"END DATE\", offer_series from plm_dbo.discount_txn_master x left outer join plm_dbo.discount_site_map y on x.discount_id = y.discount_id where x.discount_code in(" + activeCampaigns.toString() + ") order by x.discount_code,y.site_id");
			logger.info("SQL ---> " + sql.toString());
			PreparedStatement pstmt = conPLM3.prepareStatement(sql.toString());
			rsDiscountInfo = pstmt.executeQuery();
			while(rsDiscountInfo.next()) {
				tempMap = new TreeMap<String, String>();
				String siteId = rsDiscountInfo.getString("SITE_ID");
				String discountCode = rsDiscountInfo.getString("DISCOUNT_CODE");
				String discountId = rsDiscountInfo.getString("discount_id");
				String endDt = rsDiscountInfo.getString("END DATE");
				String startDt = rsDiscountInfo.getString("START DATE");
				String offerSeries = rsDiscountInfo.getString("offer_series");

				endDt = dateFormatter(endDt.substring(0, 10), "yyyy-MM-dd", "dd-MMM-yy");
				startDt = dateFormatter(startDt.substring(0, 10), "yyyy-MM-dd", "dd-MMM-yy");

				tempMap.put("DISCOUNT_ID", discountId);
				tempMap.put("START_DATE", startDt);
				tempMap.put("END_DATE", endDt);
				tempMap.put("OFFER_SERIES", offerSeries);

				if(!dbCampaignSite.contains(siteId + "_" + discountCode))
					dbCampaignSite.add(siteId + "_" + discountCode);
				else {
					String[] data = {siteId, siteCodeMap.get(siteId), discountCode, discountId, startDt, endDt, "", "", offerSeries, "Duplicate site-campaign combination in PLM" };
					materialImpactsForPlmIcomsAudit.add(data);
					//writer.writeNext(data); 
					String[] data2 = {siteId, siteCodeMap.get(siteId), discountCode, discountId, startDt, endDt, "", "", offerSeries, "Duplicate site-campaign combination in PLM", "True" };
					writer.writeNext(data2);
					continue;
				}

				plmCampaignDateMap.put(siteId+"_"+discountCode, tempMap);
			}
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in fetchActivePlmDiscountData() method: ", e.getMessage()});
			logger.error("Error in fetchActivePlmDiscountData() method: "+e.getMessage());
		}
	}


	void compareDates(ArrayList<String[]> materialImpactsForPlmIcomsAudit, String materialImpactFutureWindow) 
	{
		try {
			Map<String, String> tempPlmMap;
			Map<String, String> tempIcomsMap;
			Integer intMaterialImapctFutureWindow = 0;
			try {
				intMaterialImapctFutureWindow = Math.abs((Integer.parseInt(materialImpactFutureWindow.trim())));
			}
			catch(Exception e){
			}

			for(String key: plmCampaignDateMap.keySet()) {
				tempPlmMap = new TreeMap<String, String>();
				tempPlmMap = plmCampaignDateMap.get(key);
				String splitKey[] = key.split("_");
				Boolean isMaterialImpact = false;

				/*if(splitKey[1].equals("RXCDG5524C")) 
					System.out.println("watch out...");*/

				if(splitKey.length == 2) {
					if(icomsCampaignDateMap.containsKey(key))
					{
						tempIcomsMap = new TreeMap<String, String>();
						tempIcomsMap = icomsCampaignDateMap.get(key);
						String plmStartDt = tempPlmMap.get("START_DATE");
						String plmEndDt = tempPlmMap.get("END_DATE");
						String icomsStartDt = tempIcomsMap.get("START_DATE");
						String icomsEndDt = tempIcomsMap.get("END_DATE");
						StringBuilder dateDiscrepancy = new StringBuilder();

						if(!icomsStartDt.toUpperCase().equals(plmStartDt.toUpperCase())) {
							Boolean icomsStartDtFlag = materialImpactDateRange(icomsStartDt);
							Boolean plmStartDtFlag = materialImpactDateRange(plmStartDt); 

							if(icomsStartDtFlag && plmStartDtFlag) {
								if(materialImpactDateRange(plmStartDt,intMaterialImapctFutureWindow) || materialImpactDateRange(icomsStartDt,intMaterialImapctFutureWindow)) {
									isMaterialImpact = true;
									dateDiscrepancy.append("Start date mismatch");

								}
							}
							else {
								if(icomsStartDtFlag || plmStartDtFlag) {
									isMaterialImpact = true;
									dateDiscrepancy.append("Start date mismatch");
								}
							}
						}

						if(!icomsEndDt.toUpperCase().equals(plmEndDt.toUpperCase())) {
							if(dateDiscrepancy.toString().isEmpty()) 
								dateDiscrepancy.append("End date mismatch");
							else
								dateDiscrepancy.append("; End date mismatch");

							//writer.writeNext(data); 
							if(materialImpactDateRange(plmEndDt,intMaterialImapctFutureWindow) || materialImpactDateRange(icomsEndDt,intMaterialImapctFutureWindow)) {
								isMaterialImpact = true;
							}

						}
						String[] data = {splitKey[0], siteCodeMap.get(splitKey[0]), splitKey[1], tempPlmMap.get("DISCOUNT_ID"), plmStartDt, plmEndDt, icomsStartDt, icomsEndDt, tempPlmMap.get("OFFER_SERIES"), dateDiscrepancy.toString() };
						String[] data2 = {splitKey[0], siteCodeMap.get(splitKey[0]), splitKey[1], tempPlmMap.get("DISCOUNT_ID"), plmStartDt, plmEndDt, icomsStartDt, icomsEndDt, tempPlmMap.get("OFFER_SERIES"), dateDiscrepancy.toString(), isMaterialImpact.toString() };

						if(isMaterialImpact)
							materialImpactsForPlmIcomsAudit.add(data);

						if(!dateDiscrepancy.toString().isEmpty()) 
							writer.writeNext(data2);

						icomsCampaignDateMap.remove(splitKey[0] + "_" + splitKey[1]);
					}
					else {
						String plmStartDt = tempPlmMap.get("START_DATE");
						String plmEndDt = tempPlmMap.get("END_DATE");
						String[] data = {splitKey[0], siteCodeMap.get(splitKey[0]), splitKey[1], tempPlmMap.get("DISCOUNT_ID"), plmStartDt, plmEndDt, "", "", tempPlmMap.get("OFFER_SERIES"), "Could not find this site-campaign combination in ICOMS." };
						//writer.writeNext(data); 
						if(materialImpactDateRange(plmEndDt,intMaterialImapctFutureWindow) || materialImpactDateRange(plmStartDt,intMaterialImapctFutureWindow)) {
							materialImpactsForPlmIcomsAudit.add(data);
							isMaterialImpact = true;
						}
						String[] data2 = { splitKey[0], siteCodeMap.get(splitKey[0]), splitKey[1], tempPlmMap.get("DISCOUNT_ID"), plmStartDt, plmEndDt, "", "", tempPlmMap.get("OFFER_SERIES"), "Could not find this site-campaign combination in ICOMS.", isMaterialImpact.toString() };
						writer.writeNext(data2);
					}
				}
				else {
					String plmStartDt = tempPlmMap.get("START_DATE");
					String plmEndDt = tempPlmMap.get("END_DATE");
					String[] data = {"", "", splitKey[1], tempPlmMap.get("DISCOUNT_ID"), plmStartDt, plmEndDt, "", "", tempPlmMap.get("OFFER_SERIES"), "No sites configured for this campaign in PLM." };
					//writer.writeNext(data); 
					if(materialImpactDateRange(plmEndDt,intMaterialImapctFutureWindow) || materialImpactDateRange(plmStartDt,intMaterialImapctFutureWindow)) {
						materialImpactsForPlmIcomsAudit.add(data);
						isMaterialImpact = true;
					}
					String[] data2 = {"", "", splitKey[1], tempPlmMap.get("DISCOUNT_ID"), plmStartDt, plmEndDt, "", "", tempPlmMap.get("OFFER_SERIES"), "No sites configured for this campaign in PLM.", isMaterialImpact.toString() };
					writer.writeNext(data2);
				}
			}

			for(String key: icomsCampaignDateMap.keySet()) {
				String[] keyElements = key.split("_");
				String[] data = {keyElements[0], siteCodeMap.get(keyElements[0]), keyElements[1], "Not found", "Not found", "Not found", icomsCampaignDateMap.get(key).get("START_DATE"), icomsCampaignDateMap.get(key).get("END_DATE"), "Not found", "Site-campaign combination configured in ICOMS, but not in PLM"};
				Boolean isMaterialImpact = false;

				//writer.writeNext(data); 
				if(materialImpactDateRange(icomsCampaignDateMap.get(key).get("END_DATE"), intMaterialImapctFutureWindow) || materialImpactDateRange(icomsCampaignDateMap.get(key).get("START_DATE"), intMaterialImapctFutureWindow)) {
					materialImpactsForPlmIcomsAudit.add(data);
					isMaterialImpact = true;
				}
				String[] data2 = {keyElements[0], siteCodeMap.get(keyElements[0]), keyElements[1], "Not found", "Not found", "Not found", icomsCampaignDateMap.get(key).get("START_DATE"), icomsCampaignDateMap.get(key).get("END_DATE"), "Not found", "Site-campaign combination configured in ICOMS, but not in PLM", isMaterialImpact.toString() };
				writer.writeNext(data2);
			}

		}
		catch(Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in compareDates() method: ", e.getMessage()});
			logger.error("Error in compareDates() method: "+e.getMessage());
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
			PlmController.errorsLst.add(new String[] {"Error in dateFormatter() method: ", e.getMessage()});
			logger.error("Error in dateFormatter() method: "+e.getMessage());
		}
		return outDate;
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

	Boolean materialImpactDateRange(String inDate) 
	{
		try {
			SimpleDateFormat inDateFormatter = new SimpleDateFormat("dd-MMM-yy");
			Date date;
			Date today = new Date();
			date = inDateFormatter.parse(inDate);
			if(date.before(today))
				return false;
		} catch (ParseException e) {
			PlmController.errorsLst.add(new String[] {"Error in materialImpactDateRange() method: ", e.getMessage()});
			logger.error("Error in materialImpactDateRange() method: "+e.getMessage());
		}
		return true;
	}
}