package com.btrapp.jklarfreader.impl;

import java.io.File;
import java.io.IOException;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.KlarfReader.KlarfFormat;

public class Klarf12QuickTest {

	public static void main(String[] args) {
		File klarfFile = new File(args[0]);
		try {
			KlarfFormat kf = KlarfReader.findKlarfFormat(klarfFile);
			System.out.println("klarf format " + kf.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//		try (FileInputStream fis = new FileInputStream(klarfFile);) {
		//			Optional<KlarfRecord> klarf = KlarfReader.parseKlarf12(new KlarfParser18Pojo(), fis);
		//			if (klarf.isPresent()) {
		//				System.out.println("read success");
		//				KlarfRecord klarfRecord = klarf.get();
		//				for (KlarfRecord lotRecord : klarfRecord.findRecordsByName("LotRecord")) {
		//					System.out.println("lotrecord id " + lotRecord.getId());
		//					for (KlarfRecord waferRecord : lotRecord.findRecordsByName("WaferRecord")) {
		//						System.out.println("waferrecord id " + waferRecord.getId());
		//						int defectCount = 0;
		//						for (KlarfList defectList : waferRecord.findListsByName("DefectList")) {
		//							defectCount += defectList.size();
		//						}
		//						System.out.println("defectCount " + defectCount);
		//					}
		//				}
		//			}
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}

	}

}
