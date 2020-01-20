package com.btrapp.jklarfreader.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.btrapp.jklarfreader.KlarfParserIf18;
import com.btrapp.jklarfreader.objects.KlarfList;
import com.btrapp.jklarfreader.objects.KlarfRecord;

/**
 * An implementation of the KlarfParterIf18 interface which emits a Plain Old Java Object
 *
 * <p>
 * You may use this class directly or build your own parser using it as a guideline.
 *
 * @author btrapp
 */
public class KlarfParser18Pojo implements KlarfParserIf18<KlarfRecord> {

	/*
	 * We need a stack as we need to sorta work our way into inner records
	 * and back & forward a few times until we're all done.
	 */
	private Deque<KlarfRecord> recordStack = new ArrayDeque<>();
	// Holds information about the list as we move from definition to contents
	private KlarfList workingList = null;

	public KlarfParser18Pojo() {
		super();
	}

	@Override
	public void startRecord(String recordName, String recordId) {
		KlarfRecord record = new KlarfRecord(recordName, recordId);
		if (this.recordStack.isEmpty()) {
			// This is the first one!
			this.recordStack.add(record);
		} else {
			// Add this to the parent's "records" list
			this.recordStack.getLast().getRecords().add(record);
			// And move it onto the queue
			this.recordStack.add(record);
		}
	}

	@Override
	public void endRecord() {
		if (this.recordStack.size() > 1) {
			this.recordStack.removeLast();
		}
	}

	@Override
	public void addField(String fieldName, int fieldCount, List<String> fieldValues) {
		this.recordStack.getLast().getFields().put(fieldName, fieldValues);
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
		this.workingList.setData(map);
	}

	@Override
	public void addListRow(List<Object> row) {
		for (int i = 0; i < row.size(); i++) {
			this.workingList.addByIndex(i, row.get(i));
		}
	}

	@Override
	public void endList() {
		this.recordStack.getLast().getLists().add(this.workingList);
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
