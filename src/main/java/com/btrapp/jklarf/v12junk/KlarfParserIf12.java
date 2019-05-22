package com.btrapp.jklarf.v12junk;

import java.util.List;

import com.btrapp.jklarfreader.objects.KlarfRecord;

public interface KlarfParserIf12 {
	public static final String UNKNOWN_TYPE = "?";

	public void startList(KlarfRecord record, String listName, List<String> colNames, int colCount, int rowCount);

	public void addListRow(KlarfRecord record, List<Object> row);

	public void endList(KlarfRecord record);

	public void addField(KlarfRecord record);
	
}
