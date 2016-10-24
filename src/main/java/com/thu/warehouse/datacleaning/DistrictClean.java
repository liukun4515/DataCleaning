package com.thu.warehouse.datacleaning;

public class DistrictClean {

	private String[] schema = { "district_id", "district_name", "region", "hab_number", "city_number", "ave_salary",
			"umemploy_rate", "crime_number" };
	private DataType[] type = { DataType.INT, DataType.STRING, DataType.STRING, DataType.INT, DataType.INT,
			DataType.INT, DataType.FLOAT, DataType.INT };

	private int schemaLength;
	private int dataTypeLength;

	public static void main(String[] args) {

	}

}
