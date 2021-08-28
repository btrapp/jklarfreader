package com.btrapp.jklarfreader.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A Klarf list record. Format 1.2 and below won't have columnTypes, and may or may not even have
 * column names
 *
 * @author btrapp
 */

/*
 * Example:
 *
 * List DefectList
 * {
 * Columns 41 { int32 DEFECTID, int32 XREL, int32 YREL, int32 XINDEX, int32 YINDEX,
 * int32 XSIZE, int32 YSIZE, float DEFECTAREA, int32 DSIZE, int32 CLASSNUMBER,
 * int32 TEST, int32 ROUGHBINNUMBER, int32 FINEBINNUMBER, int32 SAMPLEBINNUMBER, float CONTRAST,
 * int32 CHANNELID, int32 MANSEMCLASS, int32 AUTOONSEMCLASS, int32 MICROSIGCLASS, int32 MACROSIGCLASS,
 * int32 AUTOOFFSEMCLASS, int32 AUTOOFFOPTADC, int32 FACLASS, int32 INTENSITY, float KILLPROB,
 * int32 MACROSIGID, int32 REGIONID, int32 EVENTTYPE, int32 EBRLINE,
 * int32 POLARITY, float CRITICALAREA, int32 MANOPTCLASS, float PHI, int32 DBCLASS,
 * int32 DBGROUP, float DBCRITICALITYINDEX, float CELLSIZE, int32 CAREAREAGROUPCODE, float PCI,
 * float LINECOMPLEXITY, float DCIRANGE }
 * Data 3
 * {
 * 2 1254960 4346692 2 24 315 195 6445.0 325 91 1 0 0 0 0.0000 0 0 0 0 0 0 0 0
 * 0 0.0000 0 0 0 0 0 0.0000 0 0.0 0 0 0.0000 0.000000 0 0.0000 0.0000
 * 0.0000 ;
 * 3 622162 924193 -3 24 1170 1495 129236.0 1295 180 1 0 0 0 0.0000 0 0 0 0 0
 * 0 0 0 0 0.0000 0 0 0 0 0 0.0000 0 0.0 0 0 0.0000 0.000000 0 0.0000
 * 0.0000 0.0000 ;
 * 128 2782531 487465 -2 -27 5039 3959 177209.0 5039 0 2 0 0 0 0.0000 0 0 0 0
 * 0 0 0 0 0 0.0000 0 0 0 0 0 0.0000 0 0.0 0 0 0.0000 0.000000 0
 * 0.0000 0.0000 0.0000 ;
 * }
 * }
 *
 * Would map to:
 *
 * name: "DefectList"
 * columnNames: ["DEFECTID","XREL",...,"DCIRANGE"]
 * coumnTypes: [int32,int32,...,float]
 * data: {
 * "DEFECTID" : [2, 3, 128],
 * "XREL" : [ 1254960, 622162, 2782531]
 * ..etc
 * }
 *
 */
public final class KlarfList {
	private String name;
	private List<String> columnNames;
	private List<String> columnTypes;
	// Store the inner defects in a JSONLines format http://jsonlines.org/
	// Why JSONLines?  Well it compresses *REALLY* nicely, and it makes it easier to see all of one attribute for all records quickly
	private Map<String, List<Object>> colMap = new HashMap<>();

	public void addByIndex(int colIndex, Object value) {
		String colName = columnNames.get(colIndex);
		List<Object> list = colMap.get(colName);
		list.add(value);
	}

	/**
	 * A simple utility getter, handles missing col names and array out of bound problems
	 *
	 * @param col
	 *            the column name to retrieve
	 * @param index
	 *            the index within that row
	 * @return the value (wrapped with optional)
	 */
	public Optional<Object> get(String col, int index) {
		List<Object> theCol = colMap.get(col);
		if (theCol == null) {
			return Optional.empty();
		}
		if (index < 0)
			return Optional.empty();
		if (index >= theCol.size())
			return Optional.empty();
		return Optional.ofNullable(theCol.get(index));
	}

	public List<Object> asRow(int rowIndex) {
		List<Object> row = new ArrayList<>(columnNames.size());
		for (String c : columnNames) {
			row.add(colMap.get(c).get(rowIndex));
		}
		return row;
	}

	/** @return the number of rows in the list (uses the 1st item in the map) */
	public int size() {
		if (colMap.isEmpty())
			return 0;
		return colMap.values().iterator().next().size();
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
		for (String colName : columnNames) {
			colMap.computeIfAbsent(colName, l -> new ArrayList<>());
		}
	}

	public List<String> getColumnTypes() {
		return columnTypes;
	}

	public void setColumnTypes(List<String> columnTypes) {
		this.columnTypes = columnTypes;
	}

	public Map<String, List<Object>> getColMap() {
		return colMap;
	}

	public void setColMap(Map<String, List<Object>> colMap) {
		this.colMap = colMap;
	}

	public List<Object> getColumn(String colName) {
		return this.colMap.get(colName);
	}
}
