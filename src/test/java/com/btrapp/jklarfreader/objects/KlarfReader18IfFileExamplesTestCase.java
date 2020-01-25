package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class KlarfReader18IfFileExamplesTestCase {

  private Optional<KlarfRecord> readTestKlarf() throws Exception {
    KlarfParser18Pojo kp18 = new KlarfParser18Pojo();
    KlarfReader.parseKlarf(kp18, this.getClass().getResourceAsStream("simple18.klarf"));
    Optional<KlarfRecord> klarfRecordO = kp18.build();
    return klarfRecordO;
  }

  private String toStr(List<? extends Object> array) {
    if (array == null) {
      return "";
    }
    return array.stream().map(Object::toString).collect(Collectors.joining(","));
  }

  @Test
  public void testFile() throws Exception {
    Optional<KlarfRecord> klarfRecordO = readTestKlarf();
    assertTrue(klarfRecordO.isPresent());
    KlarfRecord klarfRecord = klarfRecordO.get();

    //
    // Validate FileRecord
    //
    assertEquals("FileRecord", klarfRecord.getName());
    assertEquals("1.8", klarfRecord.getId());
    assertEquals(0, klarfRecord.getLists().size());
    assertEquals(1, klarfRecord.getFields().size());
    assertEquals(1, klarfRecord.getRecords().size());
    assertEquals(
        "12-12-1972,16:27:59",
        toStr(klarfRecord.getFields().getOrDefault("FileTimestamp", Collections.emptyList())));
  }

  @Test
  public void testLot() throws Exception {
    Optional<KlarfRecord> klarfRecordO = readTestKlarf();
    assertTrue(klarfRecordO.isPresent());
    KlarfRecord klarfRecord = klarfRecordO.get();

    Optional<KlarfRecord> lotRecordO =
        klarfRecord.findRecordByNameAndId("LotRecord", "aLot.String");
    assertTrue(lotRecordO.isPresent());
    // System.out.println(writer.writeValueAsString(lotRecordO.get()));
    KlarfRecord lotRecord = lotRecordO.get();
    assertEquals(1, lotRecord.getLists().size());
    assertEquals(11, lotRecord.getFields().size());
    assertEquals(2, lotRecord.getRecords().size()); // Two wafers in here

    //
    // Expected fields and values
    //
    Map<String, String> expectedFields = new HashMap<String, String>();
    expectedFields.put("DeviceID", "aDevice");
    expectedFields.put("DiePitch", "5744150,5410203");
    expectedFields.put("InspectionStationID", "NONE,A,B");
    expectedFields.put("OrientationMarkLocation", "0");
    expectedFields.put("RecipeID", "ARecipe,12-09-1971,02:08:00");
    expectedFields.put("RecipeVersion", ",NONE,");
    expectedFields.put("ResultTimestamp", "12-11-1971,12:32:22");
    expectedFields.put("SampleOrientationMarkType", "NOTCH");
    expectedFields.put("SampleSize", "300000000,0");
    expectedFields.put("SampleType", "WAFER");
    expectedFields.put("StepID", "AStepId");

    for (Map.Entry<String, String> e : expectedFields.entrySet()) {
      String fieldName = e.getKey();
      String fieldValue = e.getValue();
      assertEquals(
          fieldValue,
          toStr(lotRecord.getFields().get(fieldName)),
          "Field " + fieldName + " matches expectation");
    }

    // Make sure we don't have any fields we didn't expect
    assertEquals(
        expectedFields.size(), lotRecord.getFields().size(), "Only expected fields are found");

    // Check lists
    List<KlarfList> classLookups = lotRecord.findListsByName("ClassLookupList");
    assertEquals(1, classLookups.size());
    KlarfList classLookup = classLookups.get(0);
    // Columns are right
    assertEquals("CLASSNUMBER,CLASSNAME,CLASSCODE", toStr(classLookup.getColumnNames()));
    assertEquals("int32,string,string", toStr(classLookup.getColumnTypes()));
    assertEquals(4, classLookup.size());
    // Check list contents
    assertEquals("0,Unclassified,", toStr(classLookup.asRow(0)));
    assertEquals("1,NV,", toStr(classLookup.asRow(1)));
    assertEquals("2,FM,", toStr(classLookup.asRow(2)));
    assertEquals("3,Blobby,", toStr(classLookup.asRow(3)));
  }

  @Test
  public void testWafer() throws Exception {
    Optional<KlarfRecord> klarfRecordO = readTestKlarf();
    assertTrue(klarfRecordO.isPresent());
    Optional<KlarfRecord> lotRecordO =
        klarfRecordO.get().findRecordByNameAndId("LotRecord", "aLot.String");
    assertTrue(lotRecordO.isPresent());
    Optional<KlarfRecord> waferRecordO =
        lotRecordO.get().findRecordByNameAndId("WaferRecord", "FirstWaferId");
    assertTrue(waferRecordO.isPresent());

    KlarfRecord firstWafer = waferRecordO.get();
    Map<String, String> expectedFields =
        new HashMap<String, String>() {
          {
            put("DieOrigin", "0,0");
            put("OrientationInstructions", "");
            put("ProcessEquipmentState", "NONE,,,,,");
            put("SampleCenterLocation", "4929000,-490333");
            put("SlotNumber", "25");
          }
        };
    for (Entry<String, String> e : expectedFields.entrySet()) {
      assertEquals(e.getValue(), toStr(firstWafer.getFields().get(e.getKey())), e.getKey());
    }

    List<KlarfList> defectLists = firstWafer.findListsByName("DefectList");
    assertEquals(1, defectLists.size());
    KlarfList defectList = defectLists.get(0);
    assertEquals(3, defectList.size()); // List length is correct
    assertEquals("2", defectList.get("DEFECTID", 0).get().toString());
    assertEquals("3", defectList.get("DEFECTID", 1).get().toString());
    assertEquals("128", defectList.get("DEFECTID", 2).get().toString());

    assertEquals("1254960", defectList.get("XREL", 0).get().toString());
    assertEquals("622162", defectList.get("XREL", 1).get().toString());
    assertEquals("2782531", defectList.get("XREL", 2).get().toString());

    List<Object> row = defectList.asRow(0);
    assertEquals("2", row.get(0).toString());
    assertEquals("1254960", row.get(1).toString());

    assertFalse(defectList.get("NotARealColumn", 0).isPresent());
    assertFalse(defectList.get("DEFECTID", -1).isPresent());
  }
}
