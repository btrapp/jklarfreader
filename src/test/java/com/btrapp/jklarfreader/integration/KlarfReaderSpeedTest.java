package com.btrapp.jklarfreader.integration;

import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
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
					KlarfParser18Pojo parser = new KlarfParser18Pojo();
					Optional<KlarfRecord> klarf = KlarfReader.parseKlarf(parser, fis);
					if (!klarf.isPresent()) {
						System.err.println("COuldn't read "+f.getAbsolutePath());
					} else {
						fileCount++;
						defectCount += klarf.get()
							.findRecordsByName("LotRecord")
							.flatMap(lr->lr.findRecordsByName("WaferRecord"))
							.flatMap(wr->wr.findListByName("DefectList"))
							.mapToInt(KlarfList::size)
							.sum();
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
