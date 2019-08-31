package com.btrapp.jklarfreader.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.time.Instant;
import java.util.Optional;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KlarfReaderSpeedTest {
	public static void main(String[] args) throws Exception {
		File dir = new File("/home/btrapp/Dropbox/3600-13ED/examplesDoNotUpload/");

		long startMs = Instant.now().toEpochMilli();
		int klarfCount = 0;
		for (File klarfF : dir.listFiles()) {
			if (!klarfF.getName().startsWith("18"))
				continue;
			System.out.println(klarfF.getAbsolutePath());
			KlarfParser18Pojo parser = new KlarfParser18Pojo();
			Optional<KlarfRecord> klarf = KlarfReader.parseKlarf(parser, new FileInputStream(klarfF));
			try (FileWriter fw = new FileWriter(new File("/tmp/" + klarfF.getName() + ".json"))) {
				new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(fw, klarf.get());
				klarfCount++;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		long endMs = Instant.now().toEpochMilli();
		System.out.println("Reading " + klarfCount + " took " + (endMs - startMs) + " ms");
	}
}
