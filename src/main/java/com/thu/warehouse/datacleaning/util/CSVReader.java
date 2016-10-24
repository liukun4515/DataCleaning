package com.thu.warehouse.datacleaning.util;

import com.thu.warehouse.datacleaning.CSVException;

public class CSVReader {

	private String filename = null;

	public CSVReader() {
	}

	public CSVReader(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void init() throws CSVException {
		if (filename == null) {
			throw new CSVException("Please set the filename first");
		}
		// init the reader
	}

	public void readSchema() {

	}

	public void readLine() {

	}

	public void close() {

	}

}
