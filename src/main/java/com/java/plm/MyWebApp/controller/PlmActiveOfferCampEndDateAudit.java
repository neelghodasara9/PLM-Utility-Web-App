package com.java.plm.MyWebApp.controller;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;

public class PlmActiveOfferCampEndDateAudit {
	final static Logger logger = Logger.getLogger(PlmActiveOfferCampEndDateAudit.class);
	Connect_db connect_db = new Connect_db();
	Connection con = connect_db.getPLM1Connection();

	void compareEndDates(String strIgnoreList, String outFilePath, String materialImpactIgnoreKeywords, String materialImpactFutureWindow) 
	{
		ArrayList<String[]> materialImpactsForOfferCampaignEndDateAudit = new ArrayList<String[]>();

		List<String> ignoreList = new ArrayList<String>();
		List<String> materialImpactIgnoreKeywordsList = new ArrayList<String>();

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
		compareOfferCampaignEndDate(ignoreList, outFilePath, materialImpactIgnoreKeywordsList, materialImpactsForOfferCampaignEndDateAudit, materialImpactFutureWindow);

		Collections.sort(materialImpactsForOfferCampaignEndDateAudit,new Comparator<String[]>() {
			public int compare(String[] strings, String[] otherStrings) {
				int discountCodeComp = strings[9].compareTo(otherStrings[9]);
				return ((discountCodeComp == 0) ? strings[8].compareTo(otherStrings[8]) : discountCodeComp);
			}
		});

		PlmController.materialImpactsForMail.put(this.getClass().getSimpleName(), materialImpactsForOfferCampaignEndDateAudit);
	}

