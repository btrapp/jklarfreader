package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class KlarfRecordTestCase {
	private KlarfRecord dummyRec() {
		KlarfRecord kr = new KlarfRecord("test", "1");
		KlarfList kl = new KlarfList();
		kl.setName("List1");
		kl.set("NAME", "string", List.of("A", "B"));
		kl.set("VALUE", "string", List.of("vA", "vB"));
		kr.addList(kl);
		return kr;
	}

	@Test
	/**
	 * This should just add the list to the klarf
	 */
	public void testListAddNewName() {
		KlarfRecord kr = dummyRec();
		assertEquals(1, kr.getLists().size());
		assertEquals(1, kr.findListsByName("List1").size());
		KlarfList newList = new KlarfList();
		newList.setName("List2");
		newList.set("NAME", "string", List.of("A2", "B2"));
		newList.set("VALUE", "string", List.of("vA2", "vB2"));
		kr.setListByName(newList);
		assertEquals(2, kr.getLists().size());
	}

	@Test
	/**
	 * This should replace the existing list with the same name.
	 */
	public void testListAddSameName() {
		KlarfRecord kr = dummyRec();
		assertEquals(1, kr.getLists().size());
		assertEquals(1, kr.findListsByName("List1").size());
		KlarfList newList = new KlarfList();
		newList.setName("List1");
		newList.set("NAME", "string", List.of("A2", "B2"));
		newList.set("VALUE", "string", List.of("vA2", "vB2"));
		kr.setListByName(newList);
		assertEquals(1, kr.getLists().size());
		assertEquals(1, kr.findListsByName("List1").size());
		KlarfList kl = kr.findListsByName("List1").get(0);
		assertEquals("A2", kl.getColMap().get("NAME").get(0));
		assertEquals("B2", kl.getColMap().get("NAME").get(1));
		assertEquals("vA2", kl.getColMap().get("VALUE").get(0));
		assertEquals("vB2", kl.getColMap().get("VALUE").get(1));
	}
}