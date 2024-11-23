package com.btrapp.jklarfreader.objects;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.KlarfReader.KlarfFormat;
import com.btrapp.jklarfreader.objects.Klarf12Mapper.KlarfDataType;
import com.btrapp.jklarfreader.objects.Klarf12Mapper.KlarfMappingRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import org.junit.jupiter.api.Test;

public class KlarfParserTest {
  @Test
  public void testKlarf12Version() throws IOException {
    KlarfFormat kf =
        KlarfReader.findKlarfFormat(
            new BufferedInputStream(this.getClass().getResourceAsStream("simple12.klarf")));
    assertEquals(KlarfFormat.V1_2, kf);
    kf =
        KlarfReader.findKlarfFormat(
            new File("src/test/resources/com/btrapp/jklarfreader/objects/simple12.klarf"));
    assertEquals(KlarfFormat.V1_2, kf);
  }

  @Test
  public void testKlarf18Version() throws IOException, URISyntaxException {
    URL url = this.getClass().getResource("simple18.klarf");
    KlarfFormat kf = KlarfReader.findKlarfFormat(Paths.get(url.toURI()).toFile());
    assertEquals(KlarfFormat.V1_8, kf);
  }

  @Test
  public void testReadKlarf12() throws Exception {
    try (BufferedInputStream fis =
        new BufferedInputStream(this.getClass().getResourceAsStream("simple12.klarf")); ) {
      Optional<KlarfRecord> klarfRecord = KlarfReader.parseKlarf(fis, KlarfFormat.V1_2);
      assertTrue(klarfRecord.isPresent());
    }
  }

  @Test
  public void testReadKlarf18() throws Exception {
    try (BufferedInputStream fis =
        new BufferedInputStream(this.getClass().getResourceAsStream("simple18.klarf")); ) {
      Optional<KlarfRecord> klarfRecord = KlarfReader.parseKlarf(fis, KlarfFormat.V1_8);
      assertTrue(klarfRecord.isPresent());
    }
  }

  @Test
  public void testFailToReadKlarf12() throws Exception {
    // we might only want to parse 1.8 format and force fail if encounter other file types
    try (BufferedInputStream fis =
        new BufferedInputStream(this.getClass().getResourceAsStream("simple12.klarf")); ) {
      Throwable exception =
          assertThrows(KlarfException.class, () -> KlarfReader.parseKlarf(fis, KlarfFormat.V1_8));
      assertTrue(exception.getMessage().startsWith("Unsupported parsing of V1_2"));
    }
  }

  @Test
  void testTokenize12() {
    // String currentLine = "FileTimestamp 06-20-24 01:01:01;";

    // text block contains a new line at the end
    // String currentLine = """
    //		InspectionStationID "tooltype" "toolmodel" "toolid";
    //		""";
    String currentLine = "InspectionStationID \"tooltype\" \"toolmodel\" \"toolid\"";
    StringTokenizer st = new StringTokenizer(currentLine, ", \t{}\";", false);

    assertEquals(4, st.countTokens());
    assertEquals("InspectionStationID", st.nextToken());

    // String token;
    // while (st.hasMoreTokens()) {
    //	token = st.nextToken();
    //	System.out.println("token '" + token + "'");
    // }
  }

  @Test
  public void testBadKlarf12() throws Exception {
    try (BufferedInputStream fis =
        new BufferedInputStream(this.getClass().getResourceAsStream("bad12.klarf")); ) {
      Throwable exception = assertThrows(KlarfException.class, () -> KlarfReader.parseKlarf(fis));
      assertTrue(exception.getMessage().startsWith("More than 1 wafer record"));
    }
  }

  @Test
  public void testKlarf12Mapper() throws Exception {
    Klarf12Mapper inst = Klarf12Mapper.getInstance();
    Map<String, KlarfMappingRecord> map = inst.getMappingRecordsByKlarf12Key();
    assertEquals(KlarfDataType.EndOfFile, map.get("ENDOFFILE").klarfDataType());

    Map<String, String[]> colTypes = inst.getColumnTypesForList("ClassLookup");
    assertEquals(2, colTypes.size());
    assertEquals(
        "CLASSNAME", colTypes.entrySet().stream().skip(1).findFirst().orElse(null).getKey());
  }

