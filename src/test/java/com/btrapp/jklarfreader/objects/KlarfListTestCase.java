package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

class KlarfListTestCase {
	@Test
	void testCanSetPropertiesInAnyOrder() {
		// An old version of jklarfreader would throw an exception if you set colMap
		// *before* colNames.
		Consumer<KlarfList> setMap = (KlarfList kl) -> kl.setColMap(Map.of("A", List.of(1)));
		Consumer<KlarfList> setNames = (KlarfList kl) -> kl.setColumnNames(List.of("A"));
		Consumer<KlarfList> setTypes = (KlarfList kl) -> kl.setColumnTypes(List.of("integer"));
		Consumer<KlarfList> setName = (KlarfList kl) -> kl.setName("test");
		List<Consumer<KlarfList>> methods = List.of(setMap, setNames, setTypes, setName);
		for (int i = 0; i < 20; i++) {
			KlarfList kl = new KlarfList();
			try {
				List<Consumer<KlarfList>> shuffled = new ArrayList<>(methods);
				Collections.shuffle(shuffled);
				for (Consumer<KlarfList> c : shuffled) {
					c.accept(kl);
				}
			} catch (Exception ex) {
				assertTrue(false, "Failed in ListOrder");
			}

		}
	}

	@Test
	void testAddingNewAttr() {
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
	void testReplacingAttrs() {
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
	void testRemovingAttrs() {
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

	@Test
	void testListyConstructor() {
		List<Object> integers = List.of(1, 2, 3);
		List<Object> strings = List.of("A", "B", "C");
		KlarfList kl = new KlarfList();
		kl.setName("MyName");
		kl.set("ColA", "int32", integers);
		kl.set("ColB", "string", strings);
		assertEquals(2, kl.getColMap().size());
		assertEquals(2, kl.getColumnTypes().size());
	}
}
