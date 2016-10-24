package com.thu.warehouse.datacleaning;

public class AccountClean {
	/*
	 * 这个表主要清洗的内容：
	 * 1.account_id  key value,not null, unique
	 * 2.district_id foreign key
	 * 3.frequency ,enum 类型:POPLATEK MESICNE,POPLATEK TYDNE,POPLATEK PO OBRATU
	 * 4.data Date格式??
	 */
	
	enum ErrorType{
		ACCOUNT_ID_ERROR,DISTRICT_ID_ERROR,FREQUENCY_ERROR,DATA_ERROR
	}
	
	public static void clean(){
		
	}

	public static void main(String[] args) {

	}

}
