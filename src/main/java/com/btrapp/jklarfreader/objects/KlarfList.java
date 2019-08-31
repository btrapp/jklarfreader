package com.btrapp.jklarfreader.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * A Klarf list record.
 * Format 1.2 and below won't have columnTypes, and may or may not even have column names
 * 
 * @author btrapp
 * 
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
 * data: [
 * [2,1254960,...,0.0000]
 * ...
 * [128,2782531,...,0.0000]
 * ]
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