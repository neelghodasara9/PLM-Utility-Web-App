package com.java.plm.MyWebApp.controller;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;

public class PlmActiveOfferCampSiteAudit {
	final static Logger logger = Logger.getLogger(PlmActiveOfferCampSiteAudit.class);
	Connect_db connect_db = new Connect_db();
	Connection con = connect_db.getPLM1Connection();
	Map<String,String> siteCodeMap = new LinkedHashMap<String,String>();

	void compareSites(String strIgnoreList, String boosterSite, String outFilePath, String materialImpactIgnoreKeywords) 
	{
		ArrayList<String[]> materialImpactsForOfferCampaignSiteAudit = new ArrayList<String[]>();
		List<String> materialImpactIgnoreKeywordsList = new ArrayList<String>();

		List<String> ignoreList = new ArrayList<String>();
		if(!strIgnoreList.trim().equals("")) {
			String[] arrIgnoreList = strIgnoreList.split(",");
			for(String value : arrIgnoreList) 
				ignoreList.add(value.trim());
		}
		if(!materialImpactIgnoreKeywords.trim().equals("")) {
			String[] arrIgnoreList = materialImpactIgnoreKeywords.toUpperCase().split(",");
			for(String value : arrIgnoreList) 
				materialImpactIgnoreKeywordsList.add(value.trim());
		}
		loadSiteMap();
		compareOfferCampaignSite(ignoreList, boosterSite, outFilePath, materialImpactIgnoreKeywordsList, materialImpactsForOfferCampaignSiteAudit);
		Collections.sort(materialImpactsForOfferCampaignSiteAudit,new Comparator<String[]>() {
			public int compare(String[] strings, String[] otherStrings) {
				int discountCodeComp = strings[5].compareTo(otherStrings[5]);
				return ((discountCodeComp == 0) ? strings[4].compareTo(otherStrings[4]) : discountCodeComp);
			}
		});

		PlmController.materialImpactsForMail.put(this.getClass().getSimpleName(), materialImpactsForOfferCampaignSiteAudit);
	}

	void loadSiteMap()
	{
		try {
			ResultSet rsSiteInfo;
			String sql = "select site_id, site_code from plm_dbo.site_code_master";
			PreparedStatement pstmt = con.prepareStatement(sql);
			rsSiteInfo = pstmt.executeQuery();
			while(rsSiteInfo.next()) 
				siteCodeMap.put(rsSiteInfo.getString("SITE_ID") , rsSiteInfo.getString("site_code"));
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in loadSiteMap() method: ", e.getMessage()});

			logger.error("Error in loadSiteMap() method: "+e.getMessage());
		}
	}

