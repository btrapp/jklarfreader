package com.btrapp.jklarfreader.impl;

import com.btrapp.jklarfreader.KlarfParserIf18;
import com.btrapp.jklarfreader.objects.KlarfList;
import com.btrapp.jklarfreader.objects.KlarfRecord;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An implementation of the KlarfParterIf18 interface which emits a Plain Old Java Object
 *
 * <p>You may use this class directly or build your own parser using it as a guideline.
 *
 * @author btrapp
 */
public class KlarfParser18Pojo implements KlarfParserIf18<KlarfRecord> {

  /*
   * We need a stack as we need to sorta work our way into inner records and back
   * & forward a few times until we're all done.
   */
  protected Deque<KlarfRecord> recordStack = new ArrayDeque<>();
  // Holds information about the list as we move from definition to contents
  private KlarfList workingList = null;

  public KlarfParser18Pojo() {
    super();
  }

  @Override
  public void startRecord(String recordName, String recordId) {
    KlarfRecord krecord = new KlarfRecord(recordName, recordId);
    if (this.recordStack.isEmpty()) {
      // This is the first one, start the stack with it.
      this.recordStack.add(krecord);
    } else {
      // Add this to the parent's "records" list
      this.recordStack.getLast().addRecord(krecord);
      // And move it onto the queue
      this.recordStack.add(krecord);
    }
  }

  @Override
  public void endRecord() {
    // When a record has ended, we move back up the stack
    if (this.recordStack.size() > 1) {
      this.recordStack.removeLast();
    }
  }

  @Override
  public void setField(
      String fieldName, int fieldCount, List<String> fieldValues, boolean isQuoted) {
    this.recordStack.getLast().setField(fieldName, fieldValues, isQuoted);
  }

  @Override
  public void startList(
      String listName, List<String> colNames, List<String> colTypes, int colCount, int rowCount) {
    this.workingList = new KlarfList();
    this.workingList.setName(listName);
    this.workingList.setColumnNames(colNames);
    this.workingList.setColumnTypes(colTypes);
    Map<String, List<Object>> map = new HashMap<>();
    for (String colName : colNames) {
      map.put(colName, new ArrayList<>(rowCount));
    }
    this.workingList.setColMap(map);
  }

  @Override
  public void addListRow(List<Object> row) {
    for (int i = 0; i < row.size(); i++) {
      this.workingList.addByIndex(i, row.get(i));
    }
  }

  @Override
  public void endList() {
    this.recordStack.getLast().addList(this.workingList);
    this.workingList = null;
  }

  @Override
  public Optional<KlarfRecord> build() {
    if (!this.recordStack.isEmpty()) {
      return Optional.ofNullable(this.recordStack.getLast());
    } else {
      System.err.println("RecordStack is not empty");
      return Optional.ofNullable(this.recordStack.getLast());
    }
  }
}
