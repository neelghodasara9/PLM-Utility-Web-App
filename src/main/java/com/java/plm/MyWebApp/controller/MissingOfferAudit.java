package com.java.plm.MyWebApp.controller;

import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

public class MissingOfferAudit {
	final static Logger logger = Logger.getLogger(MissingOfferAudit.class);
	Connect_db connect_db = new Connect_db();
	Connection con = connect_db.getPLM1Connection();
	Map<String, String> needOffers; 

	void executeAudit(String inFilePath, String materialImpactFutureWindow, String outFilePath, Map<String, String> tagMap) {
		ArrayList<String[]> materialImpactsForPlmMissingOfferAudit = new ArrayList<String[]>();

		parseCsv(inFilePath);
		exportAuditFile(outFilePath, materialImpactFutureWindow, tagMap, materialImpactsForPlmMissingOfferAudit);

		/*Collections.sort(materialImpactsForPlmMissingOfferAudit,new Comparator<String[]>() {
			public int compare(String[] strings, String[] otherStrings) {
				int discountCodeComp = strings[1].compareTo(otherStrings[1]);
				return ((discountCodeComp == 0) ? strings[0].compareTo(otherStrings[0]) : discountCodeComp);
			}
		});
		 */
		PlmController.materialImpactsForMail.put(this.getClass().getSimpleName(), materialImpactsForPlmMissingOfferAudit);

	}

	void parseCsv(String inFilePath) 
	{
		try {
			FileReader filereader = new FileReader(inFilePath); 
			needOffers = new TreeMap<String,String>();

			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build(); 
			List<String[]> allData = csvReader.readAll(); 

			for (String[] row : allData) 
				needOffers.put(row[0].trim(), row[5].trim());
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in compareUtility() method: ", e.getMessage()});
			logger.error("Error in compareUtility() method: "+e.getMessage());
		}
	}

	void exportAuditFile(String outFilePath, String materialImpactFutureWindow, Map<String, String> tagMap, ArrayList<String[]> materialImpactsForPlmMissingOfferAudit) {
		try {
			PreparedStatement stmtActiveCampaigns = null;
			///Connection con = null;
			Date date = new Date();
			SimpleDateFormat outDateFormatter = new SimpleDateFormat("dd-MMM-yyyy");
			String today = outDateFormatter.format(date);

			Integer intMaterialImapctFutureWindow = 0;
			try {
				intMaterialImapctFutureWindow = Math.abs((Integer.parseInt(materialImpactFutureWindow.trim())));
			}
			catch(Exception e){
			}

			//con = connect_db.getPLM2Connection();
			String sql = "select d.discount_ID,d.discount_CODE,d.description,count(t.discount_id) as cnt,d.start_date,d.end_date, upper(z.pricing_owner_notes) \"PRICING_OWNER_NOTES\" from PLM_DBO.DISCOUNT_TXN_MASTER d left outer join(select d.discount_code,d.discount_id from PLM_DBO.DISCOUNT_TXN_MASTER d,PLM_DBO.OFFER_DISC_MAP_TXN_DET c, plm_dbo.OFFER_MASTER_TXN_DET a where c.discount_id = d.discount_id and a.offer_id = c.offer_id and a.prpostn_id is not null and c.PRIMARY_FLAG='Y' and d.end_date >= 'TODAY' and a.end_date >= 'TODAY' and a.distro_preference in ('5.0','BOTH'))t on t.discount_id = d.discount_id left outer join plm_dbo.PROJECT_MASTER_TXN_DET z on d.project_code=z.PROJECT_CODE where d.end_date >= 'TODAY' group by d.discount_ID,d.discount_code,d.description,d.start_date,d.end_date,upper(z.PRICING_OWNER_NOTES) order by cnt,d.start_date,d.discount_code";
			sql = sql.replace("TODAY", today);

			logger.info("SQL ---> " + sql.toString());
			stmtActiveCampaigns = con.prepareStatement(sql);
			ResultSet rsCampaigns = stmtActiveCampaigns.executeQuery();
			CSVWriter writer = new CSVWriter(new FileWriter(outFilePath));
			writer.writeNext(PlmController.csvFileHeaders.get("MissingOffersAudit")); 
			if (rsCampaigns.next() == false) {
				logger.info("No active campaigns present in PLM3 DB!");
			} 
			else {
				do {
					String discountId = rsCampaigns.getString("discount_id");
					String discountCode = rsCampaigns.getString("DISCOUNT_CODE");
					String description = rsCampaigns.getString("description");
					String cnt = rsCampaigns.getString("cnt");
					String pricingOwnerNotes = rsCampaigns.getString("pricing_owner_notes");
					String discountStartDt = dateFormatter(rsCampaigns.getString("START_DATE").substring(0, 10), "yyyy-MM-dd", "dd-MMM-yy");
					String discountEndDt = dateFormatter(rsCampaigns.getString("END_DATE").substring(0, 10), "yyyy-MM-dd", "dd-MMM-yy");

					Boolean isMaterialImpact = false;
					StringBuilder tag = new StringBuilder();

					/*if(discountCode.equals("RADVX0001"))
						System.out.println("watchout..");*/

					for(String s : tagMap.keySet())
						if(discountCode.startsWith(s.trim()))
							tag.append(tagMap.get(s));

					String strNeedOffer = needOffers.get(discountCode);
					if(strNeedOffer == null && pricingOwnerNotes != null) {
						for(String s : PlmController.poNotesMap.keySet()) {
							if(pricingOwnerNotes.contains(s.trim())) {
								strNeedOffer = PlmController.poNotesMap.get(s);
								break;
							}
						}
					}

					String[] data = { discountId, discountCode, description, tag.toString(), discountStartDt, discountEndDt, strNeedOffer, cnt }; 
					//writer.writeNext(data);
					if(cnt.equals("0")) {
						if(strNeedOffer != null) {
							if(!(strNeedOffer.toUpperCase().equals("NO") || strNeedOffer.toUpperCase().equals("IGNORE"))) {
								if(materialImpactDateRange(discountStartDt,intMaterialImapctFutureWindow) || materialImpactDateRange(discountEndDt,intMaterialImapctFutureWindow)) {
									isMaterialImpact = true;
									materialImpactsForPlmMissingOfferAudit.add(data);
								}
							}
						}
						else {
							if(materialImpactDateRange(discountStartDt,intMaterialImapctFutureWindow) || materialImpactDateRange(discountEndDt,intMaterialImapctFutureWindow)) {
								materialImpactsForPlmMissingOfferAudit.add(data);
								isMaterialImpact = true;
							}
						}
					}
					String[] data2 = { discountId, discountCode, description, tag.toString(), discountStartDt, discountEndDt, strNeedOffer, cnt, isMaterialImpact.toString() };
					writer.writeNext(data2);

				} while (rsCampaigns.next());
			}
			writer.flush();
			writer.close();
			logger.info("CampaignToPO-Count output file created successfully at " + outFilePath);
		}
		catch (SQLException e) {
			PlmController.errorsLst.add(new String[] {"SQL Error in exportAuditFile() method: ", e.getMessage()});
			logger.error("SQL Error in exportAuditFile() method: "+e.getMessage());		
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in exportAuditFile() method: ", e.getMessage()});
			logger.error("Error in exportAuditFile() method: "+e.getMessage());		
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
}
