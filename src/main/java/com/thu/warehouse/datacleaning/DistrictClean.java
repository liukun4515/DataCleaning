package com.thu.warehouse.datacleaning;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class DistrictClean {
	
	/*
	 * 这个表中清洗的主要内容有：
	 * 1. district id, 必须为唯一的数字，不能为整数
	 * 2. district name 与 region 一起清洗，不能为空，并且桶一个name不能对应多个region
	 * 3. hab number 必须为整数或者长整数，并且必须大于等于0
	 * 4. city number 必须为整数或者长整数，并且必须大于等于0
	 * 5. ave salary 必须为整数或者长整数，并且大于等于0
	 * 6. umemploy rate 必须为浮点数，并且大于等于0，小于等于1或者100
	 * 7. crime number 必须为整数或者长整数，并且大于等于0
	 */
	
	private static final String originFile = "district.csv";
	private static final String cleanFile = "districtClean.csv";
	private static final String errorFile = "districtError.csv";
	private static final String[] schema = {"district_id","district_name","region","hab_number","city_number","ave_salary","umemploy_rate","crime_number"};
	private static final DataType[] DATA_TYPES = {DataType.INT,DataType.STRING,DataType.STRING,DataType.INT,DataType.INT,DataType.FLOAT,DataType.INT};
	private static final int schemaLength = schema.length;
	
	enum ErrorType{
		DISTRICT_ID_ERROR,
		DISTRICT_NAME_AND_REGION_ERROR,
		HAB_NUMBER_ERROR,
		CITY_NUMBER_ERROR,
		AVE_SALARY_ERROR,
		UMEMPLOY_REATE_ERROR,
		CRIME_NUMBER_ERROR
	}
	
	public static void clean(String input,String clean,String error) throws DataCleanException, IOException{
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
		if (values.length==schemaLength) {
			// check schema
			for (int i = 0; i < values.length; i++) {
				
				if (!values[i].equals(schema[i])) {
					throw new DataCleanException("The schema name is not mapping: "+ values[i]+" "+schema[i]);
				}
			}
			
			// check value
			// should check the null
			Set<Integer> dis_id = new HashSet<Integer>();
			Map<String, String> disname_region = new HashMap<String,String>();
			
			while((values = inputFileReader.readNext())!=null){
				// check the value
				
				// check the id is int and not null and it unique
				
				
				// check the disname with region one disname just maping only one region
				
				// check the hab num just int >=0
				
				
				// check the city num just int >=0
				
				// check the ave salary just int >=0
				
				// check the rate just float >=0 <=100
				
				// just crime num just int >=0
				
				// just write the error row to the error file and don't write the clean file
				
			}
		}else{
			throw new DataCleanException("The schema size is not mapping");
		}	
	}
	
	


	public static void main(String[] args) throws DataCleanException, IOException {
		// TODO Auto-generated method stub
		clean(originFile, cleanFile, errorFile);

	}

}
