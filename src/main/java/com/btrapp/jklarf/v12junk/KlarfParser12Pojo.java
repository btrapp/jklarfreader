package com.btrapp.jklarf.v12junk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.btrapp.jklarfreader.objects.KlarfList;
import com.btrapp.jklarfreader.objects.KlarfRecord;

public class KlarfParser12Pojo implements KlarfParserIf12 {
	private List<KlarfNode> lists = new ArrayList<>();
	private KlarfList workingList;
	public KlarfParser12Pojo() {
	}
	
	@Override
	public void addField(KlarfRecord record) {
	}
	
	@Override
	public void startList(KlarfRecord record, String listName, List<String> colNames, int colCount, int rowCount) {
		workingList = new KlarfList();
		workingList.setColumnNames(colNames);
//		workingList.setColumnTypes();
		workingList.setData(new ArrayList<List<Object>>(rowCount));
	}

	@Override
	public void addListRow(KlarfRecord record, List<Object> row) {
		workingList.getData().add(row);
	}

	@Override
	public void endList(KlarfRecord record) {
		lists.add(new KlarfNode(record, workingList));
	}
	
	
	
	public List<KlarfNode> getKlarfRecord() {
		return this.lists;
	}
	
	/**
	 * Helper class used in the 1.2 parser to emit a list with its matching record
	 * @author btrapp
	 *
	 */
	public final class KlarfNode implements Serializable {
		private static final long serialVersionUID = -3249795924259486405L;
		
		private KlarfRecord record;
		private KlarfList list;
		
		public KlarfNode(KlarfRecord record, KlarfList list) {
			super();
			this.record = record;
			this.list = list;
		}
		public KlarfRecord getRecord() {
			return record;
		}
		public KlarfList getList() {
			return list;
		}
	}

}
