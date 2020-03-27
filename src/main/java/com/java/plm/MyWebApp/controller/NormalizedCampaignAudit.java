package com.java.plm.MyWebApp.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

public class NormalizedCampaignAudit {

	final static Logger logger = Logger.getLogger(NormalizedCampaignAudit.class);
	Connect_db connect_db = new Connect_db();
	Connection con = connect_db.getPLM1Connection();
	Map<String, Map<String,String>> csvIntentMap;
	Map<String, String> csvAgentMap; 
	Map<String, String> csvChannelsMap; 
	Map<String, String> csvAddonMap; 
	Map<String, String> csvNeedsOfferMap; 

	void executeNormCompare(String inFilePath, String outFilePath, String sepToken) {
		ArrayList<String[]> materialImpactsForNCAudit = new ArrayList<String[]>();
		parseCsv(inFilePath);

		fetchNormCampDbCompare(outFilePath, sepToken, materialImpactsForNCAudit);

		Collections.sort(materialImpactsForNCAudit,new Comparator<String[]>() {
			public int compare(String[] strings, String[] otherStrings) {
				int discountCodeComp = strings[1].compareTo(otherStrings[1]);
				return ((discountCodeComp == 0) ? strings[0].compareTo(otherStrings[0]) : discountCodeComp);
			}
		});

		PlmController.materialImpactsForMail.put(this.getClass().getSimpleName(), materialImpactsForNCAudit);

	}

	void parseCsv(String inFilePath) 
	{
		try {
			FileReader filereader = new FileReader(inFilePath); 

			Map<String, String> csvMap;
			csvIntentMap = new TreeMap<String,Map<String,String>>();
			csvAgentMap = new TreeMap<String,String>();
			csvChannelsMap = new TreeMap<String,String>();
			csvAddonMap = new TreeMap<String,String>();
			csvNeedsOfferMap = new TreeMap<String,String>();

			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build(); 
			List<String[]> allData = csvReader.readAll(); 
			Boolean bracesStart = false;

			for (String[] row : allData) {
				String intentList[] = row[1].replaceAll("[^a-zA-Z0-9\\[\\]\\:\\,]", "").split(",");
				StringBuilder temp = new StringBuilder();
				String key = "";
				csvMap = new TreeMap<String, String>();
				csvAgentMap.put(row[0].trim(), row[2].trim().replace(" ", ","));
				csvChannelsMap.put(row[0].trim(), row[3].trim().replace(" ", ""));
				csvAddonMap.put(row[0].trim(), row[4].trim().replace(" ", ","));
				csvNeedsOfferMap.put(row[0].trim(), row[5].trim());
				//logger.info("comparing->" + row[0]);

				/*if(row[0].equals("CHL171PEQ"))
					System.out.println("watch out");
				 */
				if(row[1]==null || row[1].equals(""))	{	
					csvIntentMap.put(row[0].trim(), null);
					//logger.info(discountCode);
					continue;
				}
				/*if(bracesStart==true)
					System.out.println("prob..");
				 */
				for (String intent : intentList) {

					String data[] = intent.split(":");

					if(data.length>1 || bracesStart==true) {
						if(bracesStart==false) {
							if(data[1].contains("[")) {
								key = data[0];
								temp.append(data[1].replace("[", ""));
								bracesStart = true;
								continue;
							}
						}
						if(data[0].contains("]")) {
							temp.append(","+data[0].replace("]", ""));
							csvMap.put(key,temp.toString());
							key = "";
							bracesStart = false;
							temp = new StringBuilder();
							continue;
						}
						if(bracesStart==false) 
							csvMap.put(data[0], data[1]);
						else
							temp.append(","+data[0]);
					}
					else
						csvMap.put(data[0], "");
				}
				/*for (String t : csvMap.keySet()) {
					System.out.println(row[0].trim()+", key: " + t + ", value: " + csvMap.get(t));

				}*/
				csvIntentMap.put(row[0].trim(), csvMap);
			}
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in parseCsv() method: ", e.getMessage()});

			logger.error("Error in parseCsv() method: "+e.getMessage());
		}
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
			setTextToCompare1.add(s);

		for(String s:arrTextToCompare2)
			setTextToCompare2.add(s);

		if(setTextToCompare1.size() == setTextToCompare2.size()) {
			if(setTextToCompare1.toString().equals(setTextToCompare2.toString())) {
				return true;
			}
		}
		return false;
	}

