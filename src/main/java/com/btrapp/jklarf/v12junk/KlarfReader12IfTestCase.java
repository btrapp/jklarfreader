package com.btrapp.jklarf.v12junk;

public class KlarfReader12IfTestCase {
//	@Test
//	public void testListRegexMatches() {
//		KlarfReader12 kr = new KlarfReader12();
//		String[] toTest = {
//				"SampleTestPlan 5",
//				"SampleTestPlan\t6",
//				"SampleTestPlan\t7",
//				"\tSampleTestPlan\t8\t",
//				" SampleTestPlan 9\r\n",
//				"SampleTestPlan 10"
//		};
//		for (int i = 0; i < toTest.length; i++) {
//			Matcher m = kr.matchesSimpleList(toTest[i]);
//			assertTrue(m.matches());
//			assertEquals("SampleTestPlan", m.group(1));
//			assertEquals((i + 5), Integer.parseInt(m.group(2)));
//			//Just make sure the other regex fails too
//			assertFalse(kr.matchesSimpleSpec(toTest[i]).matches());
//		}
//	}
//
//	@Test
//	public void testListRegexDoesntMatch() {
//		KlarfReader12 kr = new KlarfReader12();
//		assertFalse(kr.matchesSimpleList("Slot 21;").matches());
//		assertFalse(kr.matchesSimpleList("Slot\t21;").matches());
//		assertFalse(kr.matchesSimpleList("SummaryList").matches());
//		assertFalse(kr.matchesSimpleList("SummarySpec 4").matches());
//	}
//
//	@Test
//	public void testSpecRegexMatches() {
//		KlarfReader12 kr = new KlarfReader12();
//		String[] toTest = {
//				"SummarySpec 5",
//				"SummarySpec 5 COL1 COL2 COL3 COL4 COL5;",
//				"SUMMARYSPEC 5",
//				"SUMMARYSPEC 5 COL1 COL2 COL3 COL4 COL5;"
//		};
//		for (int i = 0; i < toTest.length; i++) {
//			Matcher m = kr.matchesSimpleSpec(toTest[i]);
//			assertTrue(m.matches());
//			assertEquals("SUMMARY", m.group(1).toUpperCase());
//			assertEquals(5, Integer.parseInt(m.group(2)));
//			//Just make sure the other regex fails too
//			assertFalse(kr.matchesSimpleList(toTest[i]).matches());
//
//		}
//	}
}
