package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class KlarfReader18JsonRecordTestCase {

  @Test
  void testEmptyRecord() throws Exception {
    String recordTxt = """
    		Record FileRecord  "1.8" {
    		}
    		EndOfFile;
    		""";
    Optional<KlarfRecord> k =
        KlarfReader.parseKlarf(
            new KlarfParser18Pojo(), new ByteArrayInputStream(recordTxt.getBytes()));

    assertTrue(k.isPresent());
    KlarfRecord node = k.get();
    assertNotNull(node);
    assertEquals("FileRecord", node.getName());
    assertEquals("1.8", node.getId());
    assertTrue(node.getFields().isEmpty());
    assertTrue(node.getLists().isEmpty());
  }

  @Test
  void testSimpleRecord() throws Exception {
    String recordTxt =
        """
    		Record FileRecord  "1.8" {
             Field F 1 { A }
             Field G 3 {"B C",D,"E"}
             }
            EndOfFile;
            """;
    KlarfParser18Pojo parser = new KlarfParser18Pojo();
    Optional<KlarfRecord> kO =
        new KlarfReader18<KlarfRecord>(parser)
            .readKlarf(new ByteArrayInputStream(recordTxt.getBytes()));
    assertTrue(kO.isPresent());
    KlarfRecord node = kO.get();

    assertEquals("FileRecord", node.getName());
    assertEquals("1.8", node.getId());
    List<String> fieldF = node.getFields().get("F");
    assertNotNull(fieldF);
    assertEquals(1, fieldF.size());
    assertEquals("A", fieldF.get(0));
    List<String> fieldG = node.getFields().get("G");
    assertNotNull(fieldG);
    assertEquals(3, fieldG.size());
    assertEquals("B C", fieldG.get(0));
    assertEquals("D", fieldG.get(1));
    assertEquals("E", fieldG.get(2));
    assertEquals(3, node.reqField("G", 3).size());
    assertThrows(
        KlarfContentException.class,
        () -> {
          node.reqField("G", 2);
        });
  }

  @Test
  void testNestedRecord() throws Exception {
    String recordTxt =
        """
    		Record FileRecord  \"1.8\" {
             Field F 1 { A }
             Record NestedRecord \"1\" {
              Field G 3 {\"B C\",D,\"E\"}
             }
             Record NestedRecord \"2\" {
              Field G 3 {\"B C\",D,\"E\"}
             }
            }
            EndOfFile;
            """;
    Optional<KlarfRecord> kO =
        new KlarfReader18<KlarfRecord>(new KlarfParser18Pojo())
            .readKlarf(new ByteArrayInputStream(recordTxt.getBytes()));
    assertTrue(kO.isPresent());
    KlarfRecord node = kO.get();
    assertNotNull(node);
    assertEquals("FileRecord", node.getName());
    assertEquals("1.8", node.getId());
    List<String> fieldF = node.getFields().get("F");
    assertNotNull(fieldF);
    assertEquals(1, fieldF.size());
    assertEquals("A", fieldF.get(0));
    assertEquals(2, node.getRecords().size());
    KlarfRecord nr1 =
        node.getRecords().stream()
            .filter(r -> r.getName().equals("NestedRecord"))
            .filter(r -> r.getId().equals("1"))
            .findFirst()
            .orElse(null);
    KlarfRecord nr2 =
        node.getRecords().stream()
            .filter(r -> r.getName().equals("NestedRecord"))
            .filter(r -> r.getId().equals("2"))
            .findFirst()
            .orElse(null);
    assertNotNull(nr1);
    assertNotNull(nr1.getFields().get("G"));
    assertNotNull(nr2);
    assertNotNull(nr2.getFields().get("G"));

    assertEquals(2, node.reqRecord("NestedRecord", 2).size());
    assertThrows(
        KlarfContentException.class,
        () -> {
          node.reqRecord("NestedRecord", 3);
        });
  }

  @Test
  void testSimpleLists() throws Exception {
    String recordTxt =
        """
    		Record FileRecord  "1.8" {
             List AList {
              Columns 2 { int32 Foo, float Bar }
              Data 3 {
               42 1.0;
               43 2.0;
               44 3.0;
              }
             }
             Field F 1 {A}
            }
            EndOfFile
            """;

    Optional<KlarfRecord> kO =
        new KlarfReader18<KlarfRecord>(new KlarfParser18Pojo())
            .readKlarf(new ByteArrayInputStream(recordTxt.getBytes()));
    assertTrue(kO.isPresent());
    KlarfRecord node = kO.get();
    assertNotNull(node);
    assertEquals("FileRecord", node.getName());
    assertEquals("1.8", node.getId());
    List<String> fieldF = node.getFields().get("F");
    assertNotNull(fieldF);
    assertEquals(1, fieldF.size());
    assertEquals("A", fieldF.get(0));
    Optional<KlarfList> listO =
        node.getLists().stream().filter(l -> l.getName().equals("AList")).findFirst();
    assertTrue(listO.isPresent());
    assertEquals(List.of("Foo", "Bar"), listO.get().getColumnNames());
    assertEquals(List.of("int32", "float"), listO.get().getColumnTypes());
    assertEquals(3, listO.get().size());
    assertEquals(42, listO.get().getColumn("Foo").get(0));
    assertEquals(43, listO.get().getColumn("Foo").get(1));
    assertEquals(44, listO.get().getColumn("Foo").get(2));
    float tol = 0.1f;
    assertEquals(1.0f, (Float) listO.get().get("Bar", 0).orElse(0f), tol);
    assertEquals(2.0f, (Float) listO.get().get("Bar", 1).orElse(0f), tol);
    assertEquals(3.0f, (Float) listO.get().get("Bar", 2).orElse(0f), tol);
    assertEquals(1, node.reqList("AList", 1).size());
    assertThrows(
        KlarfContentException.class,
        () -> {
          node.reqList("AList", 2);
        });
  }

  @Test
  void testReqNumberMethods() throws Exception {
    String recordTxt =
        """
    		Record FileRecord  "1.8" {
             Field A 1 { 3.14 }
             Field B 3 {3,1,4}
             }
            EndOfFile;
            """;
    Optional<KlarfRecord> kO =
        new KlarfReader18<KlarfRecord>(new KlarfParser18Pojo())
            .readKlarf(new ByteArrayInputStream(recordTxt.getBytes()));
    assertTrue(kO.isPresent());
    KlarfRecord node = kO.get();
    List<Double> fa = node.reqDoubleField("A", 1);
    assertEquals(1, fa.size());
    assertEquals(3.14, fa.get(0).doubleValue(), 0.001);
    List<Integer> fb = node.reqIntField("B", 3);
    assertEquals(3, fb.size());
    assertEquals(3, fb.get(0));
    assertEquals(1, fb.get(1));
    assertEquals(4, fb.get(2));
  }

  @Test
  void testQuoting() throws Exception {
    String recordTxt =
        """
    		Record ARecord  "1.8"
            {
    		  Field SingleLineNo 1 {0}
              Field SingleLineYes 1 {"Apple"}
              Field SingleLineMixed 2 {"2",3}
              Field MultiLineNo 3 {00
                ,11
                ,22
                }
              Field MultiLineYes 3 {"A",
                "B",
                "C"}
              Field MultiLineMixed 4 {"1"
                ,"2",3
                ,4}
            }
            EndOfFile;
            """;
    Optional<KlarfRecord> kO =
        new KlarfReader18<KlarfRecord>(new KlarfParser18Pojo())
            .readKlarf(new ByteArrayInputStream(recordTxt.getBytes()));
    assertTrue(kO.isPresent());
    KlarfRecord node = kO.get();
    assertTrue(node.getFields().containsKey("SingleLineNo"));
    assertEquals(false, node.isQuotedField("SingleLineNo"));
    assertTrue(node.getFields().containsKey("SingleLineYes"));
    assertEquals(true, node.isQuotedField("SingleLineYes"));
    assertTrue(node.getFields().containsKey("SingleLineMixed"));
    assertEquals(true, node.isQuotedField("SingleLineMixed"));
    assertTrue(node.getFields().containsKey("MultiLineNo"));
    assertEquals(false, node.isQuotedField("MultiLineNo"));
    assertTrue(node.getFields().containsKey("MultiLineYes"));
    assertEquals(true, node.isQuotedField("MultiLineYes"));
    assertTrue(node.getFields().containsKey("MultiLineMixed"));
    assertEquals(true, node.isQuotedField("MultiLineMixed"));
  }
}
