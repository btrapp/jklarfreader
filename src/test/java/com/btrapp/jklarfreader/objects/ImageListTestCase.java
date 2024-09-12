package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import com.btrapp.jklarfreader.objects.KlarfImageList.KlarfImageRow;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ImageListTestCase {

  private Optional<KlarfRecord> readTestKlarf() throws Exception {
    KlarfParser18Pojo kp18 = new KlarfParser18Pojo();
    KlarfReader.parseKlarf(kp18, this.getClass().getResourceAsStream("klarfWithImagelist.klarf"));
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
        "2021-01-01,01:00:00",
        toStr(klarfRecord.getFields().getOrDefault("FileTimestamp", Collections.emptyList())));
  }

  @Test
  void testDefectRecordsHaveNasAsMissing() throws Exception {
    Optional<KlarfRecord> klarfRecordO = readTestKlarf();
    assertTrue(klarfRecordO.isPresent());
    Optional<KlarfRecord> lotRecordO =
        klarfRecordO.get().findRecordByNameAndId("LotRecord", "aLot");
    assertTrue(lotRecordO.isPresent());
    Optional<KlarfRecord> waferRecordO =
        lotRecordO.get().findRecordByNameAndId("WaferRecord", "aWafer");
    assertTrue(waferRecordO.isPresent());

    KlarfRecord firstWafer = waferRecordO.get();

    List<KlarfList> defectLists = firstWafer.findListsByName("DefectList");
    assertEquals(1, defectLists.size());
    KlarfList defectList = defectLists.get(0);
    assertEquals(6, defectList.size());

    assertNotNull(defectList.getColumn("DEFECTID"));
    assertEquals(
        "int32", defectList.getColumnTypes().get(defectList.getColumnNames().indexOf("DEFECTID")));
    assertNotNull(defectList.getColumn("IMAGEINFO"));
    assertEquals(
        "ImageList",
        defectList.getColumnTypes().get(defectList.getColumnNames().indexOf("IMAGEINFO")));
  }

  @Test
  void testKlarfWriteback() throws Exception {
    Optional<KlarfRecord> klarfRecordO = readTestKlarf();
    assertTrue(klarfRecordO.isPresent());
    StringWriter sw = new StringWriter();
    KlarfWriter18 writer = new KlarfWriter18();
    writer.writeKlarf(klarfRecordO.get(), sw);
    List<String> klarfAsString = Arrays.asList(sw.toString().split("\n"));
    assertFalse(klarfAsString.isEmpty());
    assertTrue(klarfAsString.size() > 50);
    assertTrue(klarfAsString.stream().anyMatch(l -> l.contains("Images 3")));
    assertTrue(klarfAsString.stream().anyMatch(l -> l.contains("ImageList"))); // Has header
    assertTrue(klarfAsString.stream().anyMatch(l -> l.contains("EndOfFile;"))); // Has EOF
  }

  @Test
  void testKlarfWritebackChangeImages() throws Exception {
    Optional<KlarfRecord> klarfRecordO = readTestKlarf();
    assertTrue(klarfRecordO.isPresent());

    KlarfRecord kr = klarfRecordO.get();
    KlarfRecord wafer =
        kr.findRecordsByName("LotRecord").get(0).findRecordsByName("WaferRecord").get(0);
    KlarfList defList = wafer.findListsByName("DefectList").get(0);
    // Defect 12 has an imagelist we want to overwrite.
    // Defect 15 has no images we want to add one.
    List<Object> defectIds = defList.getColumn("DEFECTID");
    List<Object> imageList = new ArrayList<>();
    for (int i = 0; i < defectIds.size(); i++) {
      Integer defectId = (Integer) defectIds.get(i);
      KlarfImageList kil = new KlarfImageList();
      if (defectId.intValue() == 12) {
        kil.setImages(List.of(new KlarfImageRow("img1.jpg", "jpg", 1, "I1")));
      } else if (defectId.intValue() == 15) {
        kil.setImages(
            List.of(
                new KlarfImageRow("img2.jpg", "jpg", 1, "I1"),
                new KlarfImageRow("img3.jpg", "jpg", 1, "I2")));
      }
      imageList.add(kil);
    }
    defList.set("IMAGEINFO", "ImageList", imageList);

    StringWriter sw = new StringWriter();
    KlarfWriter18 writer = new KlarfWriter18();
    writer.writeKlarf(klarfRecordO.get(), sw);
    // System.out.println(sw.toString());
    List<String> klarfAsString = Arrays.asList(sw.toString().split("\n"));
    assertFalse(klarfAsString.isEmpty());
    assertTrue(klarfAsString.size() > 50);
    assertTrue(klarfAsString.stream().anyMatch(l -> l.contains("Images 1")));
    assertTrue(klarfAsString.stream().anyMatch(l -> l.contains("Images 2")));
    assertTrue(klarfAsString.stream().anyMatch(l -> l.contains("ImageList"))); // Has header
    assertTrue(klarfAsString.stream().anyMatch(l -> l.contains("EndOfFile;"))); // Has EOF
  }
}
