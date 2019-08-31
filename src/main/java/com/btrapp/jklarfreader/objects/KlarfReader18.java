package com.btrapp.jklarfreader.objects;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.btrapp.jklarfreader.KlarfParserIf18;
import com.btrapp.jklarfreader.objects.KlarfException.ExceptionCode;

public class KlarfReader18<T> {

	private KlarfParserIf18<T> parser;

	public KlarfReader18(KlarfParserIf18<T> parser) {
		super();
		this.parser = parser;
	}

	public Optional<T> readKlarf(InputStream is) throws Exception {
		KlarfTokenizer st = new KlarfTokenizer(is);
		read(st);
		return parser.build();
	}

	private void read(KlarfTokenizer kt) throws IOException, KlarfException {
		//The service has already removed the "FileRecord "1.8" bits.  put them in here first.
		//Look for the 3 main types of entries: Records, Fields, and Lists
		while (kt.nextToken()) {
			String val = kt.val();
			if (val.equalsIgnoreCase("Record")) {
				readRecord(kt); //A record could be a KEY={obj} or a KEY={subkey1: obj, subkey2: obj}
			} else if (val.equalsIgnoreCase("Field")) {
				readField(kt);
			} else if (val.equalsIgnoreCase("List")) {
				readList(kt);
			} else if (val.equals("}")) {
				parser.endRecord();
			} else if (val.equalsIgnoreCase("EndOfFile")) {
				return;
			} else {
				throw new KlarfException("What is " + val + " doing here?", kt, ExceptionCode.GenericError);
			}
		}
		throw new KlarfException("Missing trailing }", kt, ExceptionCode.GenericError);
	}

	protected void readRecord(KlarfTokenizer kt) throws IOException, KlarfException {
		String recordType = kt.nextVal();
		String recordIdOrBrace = kt.nextVal();
		if (recordIdOrBrace.equals("{")) {
			//No record Id.. just "KEY : VALUE"
			//Like 
			parser.startRecord(recordType, "");
		} else {
			//This had a record ID
			//like     Record WaferRecord "WAFERID01"
			kt.skipTo("{");
			parser.startRecord(recordType, recordIdOrBrace);
		}
	}

	protected void readField(KlarfTokenizer kt) throws KlarfException, IOException {
		String fieldName = kt.nextVal();
		int fieldCount = kt.nextIntVal();
		List<String> fieldVals = new ArrayList<>(fieldCount);
		kt.skipTo("{");
		for (int i = 0; i < fieldCount; i++) {
			if (i > 0)
				kt.skipTo(",");
			kt.nextToken();
			fieldVals.add(kt.val());
		}
		kt.skipTo("}");
		parser.addField(fieldName, fieldCount, fieldVals);
	}

	protected void readList(KlarfTokenizer kt) throws IOException, KlarfException {
		String listName = kt.nextVal();
		kt.skipTo("Columns");
		int colCount = kt.nextIntVal();
		kt.skipTo("{");
		List<String> colTypes = new ArrayList<>();
		List<String> colNames = new ArrayList<>();
		for (int i = 0; i < colCount; i++) {
			if (i > 0)
				kt.skipTo(",");
			colTypes.add(kt.nextVal());
			colNames.add(kt.nextVal());
		}
		kt.skipTo("}");

		//		System.out.println("Types: " + colTypes.toString());
		//		System.out.println("Columns: " + colNames.toString());
		kt.skipTo("Data");
		int rowCount = kt.nextIntVal();
		kt.skipTo("{");

		parser.startList(listName, colNames, colTypes, colCount, rowCount);

		for (int i = 0; i < rowCount; i++) {
			List<Object> row = new ArrayList<>();
			for (int j = 0; j < colNames.size(); j++) {
				String colType = colTypes.get(j);
				if (colType.endsWith("List")) {
					//This is an embedded list.  Embedded lists don't seem to act like the other lists, with cols and all that jazz..
					List<List<String>> embeddedList = readEmbeddedList(kt);
					row.add(embeddedList);
				} else {
					String val = kt.nextVal();
					//System.out.println("I=" + i + ",J=" + j + " VAL=" + val);
					if (val.equals(";") || val.equals("{") || val.equals("}")) {
						throw new KlarfException("ERR in list " + listName + " END (" + val + ") found at item " + i + " col " + j + " of " + rowCount, kt,
								ExceptionCode.ListFormat);
					}
					if (colType.equalsIgnoreCase("int32")) {
						row.add(Integer.valueOf(kt.intVal()));
					} else if (colType.equalsIgnoreCase("float")) {
						row.add(Float.valueOf(kt.floatVal()));
					} else if (colType.equalsIgnoreCase("string")) {
						row.add(kt.val());
					} else {
						//If its not a list, int, or float, it must be a string
						row.add(val);
					}
				}
			}
			parser.addListRow(row);
			kt.skipTo(";");
		}
		parser.endList();
		kt.skipTo("}"); //End of data
		kt.skipTo("}"); //End of list
		kt.nextToken();
		//System.out.println("List ends at " + kt.getLineNumber() + ": " + kt.getCurrentLine());
	}

	/**
	 * Reads a compacted form of the list structure, often embedded in the defect list to hold images.
	 * 
	 * Ex: Images 3 { "" "" "" "", "" "" "" "", "" "" "" ""}
	 * 
	 * @param kt
	 * @return
	 * @throws IOException
	 * @throws KlarfException
	 */
	private List<List<String>> readEmbeddedList(KlarfTokenizer kt) throws IOException, KlarfException {
		String listName = kt.nextVal();
		//I've seen both "N" and 0 used to indicate that there aren't any images.
		if (listName.equalsIgnoreCase("N") || listName.equals("0")) {
			//THis is a null list?
			return Collections.emptyList();
		}
		int listSize = kt.nextIntVal();
		kt.skipTo("{");
		List<List<String>> outerList = new ArrayList<>(listSize);
		List<String> innerList = new ArrayList<>();
		while (kt.nextToken()) {
			String val = kt.val();
			if (val.equals("}")) {
				//End list
				outerList.add(new ArrayList<>(innerList)); //Add a copy of the inner list
				innerList.clear();
				if (outerList.size() != listSize) {
					throw new KlarfException("List " + listName + " expected len of " + listSize + " but was " + outerList.size(), kt, ExceptionCode.ListFormat);
				}
				return outerList;
			} else if (val.equals(",")) { //Start a new row
				outerList.add(new ArrayList<>(innerList)); //Add a copy of the inner list
				innerList.clear();
			} else {
				innerList.add(val);
			}
		}
		throw new KlarfException("End of EmbeddedList not found", kt, ExceptionCode.ListFormat);
	}
}
