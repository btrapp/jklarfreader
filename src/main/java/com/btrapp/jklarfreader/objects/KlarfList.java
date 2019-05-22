package com.btrapp.jklarfreader.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * A Klarf list record.
 * Format 1.2 and below won't have columnTypes, and may or may not even have column names
 * @author btrapp
 *
 */
public final class KlarfList {
	private String name;
	private List<String> columnNames;
	private List<String> columnTypes;
	private List<List<Object>> data = new ArrayList<>();
	public int size() {
		return data.size();
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public List<String> getColumnTypes() {
		return columnTypes;
	}

	public void setColumnTypes(List<String> columnTypes) {
		this.columnTypes = columnTypes;
	}

	public List<List<Object>> getData() {
		return data;
	}

	public void setData(List<List<Object>> data) {
		this.data = data;
	}
}