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

public class TransClean {

	enum ErrorType {
		TRANS_ID_ERROR, ACCOUNT_ID_ERROR, DATA_ERROR, TYPE_ERROR, OPERATION_ERROR, AMOUNT_ERROR, BALANCE_ERROR, K_SYMBOL_ERROR, BANK_ERROR, LENGTH_ERROR, TRANS_ID_UNIQUE_ERROR, ACCOUNT_ID_NOT_FOUND_ERROR
	}

	private static final String origin = "trans.csv";
	private static final String clean = "transClean.csv";
	private static final String error = "transError.csv";
	private static final String cleanAccount = "accountClean.csv";

	private static final String PRIJEM = "PRIJEM";
	private static final String VYDAJ = "VYDAJ";

	private static final String PREVOD_NA_UCET = "PREVOD NA UCET";
	private static final String PREVOD_Z_UCTU = "PREVOD Z UCTU";
	private static final String VYBER = "VYBER";

	private static final String POJISTNE = "POJISTNE";
	private static final String SLUZBY = "SLUZBY";
	private static final String UROK = "UROK";
	private static final String SIPO = "SIPO";
	private static final String DUCHOD = "DUCHOD";
	private static final String UVER = "UVER";

	private static final String[] schema = { "trans_id", "account_id", "date", "type", "operation", "amount", "balance",
			"k_symbol", "bank", "account" };

	private static final DataType[] dataType = { DataType.INT, DataType.INT, DataType.DATA, DataType.STRING,
			DataType.STRING, DataType.INT, DataType.INT, DataType.STRING, DataType.STRING, DataType.INT };

	public static void clean(String input, String clean, String error) throws IOException, DataCleanException {
		File inputFile = new File(input);
		File cleanFile = new File(clean);
		File errorFile = new File(error);
		File cleanAccountFile = new File(cleanAccount);

		CSVReader inputFileReader = new CSVReader(new FileReader(inputFile));
		CSVReader cleanAccountFileReader = new CSVReader(new FileReader(cleanAccountFile));
		CSVWriter cleanFileWriter = new CSVWriter(new FileWriter(cleanFile),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER);
		CSVWriter errorFileWriter = new CSVWriter(new FileWriter(errorFile),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER);

		if (!inputFile.exists() || !cleanAccountFile.exists()) {
			System.out.println("The input file and  account clean file is not exist");
			throw new DataCleanException("The input file and  account clean file is not exist");
		}
		if (cleanFile.exists()) {
			cleanFile.delete();
		}
		if (errorFile.exists()) {
			errorFile.delete();
		}

		String[] values;
		String[] errorSchema = new String[schema.length + 1];
		Set<Integer> account_id_set = new HashSet<>();
		Set<Integer> trans_id_set = new HashSet<>();
		values = inputFileReader.readNext();
		if (values.length == schema.length) {
			for (int i = 0; i < schema.length; i++) {
				errorSchema[i] = schema[i];
				if (!values[i].equals(schema[i])) {
					throw new DataCleanException("The schema is not mapping");
				}
			}
			errorSchema[errorSchema.length - 1] = "error_info";

			cleanFileWriter.writeNext(schema);
			errorFileWriter.writeNext(errorSchema);

			// get the account id
			cleanAccountFileReader.readNext();
			List<String[]> list = cleanAccountFileReader.readAll();
			for (String[] strings : list) {
				account_id_set.add(Integer.valueOf(strings[0]));
			}

			// get values
			while ((values = inputFileReader.readNext()) != null) {
				String errorInfo = "";
				if (values.length != schema.length) {
					errorInfo = errorInfo + ErrorType.LENGTH_ERROR + " ";
				}
				// get trans id
				String trans_idString = values[0];
				if (trans_idString != null && trans_idString.matches("^[1-9]\\d*$")) {
					// unique
					int trans_idInt = Integer.valueOf(trans_idString);
					if (trans_id_set.contains(trans_idInt)) {
						errorInfo = errorInfo + ErrorType.TRANS_ID_UNIQUE_ERROR + " ";
					} else {
						trans_id_set.add(trans_idInt);
					}
				} else {
					errorInfo = errorInfo + ErrorType.TRANS_ID_ERROR + " ";
				}
				// get account id
				String account_idString = values[1];
				if (account_idString != null && account_idString.matches("^[1-9]\\d*$")) {
					// from clean account table
					int account_idInt = Integer.valueOf(account_idString);
					if (account_id_set.contains(account_idInt)) {

					} else {
						errorInfo = errorInfo + ErrorType.ACCOUNT_ID_NOT_FOUND_ERROR + " ";
					}
				} else {
					errorInfo = errorInfo + ErrorType.ACCOUNT_ID_ERROR + " ";
				}

				// check the data
				String dataString = values[2];
				if (dataString != null) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						format.parse(dataString);
					} catch (ParseException e) {
						errorInfo = errorInfo + ErrorType.DATA_ERROR;
					}
				} else {
					errorInfo = errorInfo + ErrorType.DATA_ERROR + " ";
				}
				// check the type
				String typeString = values[3];
				if (typeString != null && (PRIJEM.equals(typeString) || VYDAJ.equals(typeString))) {

				} else {
					errorInfo = errorInfo + ErrorType.TYPE_ERROR + " ";
				}
				// check the operation
				String operationString = values[4];
				if (operationString != null && (PREVOD_Z_UCTU.equals(operationString) || VYBER.equals(operationString)
						|| PREVOD_NA_UCET.equalsIgnoreCase(operationString))) {

				} else {
					errorInfo = errorInfo + ErrorType.OPERATION_ERROR + " ";
				}
				// check the ammount
				String accountString = values[5];
				if (accountString != null && accountString.matches("^[1-9]\\d*$")) {

				} else {
					errorInfo = errorInfo + ErrorType.AMOUNT_ERROR + " ";
				}
				// check the balance
				String balanceString = values[6];
				if (balanceString != null && balanceString.matches("^[-]{0,1}[0-9]{1,}$")) {

				} else {
					errorInfo = errorInfo + ErrorType.BALANCE_ERROR + " ";
				}
				// check the k symbol
				String k_symbolString = values[7];
				if (k_symbolString != null && (k_symbolString.equals(" ") || k_symbolString.equals(POJISTNE)
						|| k_symbolString.equals(SLUZBY) || k_symbolString.equals(UROK) || k_symbolString.equals(UVER)
						|| k_symbolString.equals(SIPO) || k_symbolString.equals(DUCHOD))) {

				} else {
					errorInfo = errorInfo + ErrorType.K_SYMBOL_ERROR + " ";
				}

				// check the bank
				String bankString = values[8];
				if (bankString != null && (bankString.matches("[A-Z][A-Z]") || bankString.equals(""))) {

				} else {
					errorInfo = errorInfo + ErrorType.BANK_ERROR + " ";
				}

				// check the a
				if (errorInfo.length() > 0) {
					System.out.println("The error id is " + values[0]);
					values = Arrays.copyOf(values, values.length + 1);
					values[values.length - 1] = errorInfo;
					errorFileWriter.writeNext(values);
				} else {
					System.out.println("The clean id is " + values[0]);
					cleanFileWriter.writeNext(values);
				}

			}
			inputFileReader.close();
			cleanAccountFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
		} else {
			inputFileReader.close();
			cleanAccountFileReader.close();
			cleanFileWriter.close();
			errorFileWriter.close();
			throw new DataCleanException("The schema is not mapping");
		}

	}

	public static void main(String[] args) throws IOException, DataCleanException {
		// TODO Auto-generated method stub
		clean(origin, clean, error);
	}

}
