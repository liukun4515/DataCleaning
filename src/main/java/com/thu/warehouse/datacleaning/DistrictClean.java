package com.thu.warehouse.datacleaning;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.thu.warehouse.datacleaning.util.DataCleanException;
import com.thu.warehouse.datacleaning.util.DataType;

public class DistrictClean {

	/*
	 * 这个表中清洗的主要内容有： 1. district id, 必须为唯一的数字，不能为整数 2. district name 与
	 * region一起清洗，不能为空，并且桶一个name不能对应多个region 3. hab number 必须为整数或者长整数，并且必须大于等于0
	 * 4.city number 必须为整数或者长整数，并且必须大于等于0 5. ave salary 必须为整数或者长整数，并且大于等于0
	 * 6.umemploy rate 必须为浮点数，并且大于等于0，小于等于1或者100 7. crime
	 * number必须为整数或者长整数，并且大于等于0
	 */

	private static final String originFile = "district.csv";
	private static final String cleanFile = "districtClean.csv";
	private static final String errorFile = "districtError.csv";
	private static final String[] schema = { "district_id", "district_name", "region", "hab_number", "city_number",
			"ave_salary", "umemploy_rate", "crime_number" };
	private static final DataType[] DATA_TYPES = { DataType.INT, DataType.STRING, DataType.STRING, DataType.INT,
			DataType.INT, DataType.FLOAT, DataType.INT };
	private static final int schemaLength = schema.length;

	enum ErrorType {
		DISTRICT_ID_ERROR, DISTRICT_NAME_AND_REGION_ERROR, HAB_NUMBER_ERROR, CITY_NUMBER_ERROR, AVE_SALARY_ERROR, UMEMPLOY_REATE_ERROR, CRIME_NUMBER_ERROR, LENGTH_ERROR, DISTRICT_ID_UNIQUE_ERROR, NULL_ERROR, DIS_NAME_REGION_CONTRADICTION
	}

