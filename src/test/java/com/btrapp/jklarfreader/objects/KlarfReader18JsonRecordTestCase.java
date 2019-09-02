package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;

public class KlarfReader18JsonRecordTestCase {

	@Test
	public void testEmptyRecord() throws Exception {
		String record = "Record FileRecord  \"1.8\"{}\n" +
				"EndOfFile;";
		Optional<KlarfRecord> k = KlarfReader.parseKlarf(new KlarfParser18Pojo(), new ByteArrayInputStream(record.getBytes()));

		assertTrue(k.isPresent());
		KlarfRecord node = k.get();
		//		ObjectMapper m = new ObjectMapper();
		//		System.out.println(m.writerWithDefaultPrettyPrinter().writeValueAsString(node));
		assertNotNull(node);
		assertEquals("FileRecord", node.getName());
		assertEquals("1.8", node.getId());
		assertTrue(node.getFields().isEmpty());
		assertTrue(node.getLists().isEmpty());
	}

	@Test
	public void testSimpleRecord() throws Exception {
		String record = "Record FileRecord  \"1.8\" { \n" +
				" Field F 1 { A } \n" +
				" Field G 3 {\"B C\",D,\"E\"} \n" +
				" } \n" +
				"EndOfFile;";
		KlarfParser18Pojo parser = new KlarfParser18Pojo();
		Optional<KlarfRecord> kO = new KlarfReader18<KlarfRecord>(parser).readKlarf(new ByteArrayInputStream(record.getBytes()));
		assertTrue(kO.isPresent());
		KlarfRecord node = kO.get();

		//		ObjectMapper m = new ObjectMapper();
		//		System.out.println(m.writerWithDefaultPrettyPrinter().writeValueAsString(node));
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
	}

	@Test
	public void testNestedRecord() throws Exception {
		String record = "" +
				"Record FileRecord  \"1.8\" { \n" +
				" Field F 1 { A } \n" +
				" Record NestedRecord \"1\" {\n" +
				"  Field G 3 {\"B C\",D,\"E\"} \n" +
				" }\n" +
				" Record NestedRecord \"2\" {\n" +
				"  Field G 3 {\"B C\",D,\"E\"} \n" +
				" } \n" +
				"}\n" +
				"EndOfFile;";
		Optional<KlarfRecord> kO = new KlarfReader18<KlarfRecord>(new KlarfParser18Pojo()).readKlarf(new ByteArrayInputStream(record.getBytes()));
		assertTrue(kO.isPresent());
		KlarfRecord node = kO.get();
		//		ObjectMapper m = new ObjectMapper();
		//		System.out.println(m.writerWithDefaultPrettyPrinter().writeValueAsString(node));
		assertNotNull(node);
		assertEquals("FileRecord", node.getName());
		assertEquals("1.8", node.getId());
		List<String> fieldF = node.getFields().get("F");
		assertNotNull(fieldF);
		assertEquals(1, fieldF.size());
		assertEquals("A", fieldF.get(0));
		assertEquals(2, node.getRecords().size());
		KlarfRecord nr1 = node.getRecords().stream()
				.filter(r -> r.getName().equals("NestedRecord"))
				.filter(r -> r.getId().equals("1"))
				.findFirst().orElse(null);
		KlarfRecord nr2 = node.getRecords().stream()
				.filter(r -> r.getName().equals("NestedRecord"))
				.filter(r -> r.getId().equals("2"))
				.findFirst().orElse(null);
		assertNotNull(nr1);
		assertNotNull(nr1.getFields().get("G"));
		assertNotNull(nr2);
		assertNotNull(nr2.getFields().get("G"));
	}

	@SuppressWarnings("boxing")
	@Test
	public void testSimpleLists() throws Exception {
		String record = "Record FileRecord  \"1.8\" { \n" +
				" List AList {\n" +
				"  Columns 2 { int32 Foo, float Bar }\n " +
				"  Data 3 { \n" +
				"   42 1.0;\n" +
				"   43 2.0;\n" +
				"   44 3.0;\n" +
				"  }\n" +
				" }\n" +
				" Field F 1 {A}\n" +
				"}\n" +
				"EndOfFile;";

		Optional<KlarfRecord> kO = new KlarfReader18<KlarfRecord>(new KlarfParser18Pojo()).readKlarf(new ByteArrayInputStream(record.getBytes()));
		assertTrue(kO.isPresent());
		KlarfRecord node = kO.get();
		//		ObjectMapper m = new ObjectMapper();
		//		System.out.println(m.writerWithDefaultPrettyPrinter().writeValueAsString(node));
		assertNotNull(node);
		assertEquals("FileRecord", node.getName());
		assertEquals("1.8", node.getId());
		List<String> fieldF = node.getFields().get("F");
		assertNotNull(fieldF);
		assertEquals(1, fieldF.size());
		assertEquals("A", fieldF.get(0));
		Optional<KlarfList> listO = node.getLists().stream().filter(l -> l.getName().equals("AList")).findFirst();
		assertTrue(listO.isPresent());
		assertEquals(List.of("Foo", "Bar"), listO.get().getColumnNames());
		assertEquals(List.of("int32", "float"), listO.get().getColumnTypes());
		assertEquals(3, listO.get().getData().size());
		assertEquals(42, listO.get().getData().get(0).get(0));
		assertEquals(43, listO.get().getData().get(1).get(0));
		assertEquals(44, listO.get().getData().get(2).get(0));
		float tol = 0.1f;
		assertEquals(1.0f, (Float) listO.get().getData().get(0).get(1), tol);
		assertEquals(2.0f, (Float) listO.get().getData().get(1).get(1), tol);
		assertEquals(3.0f, (Float) listO.get().getData().get(2).get(1), tol);
	}

}