	String lookForSortedMatch(String offerSites, String discountSites, String boosterSite, String sep) 
	{
		StringBuilder compareOutput = new StringBuilder();
		if(offerSites.equals(discountSites))
			return "True";

		String[] arrOfferSites = offerSites.split(sep);
		String[] arrDiscountSites = discountSites.split(sep);

		Set<String> setOfferSites = new TreeSet<String>(Arrays.asList(arrOfferSites));
		Set<String> setDiscountSites = new TreeSet<String>(Arrays.asList(arrDiscountSites));

		boolean length = true;
		if(arrOfferSites.length != arrDiscountSites.length) {
			length = false;	
		}

		/*for(String s:arrOfferSites)
			setOfferSites.add(s);

		for(String s:arrDiscountSites)
			setDiscountSites.add(s);*/

		if(setOfferSites.size() == setDiscountSites.size()) {
			if(setOfferSites.toString().equals(setDiscountSites.toString())) {
				if(length==false)
					return "Duplicate site present on offer or discount. Please check.";
				else
					return "True";
			}
		}

		//Boolean boosterDiff = false;
		StringBuilder temp = new StringBuilder();
		if(!boosterSite.trim().equals("")) {
			String[] arrBoosterSite = boosterSite.split(sep);
			Set<String> setBoosterSites = new TreeSet<String>(Arrays.asList(arrBoosterSite));
			Set<String> setTotalSites = new TreeSet<String>();

			/*	for(String s:arrBoosterSite)
				setBoosterSites.add(s);*/

			if(setBoosterSites.size() == setOfferSites.size()) {
				if(setBoosterSites.toString().equals(setOfferSites.toString())) 
					return "True";
			}
			else {
				setTotalSites.addAll(setOfferSites);
				setTotalSites.addAll(setBoosterSites);

				/*for(String site : setBoosterSites) 
					setTotalSites.add(site);*/

				if(setTotalSites.size() == setDiscountSites.size()) {
					if(setTotalSites.toString().equals(setDiscountSites.toString())) 
						return "True";
				}
			}
		}

		for(String site : setOfferSites) {
			if(setDiscountSites.contains(site)) {
				setDiscountSites.remove(site);
			}
			else {
				if(temp.length()==0)
					temp.append(siteCodeMap.get(site));
				else
					temp.append(", "+ siteCodeMap.get(site));
			}
		}
		if(temp.length()!=0) {
			compareOutput.append("Delete- " + temp);
			temp = new StringBuilder();
		}

		for(String site : setDiscountSites) {
			if(temp.length()==0)
				temp.append(siteCodeMap.get(site));
			else
				temp.append(", "+siteCodeMap.get(site));
		}
		if(compareOutput.length()!=0) 
			compareOutput.append("; Add- " + temp);
		else
			compareOutput.append("Add- " + temp);

		return compareOutput.toString();
	}

