package com.btrapp.jklarf.v12junk;

public class KlarfReader12 {
//
//	//Build the line up into one array per "line" where a line is defined by the ending ;
//	protected static final Pattern simpleListPattern = Pattern.compile("^(\\w+)\\s+(\\d+)$", Pattern.CASE_INSENSITIVE); //WORD(notSpec)spaceNUMBERnosemicolon
//	protected static final Pattern specStartsPattern = Pattern.compile("^(\\w+)Spec\\s+(\\d+).*", Pattern.CASE_INSENSITIVE); //WORDspaceNUMBERnosemicolon
//	protected static final Pattern firstWordPattern = Pattern.compile("^(\\w+)\\s+.*");
//
//	//
//	private KlarfParserIf12 parser = null;
//
//	public KlarfReader12() {
//
//	}
//
//	public void readKlarf(KlarfParserIf12 parser, BufferedReader br) throws Exception {
//		this.parser = parser;
//		read(br);
//	}
//
//	protected Matcher matchesSimpleList(String line) {
//		if (line.toUpperCase().contains("SPEC")) {
//			//Don't let this match.
//			return simpleListPattern.matcher("");
//		}
//		return simpleListPattern.matcher(line.trim());
//	}
//
//	protected Matcher matchesSimpleSpec(String line) {
//		return specStartsPattern.matcher(line.trim());
//	}
//
//	private void read(BufferedReader br) throws Exception {
//		//Look for the 3 main types of entries: Records, Fields, and Lists
//		String line = "";
//		System.out.println("======================================");
//		Matcher m;
//		KlarfRecord klarfRecord = new KlarfRecord("KlarfRecord", "1.2");
//		int rowNumber=0;
//		while ((line = br.readLine()) != null) {
//			rowNumber++;
//			line = line.trim();
//			if (line.isEmpty())
//				continue;
//			if (line.startsWith("#"))
//				continue;
//			m = firstWordPattern.matcher(line);
//			if (m.matches()) {
//				String fieldName = m.group(1);
//				String fieldNameUc = fieldName.toUpperCase();
//				if (fieldNameUc.endsWith("SPEC")) {
//					System.out.println("SPEC " + fieldNameUc);
//					//this is the start of a complex list.
//					processSpec(klarfRecord, line, br);
//				} else if (matchesSimpleList(line).matches()) {
//					m = matchesSimpleList(line);
//					m.matches();
//					String listName = m.group(1);
//					int listSize = Integer.parseInt(m.group(2));
//					System.out.println("LIST " + listName + " (" + listSize + ")");
//					processList(klarfRecord, listName, listSize, br);
//					while (!line.endsWith(";"))
//						line = br.readLine();
//				} else if (line.endsWith(";")) {
//					//This looks like a simple field
//					KlarfTokenizer ktInner = new KlarfTokenizer(new StringReader(line));
//					List<String> row = new ArrayList<>();
//					while (ktInner.nextToken()) {
//						row.add(ktInner.val());
//					}
//					if (row.size() > 3) {
//						continue;
//					}
//					row.remove(0); //Remove name
//					row.remove(row.size()-1); //REmove trailing ;
//					klarfRecord.getFields().put(fieldName, row);
//				}
//			}
//		}
//		//
//		//		while ((line = br.readLine()) != null) {
//		//			line = line.trim();
//		//			Matcher m = null;
//		//			m = specStartsPattern.matcher(line);
//		//			if (m.matches()) {
//		//				//This is the start of a complexList.
//		//				KlarfTokenizer kt = new KlarfTokenizer(new StringReader(line));
//		//				String listName = kt.nextVal().replaceAll("(?i)Spec", "");
//		//				int listSize = kt.nextIntVal();
//		//				int colCount = kt.nextIntVal();
//		//
//		//			}
//		//			//			if (val.equalsIgnoreCase("FileVersion")) {
//		//			//				parser.startRecord("FileRecord", kt.nextVal()+"."+kt.nextVal()); //Map to 1.8 format for the version
//		//			//			} else if (val.toUpperCase().endsWith("SPEC")) {
//		//			//				//This is the beginning of a proper list
//		//			//				readList(kt);
//		//			//			} else if (val.equalsIgnoreCase("ClassLookup")) {
//		//			//				readSimpleList("ClassLookupList", kt);
//		//			//			} else if (val.toUpperCase().endsWith("LIST")) {
//		//			//				readSimpleList("ClassLookupList", kt);
//		//			//			}
//		//			//			
//		//			//
//		//			//				readRecord(kt); //A record could be a KEY={obj} or a KEY={subkey1: obj, subkey2: obj}
//		//			//			} else if (val.equalsIgnoreCase("Field")) {
//		//			//				readField(kt);
//		//			//			} else if (val.equalsIgnoreCase("List")) {
//		//			//				readList(kt);
//		//			//			} else if (val.equals("}")) {
//		//			//				parser.endRecord();
//		//			//			} else if (val.equalsIgnoreCase("EndOfFile")) {
//		//			//				return;
//		//			//			} else {
//		//			//				throw new KlarfException("What is " + val + " doing here?", kt, ExceptionCode.GenericError);
//		//			//			}
//		//		}
//
//	}
//
//	private void processList(KlarfRecord record, String listName, int listSize, BufferedReader br) throws IOException {
//		parser.startList(record, listName, Collections.emptyList(), 0, listSize);
//		String line = "";
//		while (!line.endsWith(";")) {
//			line = br.readLine().trim();
//			KlarfTokenizer ktLine = new KlarfTokenizer(new StringReader(line));
//			List<Object> row = new ArrayList<>();
//			while (ktLine.nextToken()) {
//				String val = ktLine.val();
//				if (!val.equals(";"))
//					row.add(val);
//			}
//			parser.addListRow(record, row);
//			System.out.println(" Added row");
//		}
//		parser.endList(record);
//	}
//
//	private void processSpec(KlarfRecord record, String line, BufferedReader br) throws Exception {
//		Matcher m = specStartsPattern.matcher(line);
//		int expectedLen = 0;
//		if (m.matches()) {
//			//They have specified the length of the spec columns;
//			expectedLen = Integer.parseInt(m.group(2));
//		} else {
//			//No length is specified - read till ";"
//		}
//		StringBuilder completeSpecLine = new StringBuilder();
//		completeSpecLine.append(line);
//		while (!line.endsWith(";")) {
//			line = br.readLine();
//			completeSpecLine.append(line);
//		}
//		System.out.println("COmplete spec line: " + completeSpecLine.toString());
//		List<String> colNames = null;
//		List<String> colTypes = null;
//		try (KlarfTokenizer kt = new KlarfTokenizer(new StringReader(completeSpecLine.toString()))) {
//			List<String> specLine = new ArrayList<>();
//			while (kt.nextToken()) {
//				specLine.add(kt.val());
//			}
//			String specName = specLine.remove(0);
//			if (expectedLen > 0) {
//				specLine.remove(0); //Remove the lenth
//			}
//			if (!specLine.isEmpty()) {
//				specLine.remove(specLine.size() - 1); //Remove the trailing ;
//			}
//			System.out.println("COmplete spec line for '"+specName+"': " + specLine.toString());
//			colNames = specLine;
//			colTypes = new ArrayList<>(colNames.size());
//			for (String colName: colNames) {
//				colTypes.add(KlarfParserIf18.UNKNOWN_TYPE);
//			}
//		}
//		line = br.readLine(); //DefectList or DefectList # or DefectList;
//		Matcher listLineM = simpleListPattern.matcher(line);
//		int rowCount = 0;
//		String listName = line;
//		if (listLineM.matches()) {
//			listName = listLineM.group(1);
//			rowCount = Integer.parseInt(listLineM.group(2));
//		}
//		parser.startList(record, listName, colNames, colNames.size(), rowCount);
//		while (!line.endsWith(";")) {
//			line = br.readLine().trim();
//			try (KlarfTokenizer ktLine = new KlarfTokenizer(new StringReader(line))) {
//				List<Object> row = new ArrayList<>();
//				while (ktLine.nextToken()) {
//					String val = ktLine.val();
//					if (!val.equals(";"))
//						row.add(val);
//				}
//				parser.addListRow(record, row);
//			}
//		}
//		parser.endList(record);
//	}
}