	void compareOfferCampaignEndDate(List<String> ignoreList, String outFilePath, List<String> materialImpactIgnoreKeywordsList, ArrayList<String[]> materialImpactsForOfferCampaignEndDateAudit, String materialImpactFutureWindow) {
		try {
			FileWriter outputfile;
			Date date = new Date();
			//if(materialImpactIgnoreKeywordsList.stream().anyMatch("this is duplicate".toString().toUpperCase()::contains)) 
			//System.out.println("true");
			outputfile = new FileWriter(new File(outFilePath));
			CSVWriter writer = new CSVWriter(outputfile); 
			Integer intMaterialImapctFutureWindow = 0;
			try {
				intMaterialImapctFutureWindow = Math.abs((Integer.parseInt(materialImpactFutureWindow.trim())));
			}
			catch(Exception e){
			}

			ResultSet rsOfferCampaignEndDate = null;
			writer.writeNext(PlmController.csvFileHeaders.get(this.getClass().getSimpleName())); 

			SimpleDateFormat outDateFormatter = new SimpleDateFormat("dd-MMM-yyyy");
			String today = outDateFormatter.format(date);
			StringBuilder sql = new StringBuilder();
			sql.append("select OFFER_ID,OFFER_SITE_ID,SUBSTR(OFFER_START_DATE,0,9)OFFER_START_DATE,SUBSTR(OFFER_END_DATE,0,9) OFFER_END_DATE,discount_id,discount_code,discount_SITE_ID,SUBSTR(DISCOUNT_START_DATE,0,9)discount_START_DATE,SUBSTR(DISCOUNT_END_DATE,0,9)discount_end_DATE,external_id,name,sales_advise,distro_preference,status,created_date,project_code,intake_req_name,description from (select *from( select m.offer_id, LISTAGG( ( SELECT site_code FROM plm_dbo.site_code_master WHERE site_id = m.OFFER_SITE_ID ),',' ) WITHIN GROUP(ORDER BY m.OFFER_SITE_ID) AS OFFER_SITE_ID ,m.OFFER_START_DATE,m.OFFER_END_DATE,m.DISCOUNT_ID,m.discount_code,LISTAGG( ( SELECT site_code FROM plm_dbo.site_code_master WHERE site_id = m.DISCOUNT_SITE_ID ),',' ) WITHIN GROUP(ORDER BY m.DISCOUNT_SITE_ID) AS DISCOUNT_SITE_ID,m.DISCOUNT_START_DATE,m.DISCOUNT_END_DATE, m.external_id,m.name,m.sales_advise,m.distro_preference,m.status,m.created_date,m.project_code,m.INTAKE_REQ_NAME,m.DESCRIPTION from ( select t1.offer_id,t1.site_id \"OFFER_SITE_ID\",t1.start_date \"OFFER_START_DATE\",t1.end_date \"OFFER_END_DATE\",t1.DISCOUNT_ID, t1.discount_code,t2.SITE_ID \"DISCOUNT_SITE_ID\",t2.start_date \"DISCOUNT_START_DATE\",t2.END_DATE \"DISCOUNT_END_DATE\", t1.external_id,t1.name,t1.sales_advise,t1.distro_preference,t1.status,t1.created_date,t1.project_code,t1.INTAKE_REQ_NAME,t1.DESCRIPTION from(select a.offer_id,c.name,a.site_id,a.start_date,a.end_date,b.DISCOUNT_ID,e.discount_code,c.external_id,c.sales_advise,c.distro_preference,c.status,c.created_date,d.project_code,d.INTAKE_REQ_NAME,d.DESCRIPTION from PLM_DBO.OFFER_SITE_MAP a, plm_dbo.offer_disc_map_txn_det b,plm_dbo.OFFER_MASTER_TXN_DET c, plm_dbo.PROJECT_MASTER_TXN_DET d, plm_dbo.discount_txn_master e where b.discount_id = e.discount_id and c.project_code=d.PROJECT_CODE and a.offer_id=b.offer_id and c.PRPOSTN_ID is null and c.DISTRO_PREFERENCE in ('BOTH','5.0') and a.offer_id=c.offer_id ");

			if(!ignoreList.isEmpty()) {
				StringBuilder intakeIgnoreList = new StringBuilder();
				for(String value : ignoreList) {
					if(intakeIgnoreList.length()==0)
						intakeIgnoreList.append("'"+value.trim()+"'");
					else
						intakeIgnoreList.append(","+"'"+value.trim()+"'");
				}
				sql.append(" and c.project_code not in("+intakeIgnoreList +") ");
			}
			sql.append(" )t1 left outer join PLM_DBO.discount_site_map t2 on t1.discount_id=t2.discount_id and t1.SITE_ID = t2.site_ID)m group by m.offer_id,m.name,m.OFFER_START_DATE,m.OFFER_END_DATE,m.DISCOUNT_ID,m.discount_code,m.DISCOUNT_START_DATE,m.DISCOUNT_END_DATE, m.external_id,m.sales_advise,m.distro_preference,m.status,m.created_date,m.project_code,m.INTAKE_REQ_NAME,m.DESCRIPTION order by m.offer_id)m2 where CASE WHEN (m2.\"OFFER_END_DATE\"= m2.\"DISCOUNT_END_DATE\") or (m2.\"OFFER_END_DATE\" is null and m2.\"DISCOUNT_END_DATE\" IS NULL) THEN 0 ELSE 1  END = 1 and (m2.\"DISCOUNT_END_DATE\" >= 'TODAY' or m2.\"OFFER_END_DATE\" >= 'TODAY'))".replace("TODAY", today));
			logger.info(sql.toString());
			PreparedStatement pstmt = con.prepareStatement(sql.toString());
			rsOfferCampaignEndDate = pstmt.executeQuery();
			if (rsOfferCampaignEndDate.next() == false) {
				logger.info("No active offers present in PLM3 DB!");
			} 
			else {
				do {
					String[] data = { rsOfferCampaignEndDate.getString("external_id"), rsOfferCampaignEndDate.getString("offer_id"), rsOfferCampaignEndDate.getString("OFFER_SITE_ID").replace(",", ", "), rsOfferCampaignEndDate.getString("name"), rsOfferCampaignEndDate.getString("SALES_ADVISE"), rsOfferCampaignEndDate.getString("OFFER_START_DATE"), rsOfferCampaignEndDate.getString("DISCOUNT_START_DATE"), rsOfferCampaignEndDate.getString("OFFER_END_DATE"), rsOfferCampaignEndDate.getString("DISCOUNT_END_DATE"), rsOfferCampaignEndDate.getString("discount_id"),rsOfferCampaignEndDate.getString("discount_code"),rsOfferCampaignEndDate.getString("DISTRO_PREFERENCE"),rsOfferCampaignEndDate.getString("STATUS"),rsOfferCampaignEndDate.getString("CREATED_DATE"),rsOfferCampaignEndDate.getString("PROJECT_CODE"),rsOfferCampaignEndDate.getString("INTAKE_REQ_NAME"),rsOfferCampaignEndDate.getString("DESCRIPTION") }; 
					Boolean isMaterialImpact = false;

					//writer.writeNext(data); 
					if(!(materialImpactIgnoreKeywordsList.stream().anyMatch(Arrays.toString(data).toUpperCase()::contains))) {
						if(materialImpactDateRange(rsOfferCampaignEndDate.getString("OFFER_END_DATE"),intMaterialImapctFutureWindow) || materialImpactDateRange(rsOfferCampaignEndDate.getString("DISCOUNT_END_DATE"),intMaterialImapctFutureWindow)) {
							if(!rsOfferCampaignEndDate.getString("DISTRO_PREFERENCE").contains("4.1")) {
								materialImpactsForOfferCampaignEndDateAudit.add(data);
								isMaterialImpact = true;
							}
						}
					}
					String[] data2 = { rsOfferCampaignEndDate.getString("external_id"), rsOfferCampaignEndDate.getString("offer_id"), rsOfferCampaignEndDate.getString("OFFER_SITE_ID").replace(",", ", "), rsOfferCampaignEndDate.getString("name"), rsOfferCampaignEndDate.getString("SALES_ADVISE"), rsOfferCampaignEndDate.getString("OFFER_START_DATE"), rsOfferCampaignEndDate.getString("DISCOUNT_START_DATE"), rsOfferCampaignEndDate.getString("OFFER_END_DATE"), rsOfferCampaignEndDate.getString("DISCOUNT_END_DATE"), rsOfferCampaignEndDate.getString("discount_id"),rsOfferCampaignEndDate.getString("discount_code"),rsOfferCampaignEndDate.getString("DISTRO_PREFERENCE"),rsOfferCampaignEndDate.getString("STATUS"),rsOfferCampaignEndDate.getString("CREATED_DATE"),rsOfferCampaignEndDate.getString("PROJECT_CODE"),rsOfferCampaignEndDate.getString("INTAKE_REQ_NAME"),rsOfferCampaignEndDate.getString("DESCRIPTION"), isMaterialImpact.toString() };
					writer.writeNext(data2);
				} while (rsOfferCampaignEndDate.next());
			}
			writer.flush();
			writer.close();
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in compareOfferCampaignEndDates() method: ", e.getMessage()});
			logger.error("Error in compareOfferCampaignEndDates() method: "+ e.getMessage());
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
