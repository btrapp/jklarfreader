package com.btrapp.jklarfreader.objects;

import com.btrapp.jklarfreader.KlarfParserIf18;
import com.btrapp.jklarfreader.objects.Klarf12Mapper.KlarfDataType;
import com.btrapp.jklarfreader.objects.Klarf12Mapper.KlarfMappingRecord;
import com.btrapp.jklarfreader.objects.KlarfException.ExceptionCode;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class KlarfReader12<T> {

  private KlarfParserIf18<T> parser;

  private static final String END_OF_LINE = ";";

  private Klarf12Mapper mapper;
  private Map<String, KlarfMappingRecord> klarfMapping;
  private boolean waferRecordFound = false;

  public KlarfReader12(KlarfParserIf18<T> parser) throws Exception {
    super();
    this.parser = parser;
    mapper = Klarf12Mapper.getInstance();
    klarfMapping = mapper.getMappingRecordsByKlarf12Key();
  }

  private KlarfMappingRecord getObjectType(String tag) {
    return Optional.ofNullable(klarfMapping.get(tag))
        .orElse(Klarf12Mapper.unsupportedKlarfMappingRecord);
  }

  public Optional<T> readKlarf(InputStream is) throws Exception {
    KlarfTokenizer st = new KlarfTokenizer(is);
    read(st);
    return parser.build();
  }

  private void read(KlarfTokenizer kt) throws IOException, KlarfException {
    while (kt.nextToken()) {
      String val = kt.val().toUpperCase();
      KlarfMappingRecord kmr = getObjectType(val);
      KlarfDataType kdt = kmr.klarfDataType();
      switch (kdt) {
        case Record, Ignored_Record:
          // System.out.println(val + " " + kdt.toString());
          readRecord(kt, kmr);
          break;
        case Field, Ignored_Field:
          // System.out.println(val + " " + kdt.toString());
          readField(kt, kmr);
          break;
        case List:
          // System.out.println(val + " " + kdt.toString());
          readList(kt, kmr);
          break;
        case EndOfFile:
          // done
          parser.endRecord(); // end WaferRecord
          parser.endRecord(); // end LotRecord
          parser.endRecord(); // end FileRecord
          // kt.skipTo(END_OF_LINE);
          return;
        case Unsupported:
          // TODO show last known good thing and add to that
          System.out.println(
              "unsupported token '"
                  + val
                  + "'"
                  + " current line "
                  + kt.getCurrentLine()
                  + " line num "
                  + kt.getLineNumber());
          break;
        default:
          // throw new IllegalArgumentException("Unexpected value: " + kot);
          // throw new KlarfException("Missing trailing }", kt, ExceptionCode.GenericError);
          throw new KlarfException("Unexpected value", kt, ExceptionCode.GenericError);
      }
    }
  }

  protected void readRecord(KlarfTokenizer kt, KlarfMappingRecord kmr)
      throws IOException, KlarfException {
    // String recordType = kt.nextVal();
    // String recordIdOrBrace = kt.nextVal();
    if (kmr.klarfDataType() == KlarfDataType.Ignored_Record) {
      kt.skipTo(END_OF_LINE);
      return;
    }
    if ("FILEVERSION".equalsIgnoreCase(kmr.klarfKey12())) {
      // List<String> vals = readTokensUntil(kt, END_OF_LINE);
      String major = kt.nextVal();
      String minor = kt.nextVal();
      kt.skipTo(END_OF_LINE);
      // parser.startRecord("FileRecord", (major + "." + minor));
      parser.startRecord("FileRecord", "1.8"); // writing to 1.8 format
    } else if (kmr.klarfKey12().endsWith("SPEC")) {
      // this line is prior to a list and it has quantity of columns and the column names in the
      // next list
      // int numColumns = kt.nextIntVal().intValue();
      // this logic is done in list processing because need the col names
    } else {
      // TODO parser.endRecord();//end the previous record for certain types???
      String id = kt.nextVal();
      kt.skipTo(END_OF_LINE);
      // System.out.println("start record " + id + " " + kmr.klarfKey18());
      if (kmr.klarfKey18().equalsIgnoreCase("WaferRecord")) {
        // only support 1 wafer per file
        if (!waferRecordFound) {
          waferRecordFound = true;
        } else {
          throw new KlarfException("More than 1 wafer record", kt, ExceptionCode.GenericError);
        }
      }
      parser.startRecord(kmr.klarfKey18(), id);
    }
  }

  protected void readField(KlarfTokenizer kt, KlarfMappingRecord kmr)
      throws KlarfException, IOException {
    if (KlarfDataType.Ignored_Field.equals(kmr.klarfDataType())) {
      kt.skipTo(END_OF_LINE);
      return;
    }
    List<String> vals = new ArrayList<>();
    String v = null;
    boolean isQuoted = false;
    while (!(v = kt.nextVal()).equals(END_OF_LINE)) {
      if (kt.isQuoted()) {
        isQuoted = true;
      }
      vals.add(v);
    }
    //		if (kmr.klarfKey18().equalsIgnoreCase("SampleSize")) {
    //			List<String> newVals = new ArrayList<>();
    //			newVals.add(vals.get(1) + "000000");
    //			newVals.add("0");
    //			vals = newVals;
    //		}
    if (mapper.getUnitConversion().containsKey(kmr.klarfKey18().toUpperCase())) {
      String colType = mapper.getUnitConversion().get(kmr.klarfKey18().toUpperCase());
      List<String> newVals = new ArrayList<>();
      for (String val : vals) {
        String newVal = convertUnitsToNm(kmr.klarfKey18(), val, colType);
        newVals.add(newVal);
      }
      if (kmr.klarfKey18().equalsIgnoreCase("SampleSize")) {
        // sample size in 2nd position in 1.2 and size is in mm
        Collections.reverse(newVals);
        newVals.set(1, "0");
      }
      vals = newVals;
    }
    parser.setField(kmr.klarfKey18(), vals.size(), vals, isQuoted);
  }

  private static boolean isNumber(String val, String type) {
    if (val == null || val.isBlank() || val.isEmpty()) return false;
    String regex = "[0-9+/\\-.eE,]+";
    if (val.matches(regex)) {
      // if the chars look ok continue to try and parse it
      if (type.equalsIgnoreCase("int32")) {
        try {
          Integer.parseInt(val);
        } catch (NumberFormatException e) {
          return false;
        }
      } else if (type.equalsIgnoreCase("float")) {
        try {
          Float.parseFloat(val);
        } catch (NumberFormatException e) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public static String convertUnitsToNm(String name, String val, String type)
      throws KlarfException {
    name = name.toUpperCase();
    // System.out.println("in convert units for " + name);
    // NumberFormat decFormat = new DecimalFormat("#.##########");
    // if (NumberUtils.isCreatable(val)) {
    if (isNumber(val, type)) {
      int mult = 1000;
      if (name.equalsIgnoreCase("SampleSize")) {
        mult = 1000000;
      } else if (name.contains("AREA")) {
        mult = 1000 * 1000;
      }
      if (type.equalsIgnoreCase("INT32")) {
        try {
          int n = Integer.parseInt(val) * mult;
          // System.out.println("int n is now " + n);
          return Integer.toString(n);
        } catch (NumberFormatException nfe) {
          throw new KlarfException(
              "Value '" + val + "' was expected to be an integer but was not.",
              null,
              ExceptionCode.NumberFormat);
        }
      } else if (type.equalsIgnoreCase("FLOAT")) {
        try {
          BigDecimal n =
              new BigDecimal(val)
                  .multiply(
                      BigDecimal.valueOf(
                          mult)); // use bigdecimal to prevent floating point rounding, ex
          // 1.1110100000e+010
          // float n = Float.parseFloat(val) * mult;
          // System.out.println("float n is now " + n);
          if (name.contains("AREA")) {
            return String.format("%.10e", n);
          } else {
            // return decFormat.format(n);
            // all floats in should be ints out for klarf 1.8
            return Integer.toString(n.intValue());
          }
        } catch (NumberFormatException nfe) {
          throw new KlarfException(
              "Value '" + val + "' was expected to be a float but was not.",
              null,
              ExceptionCode.NumberFormat);
        }
      }
    } else {
      // System.out.println("val is not parseable " + val);//Could be NA
    }
    return null;
  }

  protected void readList(KlarfTokenizer kt, KlarfMappingRecord kmr)
      throws IOException, KlarfException {
    int rowCount = 0; // might not know the row count
    List<String> colTypes12 = new ArrayList<>();
    List<String> colTypes18 = new ArrayList<>();
    List<String> colNames = new ArrayList<>();
    Map<String, String[]> listColMapping = mapper.getColumnTypesForList(kmr.klarfKey12());

    if (kmr.klarfKey12().equalsIgnoreCase("SampleTestPlan")
        || kmr.klarfKey12().equalsIgnoreCase("RemovedDieList")
        || kmr.klarfKey12().equalsIgnoreCase("ClassLookup")) {
      rowCount = kt.nextIntVal().intValue();
      for (Entry<String, String[]> e : listColMapping.entrySet()) {
        colNames.add(e.getKey());
        colTypes12.add(e.getValue()[0]);
        colTypes18.add(e.getValue()[1]);
      }
      parser.startList(kmr.klarfKey18(), colNames, colTypes18, colNames.size(), rowCount);
      for (int i = 0; i < rowCount; i++) {
        List<Object> row = new ArrayList<>();
        for (int j = 0; j < colNames.size(); j++) {
          // System.out.println("cur line " + kt.getCurrentLine());
          String colType12Uc = colTypes12.get(j).toUpperCase();
          String colType18Uc = colTypes18.get(j).toUpperCase();
          if (colType12Uc.endsWith("LIST")) {
            List<List<String>> embeddedList = readEmbeddedList(kt);
            row.add(embeddedList);
          } else {
            String val = kt.nextVal();
            // System.out.println(val + " " + colTypeUc + " " + row.size() + " " +
            // colNames.get(row.size()));
            if (val.equals(";") || val.equals("{") || val.equals("}")) {
              throw new KlarfException(
                  "ERR in list "
                      + kmr.klarfKey12()
                      + " END ("
                      + val
                      + ") found at item "
                      + i
                      + " col "
                      + j
                      + " of "
                      + rowCount,
                  kt,
                  ExceptionCode.ListFormat);
            }
            String colName = colNames.get(j);
            if (mapper.getUnitConversion().containsKey(colName)) {
              String unitColType = mapper.getUnitConversion().get(colName.toUpperCase());
              String newVal = convertUnitsToNm(colName, val, unitColType);
              if (colType18Uc.equalsIgnoreCase("INT32")) {
                // row.add(Math.round(Float.valueOf(newVal)));//round to int (rounding already
                // handled - not needed here)
                row.add(Integer.valueOf(newVal));
              } else {
                row.add(Float.valueOf(newVal));
              }
            } else {
              if (colType12Uc.equalsIgnoreCase("INT32")) {
                row.add(kt.intVal());
              } else if (colType12Uc.equalsIgnoreCase("FLOAT")) {
                row.add(kt.floatVal());
              } else if (colType12Uc.equalsIgnoreCase("STRING")) {
                row.add(kt.val());
              } else {
                // If its not a list, int, or float, it must be a string
                row.add(val);
              }
            }
          }
        }
        parser.addListRow(row);
        // kt.skipTo(";");
      }
      parser.endList();
      // kt.nextToken();
      kt.skipTo(END_OF_LINE);
      return;
    }

    if (kmr.klarfKey12().equalsIgnoreCase("SUMMARYSPEC")
        || kmr.klarfKey12().equalsIgnoreCase("DefectRecordSpec")) {
      boolean summSpec = kmr.klarfKey12().equalsIgnoreCase("SUMMARYSPEC") ? true : false;
      boolean defSpec = kmr.klarfKey12().equalsIgnoreCase("DefectRecordSpec") ? true : false;
      if (summSpec) parser.startRecord("SummaryRecord", ""); // it's part of the SummaryRecord
      int colCount = kt.nextIntVal().intValue();
      for (int i = 0; i < colCount; i++) {
        String colName = kt.nextVal();
        String colType[] = listColMapping.getOrDefault(colName, new String[] {"STRING", "STRING"});
        // System.out.println("colname to type " + colName + " " + colType);
        colNames.add(colName);
        colTypes12.add(colType[0]);
        colTypes18.add(colType[1]);
      }
      kt.skipTo(END_OF_LINE);
      if (defSpec) kt.skipTo("DefectList");
      if (summSpec) kt.skipTo("SummaryList");
      List<List<Object>> rows = new ArrayList<>();
      String val = null;
      // int colCounter = 1;
      List<Object> row = new ArrayList<>();
      while (!(val = kt.nextVal()).equals(END_OF_LINE)) {
        // System.out.println("cur line " + kt.getCurrentLine());
        String colType12Uc = colTypes12.get(row.size()).toUpperCase();
        String colType18Uc = colTypes18.get(row.size()).toUpperCase();
        // System.out.println(val + " " + colType12Uc + " " + row.size() + " " +
        // colNames.get(row.size()));
        if (colType12Uc.endsWith("LIST")) {
          List<List<String>> embeddedList = readEmbeddedList(kt);
          row.add(embeddedList);
        } else {
          String colName = colNames.get(row.size());
          if (mapper.getUnitConversion().containsKey(colName)) {
            String unitColType = mapper.getUnitConversion().get(colName.toUpperCase());
            // System.out.println("in " + colName + " " + val + " " + unitColType);
            String newVal =
                convertUnitsToNm(
                    colName,
                    val,
                    unitColType); // the new value should be submitted with the klarf 18 units
            // System.out.println("  out " + newVal);
            if (colType18Uc.equalsIgnoreCase("INT32")) {
              // row.add(Math.round(Float.valueOf(newVal)));//round to int (rounding already handled
              // - not needed here)
              row.add(Integer.valueOf(newVal)); // round to int
            } else {
              row.add(Float.valueOf(newVal));
            }
          } else {
            if (colType12Uc.equalsIgnoreCase("INT32")) {
              row.add(kt.intVal());
            } else if (colType12Uc.equalsIgnoreCase("FLOAT")) {
              row.add(kt.floatVal());
            } else if (colType12Uc.equalsIgnoreCase("STRING")) {
              row.add(kt.val());
            } else {
              // If its not a list, int, or float, it must be a string
              row.add(val);
            }
          }
        }
        // System.out.println("val " + val + " row size " + row.size());
        if (row.size() == colCount) {
          // System.out.println("============reset row========");
          // parser.addListRow(row);
          rows.add(row);
          // colCounter = 1;
          row = new ArrayList<>();
        }
        // colCounter++;
      }
      parser.startList(kmr.klarfKey18(), colNames, colTypes18, colNames.size(), row.size());
      rows.forEach(r -> parser.addListRow(r));
      parser.endList();
      if (summSpec) parser.endRecord();
      return;
    }

    throw new KlarfException(
        "Klarf list type not supported " + kmr.klarfKey12(), kt, ExceptionCode.ListFormat);
  }

  private List<List<String>> readEmbeddedList(KlarfTokenizer kt)
      throws IOException, KlarfException {
    // have not seen a case for this yet - see KlarfReader18 readEmbeddedList if needed
    int imageQty = kt.intVal();
    // System.out.println("read embedded list qty is " + imageQty);
    if (imageQty == 0) {
      return Collections.emptyList();
    }
    for (int i = 0; i < imageQty; i++) {
      // there are two columns - skip them
      for (int j = 0; j < 2; j++) {
        String v = kt.nextVal();
        // System.out.println("skip " + v);
      }
    }
    return Collections.emptyList();
  }
}
