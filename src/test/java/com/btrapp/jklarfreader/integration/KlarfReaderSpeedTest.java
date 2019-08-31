package com.btrapp.jklarfreader.integration;

import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import com.btrapp.jklarfreader.objects.KlarfList;
import com.btrapp.jklarfreader.objects.KlarfRecord;

public class KlarfReaderSpeedTest {
	public static void main(String[] args) throws Exception {
		File testKlarfDir = new File(args[0]);
		int fileCount = 0;
		int defectCount = 0;
		
		Instant start = Instant.now();
		for (File f: testKlarfDir.listFiles()) {
			if (f.getName().startsWith("18")) {
				try (FileInputStream fis = new FileInputStream(f)) {
					Optional<KlarfRecord> klarf = KlarfReader.parseKlarf(new KlarfParser18Pojo(), fis);
					if (!klarf.isPresent()) {
						System.err.println("COuldn't read "+f.getAbsolutePath());
					} else {
						fileCount++;
						KlarfRecord klarfRecord = klarf.get();
						List<String> fileTimestamp = klarfRecord.findField("FileTimestamp").orElse(Collections.emptyList());
						for (KlarfRecord lotRecord: klarfRecord.findRecordsByName("LotRecord")) {
							for (KlarfRecord waferRecord: lotRecord.findRecordsByName("WaferRecord")) {
								KlarfList defects = waferRecord.findListByName("DefectList").orElse(new KlarfList());
								defectCount += defects.size();
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		long deltaMs = Instant.now().toEpochMilli() - start.toEpochMilli();
		System.out.println("Read "+fileCount+" klarfs with "+defectCount+" defects in "+deltaMs+" ms");
	}
}
