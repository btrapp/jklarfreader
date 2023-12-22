package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class WierdKlarfTestCases {

  private Optional<KlarfRecord> readTestKlarf() throws Exception {
    KlarfParser18Pojo kp18 = new KlarfParser18Pojo();
    KlarfReader.parseKlarf(kp18, this.getClass().getResourceAsStream("wierdKlarfWithNAs.klarf"));
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
  void testFile() throws Exception {
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
  void testDefectRecordsHaveNasAsMissing() throws Exception {
    Optional<KlarfRecord> klarfRecordO = readTestKlarf();
    assertTrue(klarfRecordO.isPresent());
    Optional<KlarfRecord> lotRecordO =
        klarfRecordO.get().findRecordByNameAndId("LotRecord", "aLot.String");
    assertTrue(lotRecordO.isPresent());
    Optional<KlarfRecord> waferRecordO =
        lotRecordO.get().findRecordByNameAndId("WaferRecord", "FirstWaferId");
    assertTrue(waferRecordO.isPresent());

    KlarfRecord firstWafer = waferRecordO.get();

    List<KlarfList> defectLists = firstWafer.findListsByName("DefectList");
    assertEquals(1, defectLists.size());
    KlarfList defectList = defectLists.get(0);

    assertTrue(defectList.get("DBGROUP", 0).isPresent());
    assertFalse(defectList.get("DBGROUP", 1).isPresent());
    assertTrue(defectList.get("DBGROUP", 2).isPresent());

    assertTrue(defectList.get("DCIRANGE", 0).isPresent());
    assertTrue(defectList.get("DCIRANGE", 1).isPresent());
    assertFalse(defectList.get("DCIRANGE", 2).isPresent());
  }
}
