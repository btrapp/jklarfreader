package com.btrapp.jklarfreader;

import java.util.List;
import java.util.Optional;

public interface KlarfParserIf18<T> {

	/**
	 * This is called when we see a new Record line.
	 * 
	 * Ex: Record LotRecord "YourLot.Id" {
	 * 
	 * @param recordName
	 * @param recordId
	 *            (Blank if not provided in the Klarf)
	 */
	public void startRecord(String recordName, String recordId);

	public void endRecord();

	/**
	 * Called when a field is found.
	 * 
	 * Ex: Field OrientationInstructions 1 {""}
	 * 
	 * @param fieldName
	 * @param fieldCount
	 * @param fieldValues
	 */
	public void addField(String fieldName, int fieldCount, List<String> fieldValues);

	/**
	 * Called when a list definition is found.
	 * 
	 * Ex: List DefectList {
	 * Columns 42 { int32 DEFECTID, int32 XREL, int32 YREL, .....
	 *
	 * Embedded, simple lists (Like the "ImageList" defect attribute) are handled separately
	 * 
	 * @param listName
	 * @param colNames
	 * @param colTypes
	 * @param colCount
	 * @param rowCount
	 */
	public void startList(String listName, List<String> colNames, List<String> colTypes, int colCount, int rowCount);

	/**
	 * Called each time a lists's row is found
	 * 
	 * @param row
	 */
	public void addListRow(List<Object> row);

	public void endList();

	public Optional<T> build();

}