	void fetchNormCampDbCompare(String outFilePath, String sepToken, ArrayList<String[]> materialImpactsForNCAudit) {
		try {
			FileWriter outputfile;
			String timeStamp  = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
			if(outFilePath.trim().equals("")) 
				outFilePath = System.getProperty("user.dir") + "\\ACTIVE_CAMPAIGN_TO_PO_COUNT_AUDIT-" + timeStamp + ".csv";
			outputfile = new FileWriter(new File(outFilePath));
			CSVWriter writer = new CSVWriter(outputfile); 
			Map<String, String> intentMap;
			Map<String, String> tempMap;
			Set<String> dbDiscountCodeSet = new LinkedHashSet<>();
			Set<String> dbInactiveDiscountCodeSet = new LinkedHashSet<>();
			SimpleDateFormat outDateFormatter = new SimpleDateFormat("dd-MMM-yyyy");
			String today = outDateFormatter.format(new Date());

			ResultSet rsInactiveDiscount = null;
			StringBuilder sql = new StringBuilder();
			sql.append("select discount_code from PLM_DBO.DISCOUNT_TXN_MASTER where discount_code not in(select discount_code from PLM_DBO.DISCOUNT_TXN_MASTER where start_date<='" + today + "' and end_date>'" + today + "')");
			PreparedStatement pstmtInactiveDisc = con.prepareStatement(sql.toString());
			rsInactiveDiscount = pstmtInactiveDisc.executeQuery();
			if (rsInactiveDiscount.next() == false) {
				logger.info("No inactive campaigns found in PLM3 DB!");
			} 
			else {
				do {
					dbInactiveDiscountCodeSet.add(rsInactiveDiscount.getString("DISCOUNT_CODE"));
				} while (rsInactiveDiscount.next());
			}

			ResultSet rsNormalizedDiscount = null;
			writer.writeNext(PlmController.csvFileHeaders.get(this.getClass().getSimpleName())); 

			sql = new StringBuilder();
			sql.append("select listagg(a.discount_id,', ') WITHIN GROUP (ORDER BY a.discount_id) AS DISCOUNT_ID, a.DISCOUNT_CODE, a.INTENT, a.AGENT_TYPE, a.CHANNELS_LABEL, a.ADDON_TYPE from PLM_DBO.DISCOUNT_TXN_MASTER b left outer join PLM_DBO.vw_normlzd_campgn a on a.discount_id=b.discount_id where b.start_date<='" + today + "' and b.end_date>'" + today + "' group by a.DISCOUNT_CODE, a.INTENT, a.AGENT_TYPE, a.CHANNELS_LABEL, a.ADDON_TYPE order by a.DISCOUNT_CODE, a.INTENT, a.AGENT_TYPE, a.CHANNELS_LABEL, a.ADDON_TYPE");
			logger.info("SQL ---> " + sql.toString());
			PreparedStatement pstmt = con.prepareStatement(sql.toString());
			rsNormalizedDiscount = pstmt.executeQuery();
			if (rsNormalizedDiscount.next() == false) {
				logger.info("No active normalized campaign present in PLM3 DB!");
			} 
			else {
				do {
					intentMap = new TreeMap<String, String>();
					String discountId = rsNormalizedDiscount.getString("discount_id");
					String discountCode = rsNormalizedDiscount.getString("DISCOUNT_CODE");
					String intentList = rsNormalizedDiscount.getString("INTENT");
					String agentType = rsNormalizedDiscount.getString("AGENT_TYPE");
					String channels = rsNormalizedDiscount.getString("CHANNELS_LABEL");
					String addonType = rsNormalizedDiscount.getString("ADDON_TYPE");
					StringBuilder correctionStr = new StringBuilder();
					String arrIntent[];

					dbDiscountCodeSet.add(discountCode);

					if(agentType == null || agentType.equals(""))		
						agentType = "";
					else
						agentType = agentType.replace("#", ",");

					if(channels == null || channels.equals(""))		
						channels = "";
					else
						channels = channels.replace("#", ",");

					if(addonType == null || addonType.equals(""))		
						addonType = "";
					else
						addonType = addonType.replace("#", ",");

					/*
					 * if(discountId.equals("40847")) System.out.println("watch out");
					 */

					/*if(discountCode.equals("AC191DS12S") || discountCode.equals("RXCCQ0024C"))
						System.out.println("watch out");
					 */
					//logger.info(discountCode);
					if(intentList==null || intentList.equals(""))	{	
						intentMap=null;
					}
					else {
						arrIntent = intentList.split(";");
						for (String intent : arrIntent) {
							if(!intent.equals("")) {
								String data[] = intent.split("#");
								if(data[1].equals("NA"))
									intentMap.put(data[0], data[1].replace("NA", ""));
								else
									intentMap.put(data[0], data[1]);
							}
						}
					}
					//logger.info(discountCode);


					////////// compare ////////////
					StringBuilder outStrDb = new StringBuilder();
					if(intentMap == null)
						outStrDb.append("");
					else {
						for (String dbKey : intentMap.keySet()) 
							outStrDb.append(dbKey + ":" + intentMap.get(dbKey) + sepToken);
					}

					StringBuilder outStrCsv = new StringBuilder();
					if(!csvIntentMap.containsKey(discountCode))
						outStrCsv.append("");
					else {
						if(csvIntentMap.get(discountCode) == null)
							outStrCsv.append("");
						else {
							for (String keyCsv : csvIntentMap.get(discountCode).keySet()) 
								outStrCsv.append(keyCsv + ":" + csvIntentMap.get(discountCode).get(keyCsv) + sepToken);
						}
					}

					if(!csvIntentMap.containsKey(discountCode)) {
						String[] data = { discountId, discountCode, "", "", outStrDb.toString(), "", agentType, "", channels, "", addonType, "No data found in Golden-source csv." }; 
						writer.writeNext(data); 
						materialImpactsForNCAudit.add(data);
					}
					else {
						Boolean intentIsSame = true;
						if(lookForSortedMatch(agentType, csvAgentMap.get(discountCode), ",") == false)
						{
							if(correctionStr.length()==0)
								correctionStr.append("Agent");
							else
								correctionStr.append(", Agent");
						}

						if(lookForSortedMatch(channels, csvChannelsMap.get(discountCode), ",") == false)
						{
							if(correctionStr.length()==0)
								correctionStr.append("Channels");
							else
								correctionStr.append(", Channels");
						}

						if(lookForSortedMatch(addonType, csvAddonMap.get(discountCode), ",") == false)
						{
							if(correctionStr.length()==0)
								correctionStr.append("AddonType");
							else
								correctionStr.append(", AddonType");
						}

						if(csvIntentMap.get(discountCode)==null && intentMap==null)
							intentIsSame=true;
						else if((intentMap==null && csvIntentMap.get(discountCode)!=null) || (intentMap!=null && csvIntentMap.get(discountCode)==null))
							intentIsSame=false;
						else if(csvIntentMap.get(discountCode).equals(intentMap)) 
							intentIsSame=true;
						else {
							if(csvIntentMap.get(discountCode).size() == intentMap.size()) {
								for (String keyDb : intentMap.keySet()) {
									if(!csvIntentMap.get(discountCode).containsKey(keyDb)) {
										intentIsSame = false;
										break;
									}
									if(lookForSortedMatch(intentMap.get(keyDb), csvIntentMap.get(discountCode).get(keyDb), ",") == false) {
										intentIsSame = false;
										break;
									}
								}
							}
							else 
								intentIsSame = false;
						}
						if(!intentIsSame)
						{
							if(correctionStr.length()==0)
								correctionStr.append("Intent");
							else
								correctionStr.append(", Intent");
						}
						if(correctionStr.length()>0) {
							String[] data = { discountId, discountCode, csvNeedsOfferMap.get(discountCode), outStrCsv.toString(), outStrDb.toString(), csvAgentMap.get(discountCode), agentType, csvChannelsMap.get(discountCode), channels, csvAddonMap.get(discountCode), addonType, correctionStr.toString() }; 
							writer.writeNext(data);
							if(csvNeedsOfferMap.containsKey(discountCode)) {
								if(!(csvNeedsOfferMap.get(discountCode).toUpperCase().equals("NO") || csvNeedsOfferMap.get(discountCode).toUpperCase().equals("IGNORE"))) {
									materialImpactsForNCAudit.add(data);
								}
							}
							else {
								materialImpactsForNCAudit.add(data);
							}
						}
					}
				} while (rsNormalizedDiscount.next());
			}

			for (String csvKey : csvIntentMap.keySet()) {
				if(!dbDiscountCodeSet.contains(csvKey) && !dbInactiveDiscountCodeSet.contains(csvKey)) {
					tempMap = new TreeMap<String, String>();
					tempMap = csvIntentMap.get(csvKey);
					StringBuilder tempStr = new StringBuilder();
					if(tempMap == null)
						tempStr.append("");
					else {
						for (String key : tempMap.keySet()) 
							tempStr.append(key + ":" + tempMap.get(key) + sepToken);
					}

					String[] data = { "", csvKey, csvNeedsOfferMap.get(csvKey), tempStr.toString(), "", csvAgentMap.get(csvKey), "", csvChannelsMap.get(csvKey), "", csvAddonMap.get(csvKey), "", "Campaign present in Golden-source but not in PLM3 db." }; 
					writer.writeNext(data); 
					if(csvNeedsOfferMap.containsKey(csvKey)) {
						if(!(csvNeedsOfferMap.get(csvKey).toUpperCase().equals("NO") || csvNeedsOfferMap.get(csvKey).toUpperCase().equals("IGNORE"))) {
							materialImpactsForNCAudit.add(data);
						}
					}
					else {
						materialImpactsForNCAudit.add(data);
					}
				}
			}
			writer.flush();
			writer.close();
		}
		catch (Exception e) {
			PlmController.errorsLst.add(new String[] {"Error in fetchNormalizedCampaignsFromDB() method: ", e.getMessage()});
			logger.error("Error in fetchNormalizedCampaignsFromDB() method: "+ e.getMessage());
		}
	}
}
