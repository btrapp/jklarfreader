package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class KlarfReader18IfFileExamplesTestCase {
	private ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();

	private Optional<KlarfRecord> readTestKlarf() throws Exception {
		KlarfParser18Pojo kp18 = new KlarfParser18Pojo();
		new KlarfReader().parseKlarf(kp18, this.getClass().getResourceAsStream("simple18.klarf"));
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
		assertEquals("12-12-1972,16:27:59", toStr(klarfRecord.getFields().getOrDefault("FileTimestamp", Collections.emptyList())));

	}

	@Test
	public void testLot() throws Exception {
		Optional<KlarfRecord> klarfRecordO = readTestKlarf();
		assertTrue(klarfRecordO.isPresent());
		KlarfRecord klarfRecord = klarfRecordO.get();

		Optional<KlarfRecord> lotRecordO = klarfRecord.findRecordByNameAndId("LotRecord", "aLot.String");
		assertTrue(lotRecordO.isPresent());
		//System.out.println(writer.writeValueAsString(lotRecordO.get()));
		KlarfRecord lotRecord = lotRecordO.get();
		assertEquals(1, lotRecord.getLists().size());
		assertEquals(11, lotRecord.getFields().size());
		assertEquals(2, lotRecord.getRecords().size()); //Two wafers in here

		//
		// Expected fields and values
		//
		Map<String, String> expectedFields = new HashMap<String, String>() {
			{
				put("DeviceID", "aDevice");
				put("DiePitch", "5744150,5410203");
				put("InspectionStationID", "NONE,A,B");
				put("OrientationMarkLocation", "0");
				put("RecipeID", "ARecipe,12-09-1971,02:08:00");
				put("RecipeVersion", ",NONE,");
				put("ResultTimestamp", "12-11-1971,12:32:22");
				put("SampleOrientationMarkType", "NOTCH");
				put("SampleSize", "300000000,0");
				put("SampleType", "WAFER");
				put("StepID", "AStepId");
			}
		};
		for (Map.Entry<String, String> e : expectedFields.entrySet()) {
			String fieldName = e.getKey();
			String fieldValue = e.getValue();
			assertEquals(fieldValue, toStr(lotRecord.getFields().get(fieldName)), "Field " + fieldName + " matches expectation");
		}
		//NO surprise fields
		assertEquals(expectedFields.size(), lotRecord.getFields().size(), "Only expected fields are found");

		//Check lists
		List<KlarfList> classLookups = lotRecord.findListsByName("ClassLookupList");
		assertEquals(1, classLookups.size());
		KlarfList classLookup = classLookups.get(0);
		//Columns are right
		assertEquals("CLASSNUMBER,CLASSNAME,CLASSCODE", toStr(classLookup.getColumnNames()));
		assertEquals("int32,string,string", toStr(classLookup.getColumnTypes()));
		assertEquals(4, classLookup.getData().size());
		//Check list contents
		assertEquals("0,Unclassified,", toStr(classLookup.getData().get(0)));
		assertEquals("1,NV,", toStr(classLookup.getData().get(1)));
		assertEquals("2,FM,", toStr(classLookup.getData().get(2)));
		assertEquals("3,Blobby,", toStr(classLookup.getData().get(3)));
	}

	//	@Test
	//	public void testWafer() {
	//		Map<String,String> expectedFields = new HashMap<String,String>() {{
	//			put("DieOrigin","0,0");
	//			put("OrientationInstructions","1");
	//			put("ProcessEquipmentState","NONE,,,,,");
	//			put("SampleCenterLocation","4929000,-490333");
	//			put("SlotNumber","25");
	//		}};
	//		for (Map.Entry<String,String> e: expectedFields) {
	//			
	//		}
	//	}
}