	void compareOfferCampaignSite(List<String> ignoreList, String boosterSite, String outFilePath, List<String> materialImpactIgnoreKeywordsList, ArrayList<String[]> materialImpactsForOfferCampaignSiteAudit) {
		try {
			FileWriter outputfile;
			Date date = new Date();

			outputfile = new FileWriter(new File(outFilePath));
			CSVWriter writer = new CSVWriter(outputfile); 

			ResultSet rsOfferCampaignSites = null;
			writer.writeNext(PlmController.csvFileHeaders.get(this.getClass().getSimpleName())); 

			SimpleDateFormat outDateFormatter = new SimpleDateFormat("dd-MMM-yyyy");
			String today = outDateFormatter.format(date);
			StringBuilder sql = new StringBuilder();
			sql.append("select project_code,offer_id , name, sales_advise, distro_preference,discount_id_list,discount_code_list,case when DISCOUNT_SITE_INTERSECTION is NOT NULL then DISCOUNT_SITE_INTERSECTION else DISCOUNT_SITE_ID end as DISCOUNT_SITE_ID, OFFER_SITE_ID from (select distinct d.project_code,a.offer_id , d.name, d.sales_advise, d.distro_preference, ( select LISTAGG(discount_id ,',')  WITHIN GROUP(ORDER BY discount_id) AS discount_id FROM plm_dbo.OFFER_DISC_MAP_TXN_DET c WHERE a.offer_id = c.offer_id GROUP BY a.offer_id ) as discount_id_list,( select LISTAGG(discount_code ,',')  WITHIN GROUP(ORDER BY discount_code) AS discount_code FROM plm_dbo.OFFER_DISC_MAP_TXN_DET ac,plm_dbo.discount_txn_master b WHERE ac.discount_id = b.discount_id and a.offer_id = ac.offer_id GROUP BY ac.offer_id ) as discount_code_list,(select listagg(site_id,',') within group (order by site_id) from (select distinct site_id, count(*) as \"INTERSECTED_SITES\" from plm_dbo.discount_site_map t,plm_dbo.OFFER_DISC_MAP_TXN_DET z where z.offer_id=a.offer_id and t.discount_id=z.discount_id and t.end_date>='TODAY' group by site_id having count(*)>1)) as DISCOUNT_SITE_INTERSECTION,(select listagg(site_id,',') within group (order by site_id) from (select distinct site_id, count(*) as \"INTERSECTED_SITES\" from plm_dbo.discount_site_map t,plm_dbo.OFFER_DISC_MAP_TXN_DET z where z.offer_id=a.offer_id and t.discount_id=z.discount_id and t.end_date>='TODAY' group by site_id having count(*)=1)) as DISCOUNT_SITE_ID,( select LISTAGG(c.site_id ,',')  WITHIN GROUP(ORDER BY c.site_id) AS site_codeid FROM plm_dbo.offer_site_map c WHERE a.offer_id = c.offer_id and c.end_date>='TODAY' GROUP BY a.offer_id ) as OFFER_SITE_ID from plm_dbo.OFFER_DISC_MAP_TXN_DET a, plm_dbo.DISCOUNT_SITE_MAP b, plm_dbo.discount_txn_master c, plm_dbo.OFFER_MASTER_TXN_DET d where a.offer_id = d.offer_id and a.discount_id = b.discount_id and a.discount_id = c.discount_id and a.offer_id in(select offer_id from plm_dbo.OFFER_MASTER_TXN_DET where end_date>='TODAY' and d.distro_preference not in('4.1') and PRPOSTN_ID is null ".replace("TODAY", today));

			if(!ignoreList.isEmpty()) {
				StringBuilder intakeIgnoreList = new StringBuilder();
				for(String value : ignoreList) {
					if(intakeIgnoreList.length()==0)
						intakeIgnoreList.append("'"+value.trim()+"'");
					else
						intakeIgnoreList.append(","+"'"+value.trim()+"'");
				}
				sql.append(" and project_code not in("+intakeIgnoreList +") ");
			}
			sql.append(") GROUP by d.project_code,a.offer_id,d.name, d.sales_advise,d.distro_preference,a.discount_id,discount_code)master");
			logger.info(sql.toString());
			PreparedStatement pstmt = con.prepareStatement(sql.toString());
			rsOfferCampaignSites = pstmt.executeQuery();
			if (rsOfferCampaignSites.next() == false) {
				logger.info("No active offers present in PLM3 DB!");
			} 
			else {
				do {
					String offertId = rsOfferCampaignSites.getString("offer_id");
					String offerName = rsOfferCampaignSites.getString("name");
					String salesAdvice = rsOfferCampaignSites.getString("sales_advise");
					String discountId = rsOfferCampaignSites.getString("discount_id_list");
					String discountCode = rsOfferCampaignSites.getString("discount_code_list");
					String discountSites = rsOfferCampaignSites.getString("DISCOUNT_SITE_ID");
					String offerSites = rsOfferCampaignSites.getString("OFFER_SITE_ID");
					String projectCode = rsOfferCampaignSites.getString("project_code");
					String distroPref = rsOfferCampaignSites.getString("distro_preference");

					//if(offertId.equals("11999"))
					//	System.out.println(offertId + "\n");
					Boolean isMaterialImpact = false;

					////////// compare ////////////
					if(offerSites==null)
						offerSites = "";
					if(discountSites == null)
						discountSites = "";
					
					String compareOutput = lookForSortedMatch(offerSites, discountSites, boosterSite, ",");
					if(!compareOutput.equals("True")) {
						String[] data = { projectCode, offertId, offerName, salesAdvice, distroPref, discountId, discountCode, discountSites.replace(",", ", "), offerSites.replace(",", ", "), compareOutput }; 
						//writer.writeNext(data); 
						if(!(materialImpactIgnoreKeywordsList.stream().anyMatch(Arrays.toString(data).toUpperCase()::contains))) {
							if(!distroPref.equals("4.1")) {
								materialImpactsForOfferCampaignSiteAudit.add(data);
								isMaterialImpact = true;
							}
						}
						String[] data2 = { projectCode, offertId, offerName, salesAdvice, distroPref, discountId, discountCode, discountSites.replace(",", ", "), offerSites.replace(",", ", "), compareOutput, isMaterialImpact.toString() };
						writer.writeNext(data2);
					}
				} while (rsOfferCampaignSites.next());
			}
			writer.flush();
			writer.close();
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in compareOfferCampaignSite() method: ", e.getMessage()});

			logger.error("Error in compareOfferCampaignSite() method: "+ e.getMessage());
		}
	}
}
