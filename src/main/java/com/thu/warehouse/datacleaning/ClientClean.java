package com.thu.warehouse.datacleaning;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.thu.warehouse.datacleaning.util.DataCleanException;
import com.thu.warehouse.datacleaning.util.DataType;

public class ClientClean {

	enum ErrorType {
		CLIENT_ID_ERROR, BIRTH_NUMBER_ERROR, DISTRICT_ID_ERROR, CLIENT_ID_UNIQUE_ERROR, LENGTH_ERROR, DISTRICT_ID_NOT_FOUNT_ERROR
	}

	public static String[] schema = { "client_id", "birth_number", "district_id" };
	public static final DataType[] dataType = { DataType.INT, DataType.INT, DataType.INT };

	public static final String origin = "client.csv";
	public static final String cleanDistrict = "district.csv";
	public static final String clean = "clientClean.csv";
	public static final String error = "clientError.csv";
	public static final String sex = "sex";
	public static final String age = "age";
	public static final String ageRange = "age_range";

	public static void clean(String input, String clean, String error) throws DataCleanException, IOException {

		File inputFile = new File(input);
		File cleanFile = new File(clean);
		File districtFile = new File(cleanDistrict);
		File errorFile = new File(error);

		if (!inputFile.exists()) {
			System.out.println("The input file is not exist " + inputFile.getName());
			throw new DataCleanException("The inputFile is not exist");
		}
		if (!districtFile.exists()) {
			System.out.println("The clean district file is not exist" + districtFile.getName());
			throw new DataCleanException("The clean district file is not exist" + districtFile.getName());
		}
		if (cleanFile.exists()) {
			cleanFile.delete();
		}
		if (errorFile.exists()) {
			errorFile.delete();
		}

		CSVReader inputFileReader = new CSVReader(new FileReader(inputFile));
		CSVReader districtFileReader = new CSVReader(new FileReader(districtFile));
		CSVWriter cleanFileWriter = new CSVWriter(new FileWriter(cleanFile),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER);
		CSVWriter errorFileWriter = new CSVWriter(new FileWriter(errorFile),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER);

		String[] values;
		String[] errorSchema = new String[schema.length + 1];
		Set<Integer> client_id_set = new HashSet<>();
		Set<Integer> district_id_set = new HashSet<>();
		// construct the district_id_set
		districtFileReader.readNext();// skip the schema
		List<String[]> list = districtFileReader.readAll();
		for (String[] strings : list) {
			district_id_set.add(Integer.valueOf(strings[0]));
		}
		// check the schema length
		values = inputFileReader.readNext();
		if (values.length == schema.length) {

			for (int i = 0; i < schema.length; i++) {
				errorSchema[i] = schema[i];
				if (!values[i].equals(schema[i])) {
					throw new DataCleanException("The schema is not mapping " + values[i] + " " + schema[i]);
				}
			}
			errorSchema[errorSchema.length - 1] = "error_info";

			// add the schema
			schema = Arrays.copyOf(schema, schema.length + 3);
			schema[schema.length - 1] = ageRange;
			schema[schema.length - 2] = age;
			schema[schema.length - 3] = sex;

			cleanFileWriter.writeNext(schema);
			errorFileWriter.writeNext(errorSchema);
			while ((values = inputFileReader.readNext()) != null) {
				String sex = "";
				int age = 0;
				int ageRange = 0;
				String errorInfo = "";
				// check the value length
				if (values.length != schema.length-3) {
					errorInfo = errorInfo + ErrorType.LENGTH_ERROR + " ";
				}
				// check the client id
				String client_idString = values[0];
				if (client_idString != null && client_idString.matches("^[1-9]\\d*$")) {
					System.out.println("Match id " + client_idString);
					// check the unique
					int client_idInt = Integer.valueOf(client_idString);
					if (client_id_set.contains(client_idInt)) {
						errorInfo = errorInfo + ErrorType.CLIENT_ID_UNIQUE_ERROR + " ";
					} else {
						client_id_set.add(client_idInt);
					}
				} else {
					errorInfo = errorInfo + ErrorType.CLIENT_ID_ERROR + " ";
				}
				// check the birth data
				String birth_numberString = values[1];
				if (birth_numberString.length() == 6 && birth_numberString.matches("^[0-9]\\d*$")) {
					int birth_numInt = Integer.valueOf(birth_numberString);
					age = birth_numInt / 10000;
					ageRange = age / 10 + 1;// 0-9 1 10-19 2 20-29 3
					// solve the yymmdd the mm
					int mm = birth_numInt / 100 % 100;
					sex = "man";
					if (mm > 12) {
						mm = mm - 50;
						sex = "woman";
					}
					if (mm > 0 && mm <= 12) {
						if (mm <= 12 && mm >= 10) {
							String result = "";
							result = result + birth_numInt / 10000;
							result = result + mm;
							result = result + birth_numInt % 100;
							birth_numberString = result;
						} else {
							String result = "";
							result = result + birth_numInt / 10000;
							result = result + "0" + mm;
							result = result + birth_numInt % 100;
							birth_numberString = result;
						}
						SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
						format.setLenient(false);
						try {
							format.parse(birth_numberString);
						} catch (ParseException e) {
							errorInfo = errorInfo + ErrorType.BIRTH_NUMBER_ERROR + " ";
						}
					} else {
						errorInfo = errorInfo + ErrorType.BIRTH_NUMBER_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.BIRTH_NUMBER_ERROR + " ";
				}

				// check the district id
				String district_idString = values[2];
				if (district_idString != null && district_idString.matches("^[1-9]\\d*$")) {
					int district_idInt = Integer.valueOf(district_idString);
					if (district_id_set.contains(district_idInt)) {

					} else {
						errorInfo = errorInfo + ErrorType.DISTRICT_ID_NOT_FOUNT_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.DISTRICT_ID_ERROR + " ";
				}

				if (errorInfo.length() > 0) {
					System.out.println("The error id +" + values[0]);
					values = Arrays.copyOf(values, values.length + 1);
					values[values.length - 1] = errorInfo;
					errorFileWriter.writeNext(values);
				} else {
					// write the clean data
					System.out.println("The clean id " + values[0]);
					values = Arrays.copyOf(values, values.length + 3);
					values[values.length - 1] = String.valueOf(ageRange);
					values[values.length - 2] = String.valueOf(age);
					values[values.length - 3] = sex;
					cleanFileWriter.writeNext(values);
				}

			}
			inputFileReader.close();
			districtFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();

		} else {
			inputFileReader.close();
			districtFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
			throw new DataCleanException("The schema length is not mapping");
		}

	}

	public static void main(String[] args) throws DataCleanException, IOException {
		// TODO Auto-generated method stub
		clean(origin, clean, error);
	}

}
