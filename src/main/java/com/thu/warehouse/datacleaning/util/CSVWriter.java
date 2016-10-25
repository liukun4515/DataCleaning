package com.thu.warehouse.datacleaning.util;

/**
 * @deprecated
 * @author liukun
 *
 */
public class CSVWriter {

	private String filename = null;

	public CSVWriter() {

	}

	public CSVWriter(String filename) {
		super();
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
			throw new CSVException("please set the filename first");
		}
	}

	public void writeSchema() {

	}

	public void writeLine() {

	}

	public void close() {

	}

	public static void main(String[] args) {

	}

}
