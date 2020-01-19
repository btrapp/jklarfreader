package com.btrapp.jklarfreader;

import java.util.List;
import java.util.Optional;

/**
 * If you're familiar with XML parsing this inteface works a lot like the SAX based parser works. We
 * call start/end methods for the more complex records (Records, Lists) and simpler add methods for
 * the simpler records (Fields, List body rows)
 *
 * @author btrapp
 * @param <T> The class to emit
 */
public interface KlarfParserIf18<T> {

  /**
   * This is called when we see a new Record line.
   *
   * <p>Ex: Record LotRecord "YourLot.Id" {
   *
   * @param recordName (LotRecord)
   * @param recordId (YourLot.ID or Blank if not provided in the Klarf)
   */
  public void startRecord(String recordName, String recordId);

  public void endRecord();

  /**
   * Called when a field is found.
   *
   * <p>Ex: Field OrientationInstructions 1 {""}
   *
   * @param fieldName ex: OrientationInstructions
   * @param fieldCount ex: 1
   * @param fieldValues ex: [values arr]
   */
  public void addField(String fieldName, int fieldCount, List<String> fieldValues);

  /**
   * Called when a list definition is found.
   *
   * <p>Ex: List DefectList { Columns 42 { int32 DEFECTID, int32 XREL, int32 YREL, .....
   *
   * <p>Embedded, simple lists (Like the "ImageList" defect attribute) are handled separately
   *
   * @param listName ex: DefectList
   * @param colNames ex: [DEFECTID, XREL...]
   * @param colTypes ex: [int32, int32...]
   * @param colCount #cols expected
   * @param rowCount #rows expected
   */
  public void startList(
      String listName, List<String> colNames, List<String> colTypes, int colCount, int rowCount);

  /**
   * Called each time a lists's row is found
   *
   * @param row the row to add
   */
  public void addListRow(List<Object> row);

  /** Called when then end of the list is found */
  public void endList();

  /**
   * Called at the very end of the Klarf
   *
   * @return the record
   */
  public Optional<T> build();
}