  @Test
  public void testUnitConversion() throws ParseException, KlarfException {
    String val = "150";
    String colType = "int32";
    String colName = "SampleSize";
    String newVal = KlarfReader12.convertUnitsToNm(colName, val, colType);
    assertEquals("150000000", newVal);
    Integer.parseInt(newVal);

    val = "7.7777000000e+003";
    colType = "float";
    colName = "DiePitch";
    newVal = KlarfReader12.convertUnitsToNm(colName, val, colType);
    assertEquals("7777700", newVal);
    Integer.parseInt(newVal);

    val = "5.246328E2";
    colType = "float";
    colName = "YREL";
    newVal = KlarfReader12.convertUnitsToNm(colName, val, colType);
    assertEquals("524632", newVal); // note the rounding
    Integer.parseInt(newVal);

    val = "1.1110100000e+010";
    colType = "float";
    colName = "AreaPerTest";
    newVal = KlarfReader12.convertUnitsToNm(colName, val, colType);
    assertEquals("1.1110100000e+16", newVal);
    Float.parseFloat(newVal);

    val = "1.1234500E1";
    colType = "float";
    colName = "DSIZE";
    newVal = KlarfReader12.convertUnitsToNm(colName, val, colType);
    assertEquals("11234", newVal);
    Integer.parseInt(newVal);

    val = "0.1234000000E0";
    colType = "float";
    colName = "DSIZE";
    newVal = KlarfReader12.convertUnitsToNm(colName, val, colType);
    assertEquals("123", newVal);
    Integer.parseInt(newVal);

    List<String> vals = Arrays.asList("", "NA", " 1", " N", null, "5_000_000");
    for (String v : vals) {
      colType = "float";
      colName = "DSIZE";
      newVal = KlarfReader12.convertUnitsToNm(colName, v, colType);
      assertNull(newVal);
    }

    vals = Arrays.asList("-5", "10.5", "5e+03", "50e-1", "-5.123E2", "-5.123E-2", ".1");
    for (String v : vals) {
      colType = "float";
      colName = "DSIZE";
      newVal = KlarfReader12.convertUnitsToNm(colName, v, colType);
      assertNotNull(newVal);
    }
  }

  @Test
  void testKlarfWriters() throws Exception {
    // write to klarf
    Optional<KlarfRecord> klarfRecordOpt =
        KlarfReader.parseKlarf(
            new BufferedInputStream(this.getClass().getResourceAsStream("klarf12Example.klarf")),
            KlarfFormat.V1_2);
    assertTrue(klarfRecordOpt.isPresent());

    klarfRecordOpt
        .get()
        .setField(
            "ConvertTimestampUtc",
            Arrays.asList("Converted from v1.2", Instant.now().toString()),
            true);

    StringWriter sw = new StringWriter();
    KlarfWriter18 writer = new KlarfWriter18();
    writer.writeKlarf(klarfRecordOpt.get(), sw);
    // System.out.println(sw);
    List<String> klarfAsString = Arrays.asList(sw.toString().split("\n"));
    assertTrue(klarfAsString.stream().anyMatch(l -> l.contains("DefectList"))); // Has DefectList
    assertTrue(klarfAsString.stream().anyMatch(l -> l.contains("EndOfFile;"))); // Has EOF
    assertTrue(
        klarfAsString.stream().anyMatch(l -> l.contains("ConvertTimestampUtc"))); // Has DefectList

    int defectCount = 0;
    KlarfRecord klarfRecord = klarfRecordOpt.get();
    assertNotNull(klarfRecord);
    assertEquals("FileRecord", klarfRecord.getName());
    assertEquals("1.8", klarfRecord.getId());
    assertEquals(2, klarfRecord.findField("ConvertTimestampUtc").size());
    for (KlarfRecord lotRecord : klarfRecord.findRecordsByName("LotRecord")) {
      for (KlarfRecord waferRecord : lotRecord.findRecordsByName("WaferRecord")) {
        for (KlarfList defectList : waferRecord.findListsByName("DefectList")) {
          defectCount += defectList.size();
        }
      }
    }
    assertEquals(2, defectCount);

    // write to json
    ObjectWriter objWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
    sw.getBuffer().setLength(0);
    sw.getBuffer().trimToSize();
    objWriter.writeValue(sw, klarfRecordOpt.get());
    // System.out.println(sw);
    klarfAsString = Arrays.asList(sw.toString().split("\n"));
    assertTrue(klarfAsString.stream().anyMatch(l -> l.contains("DefectList"))); // Has DefectList
    assertTrue(
        klarfAsString.stream().anyMatch(l -> !l.contains("EndOfFile;"))); // Does not have EOF

    // write to image
  }

  @Test
  void testDefectList12() throws Exception {
    Optional<KlarfRecord> klarfRecordOpt =
        KlarfReader.parseKlarf(
            new BufferedInputStream(
                this.getClass().getResourceAsStream("klarf12DefectListExample.klarf")),
            KlarfFormat.V1_2);
    assertTrue(klarfRecordOpt.isPresent());
    assertTrue(klarfRecordOpt.isPresent());
    Optional<KlarfRecord> lotRecordO =
        klarfRecordOpt.get().findRecordByNameAndId("LotRecord", "aLot");
    assertTrue(lotRecordO.isPresent());
    Optional<KlarfRecord> waferRecordO =
        lotRecordO.get().findRecordByNameAndId("WaferRecord", "aWafer");
    assertTrue(waferRecordO.isPresent());

    KlarfRecord firstWafer = waferRecordO.get();

    List<KlarfList> defectLists = firstWafer.findListsByName("DefectList");
    assertEquals(1, defectLists.size());
    KlarfList defectList = defectLists.get(0);
    assertEquals(13, defectList.size());

    KlarfWriter18 kw = new KlarfWriter18();
    StringWriter sw = new StringWriter();
    // try (FileWriter fw = new FileWriter(new File("/tmp/testklarf.klarf"))) {
    //	kw.writeKlarf(klarfRecordOpt.get(), fw);
    // }
    kw.writeKlarf(klarfRecordOpt.get(), sw);
    assertTrue(!sw.toString().contains("Field TiffFileName"));
    // System.out.println(sw);
  }
}
