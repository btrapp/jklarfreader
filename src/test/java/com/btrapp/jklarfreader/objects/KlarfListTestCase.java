package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class KlarfListTestCase {
	@Test
	public void testAddingNewAttr() {
		KlarfList kl = new KlarfList();
		kl.setColumnNames(List.of("A", "B"));
		kl.setColumnTypes(List.of("string", "int32"));
		Map<String, List<Object>> map = new HashMap<>();
		map.put("A", List.of("a0", "a1", "a2"));
		map.put("B", List.of(0, 1, 2));
		kl.setColMap(map);

		assertNotNull(kl.getColumn("A"));
		assertNotNull(kl.getColumn("B"));
		assertNull(kl.getColumn("C"));
		assertEquals("a0", kl.getColMap().get("A").get(0));
		assertEquals(0, kl.getColMap().get("B").get(0));

		kl.set("C", "float", List.of(0f, 1f, 2f));

		assertNotNull(kl.getColumn("A"));
		assertNotNull(kl.getColumn("B"));
		assertNotNull(kl.getColumn("C"));
		assertEquals("a0", kl.getColMap().get("A").get(0));
		assertEquals(0, kl.getColMap().get("B").get(0));
		assertEquals("a0", kl.getColMap().get("A").get(0));
		assertEquals(0f, (Float) kl.getColMap().get("C").get(0), 0.01f);

	}

	@Test
	public void testReplacingAttrs() {
		KlarfList kl = new KlarfList();
		kl.setColumnNames(List.of("A", "B"));
		kl.setColumnTypes(List.of("string", "int32"));
		Map<String, List<Object>> map = new HashMap<>();
		map.put("A", List.of("a0", "a1", "a2"));
		map.put("B", List.of(0, 1, 2));
		kl.setColMap(map);

		kl.set("B", "float", List.of(10f, 11f, 12f));

		assertEquals("a0", kl.getColMap().get("A").get(0));
		assertEquals(10f, (Float) kl.getColMap().get("B").get(0), 0.01f);

	}

	@Test
	public void testRemovingAttrs() {
		KlarfList kl = new KlarfList();
		kl.setColumnNames(List.of("A", "B"));
		kl.setColumnTypes(List.of("string", "int32"));
		Map<String, List<Object>> map = new HashMap<>();
		map.put("A", List.of("a0", "a1", "a2"));
		map.put("B", List.of(0, 1, 2));
		kl.setColMap(map);

		kl.remove("A");

		assertNull(kl.getColumn("A"));
		assertNotNull(kl.getColumn("B"));
		assertEquals(0, kl.getColMap().get("B").get(0));

	}
}