	public static void clean(String input, String clean, String error) throws DataCleanException, IOException {
		File inputFile = new File(input);
		File cleanFile = new File(clean);
		File errorFile = new File(error);
		if (!inputFile.exists()) {
			throw new DataCleanException("The inputFile is not exist");
		}
		if (cleanFile.exists()) {
			cleanFile.delete();
		}
		if (errorFile.exists()) {
			errorFile.delete();
		}

		CSVReader inputFileReader = new CSVReader(new FileReader(inputFile));
		CSVWriter cleanFileWriter = new CSVWriter(new FileWriter(cleanFile));
		CSVWriter errorFileWriter = new CSVWriter(new FileWriter(errorFile));
		String[] values;
		// get the schema
		values = inputFileReader.readNext();
		String[] errorSchema = new String[schemaLength + 1];
		if (values.length == schemaLength) {
			// check schema
			for (int i = 0; i < values.length; i++) {
				errorSchema[i] = schema[i];
				if (!values[i].equals(schema[i])) {
					throw new DataCleanException("The schema name is not mapping: " + values[i] + " " + schema[i]);
				}
			}
			// add schema infomation
			errorSchema[errorSchema.length - 1] = "error_info";

			// check value
			// should check the null
			Set<Integer> dis_id = new HashSet<Integer>();
			Map<String, String> disname_region = new HashMap<String, String>();
			errorFileWriter.writeNext(errorSchema);
			cleanFileWriter.writeNext(schema);
			while ((values = inputFileReader.readNext()) != null) {
				String errorInfo = "";
				// check the length
				if (values.length != schemaLength) {
					errorInfo = errorInfo + ErrorType.LENGTH_ERROR + " ";
				}
				// check the value
				// check the id is int and not null and it unique
				String disIdString = values[0];
				if (disIdString != null && disIdString.matches("^[1-9]\\d*$")) {
					System.out.println("match id " + disIdString);
					// check the id is unique
					int disIdInt = Integer.valueOf(disIdString);
					if (dis_id.contains(disIdInt)) {
						errorInfo = errorInfo + ErrorType.DISTRICT_ID_UNIQUE_ERROR + " ";
					} else {
						dis_id.add(disIdInt);
					}
				} else {
					System.out.println("Not match id " + disIdString);
					errorInfo = errorInfo + ErrorType.DISTRICT_ID_ERROR + " ";
				}

				// check the disname with region one disname just maping only
				String disNameString = values[1];
				String regionString = values[2];
				if (disNameString != null && regionString != null) {
					if (disname_region.containsKey(disNameString)) {
						String current_region = disname_region.get(disNameString);
						if (current_region.equals(regionString)) {
							// do nothing
						} else {
							errorInfo = errorInfo + ErrorType.DIS_NAME_REGION_CONTRADICTION + " ";
						}
					} else {
						disname_region.put(disNameString, regionString);
					}
				} else {
					errorInfo = errorInfo + ErrorType.NULL_ERROR + "";
				}
				// check the hab num just int >=0
				String hab_numString = values[3];
				if (hab_numString != null && hab_numString.matches("^[1-9]\\d*$")) {
					int hab_numInt = Integer.valueOf(hab_numString);
					if (hab_numInt >= 0) {
						// do nothing
					} else {
						errorInfo = errorInfo + ErrorType.HAB_NUMBER_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.HAB_NUMBER_ERROR + " ";
				}
				// check the city num just int >=0
				String city_numString = values[4];
				if (city_numString != null && city_numString.matches("^[1-9]\\d*$")) {
					int city_numInt = Integer.valueOf(city_numString);
					if (city_numInt > 0) {

					} else {
						errorInfo = errorInfo + ErrorType.CITY_NUMBER_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.CITY_NUMBER_ERROR + " ";
				}
				// check the ave salary just int >=0
				String ava_salaryString = values[5];
				if (ava_salaryString != null && ava_salaryString.matches("^[1-9]\\d*$")) {
					int ave_salaryInt = Integer.valueOf(ava_salaryString);
					if (ave_salaryInt >= 0) {

					} else {
						errorInfo = errorInfo + ErrorType.AVE_SALARY_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.AVE_SALARY_ERROR + " ";
				}
				// check the rate just float >=0 <=100
				String unemploy_rateString = values[6];
				if (unemploy_rateString != null && unemploy_rateString.matches("^\\d+(\\.\\d+)?$")) {
					float umemploy_rateFloat = Float.valueOf(unemploy_rateString);
					if (umemploy_rateFloat >= 0 && umemploy_rateFloat <= 100) {

					} else {
						errorInfo = errorInfo + ErrorType.UMEMPLOY_REATE_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.UMEMPLOY_REATE_ERROR + " ";
				}
				// just crime num just int >=0
				String crime_numString = values[7];
				if (crime_numString != null && crime_numString.matches("^[0-9]\\d*$")) {
					int crime_numInt = Integer.valueOf(crime_numString);
					if (crime_numInt >= 0) {

					} else {
						errorInfo = errorInfo + ErrorType.CRIME_NUMBER_ERROR;
					}
				} else {
					errorInfo = errorInfo + ErrorType.CRIME_NUMBER_ERROR + " ";
				}
				// just write the error row to the error file and don't write
				// write info schema

				if (errorInfo.length() > 0) {
					// write error info
					values = Arrays.copyOf(values, values.length + 1);
					values[values.length - 1] = errorInfo;
					errorFileWriter.writeNext(values);
				} else {
					// don't write error data just write clean data
					// cleanFileWriter.writeNext(values);
				}
				// the clean file

			}
			inputFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
		} else {
			inputFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
			throw new DataCleanException("The schema size is not mapping");
		}
	}

	public static void main(String[] args) throws DataCleanException, IOException {
		// TODO Auto-generated method stub
		clean(originFile, cleanFile, errorFile);

	}

}
