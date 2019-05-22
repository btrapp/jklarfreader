package com.btrapp.jklarfreader.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.time.Instant;
import java.util.Optional;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Read18 {
	public static void main(String[] args) throws Exception {
		KlarfReader kr = new KlarfReader();
		File dir = new File("/home/btrapp/Dropbox/3600-13ED/examplesDoNotUpload/");

		long startMs = Instant.now().toEpochMilli();
		for (File klarfF : dir.listFiles()) {
			System.out.println(klarfF.getAbsolutePath());
			if (!klarfF.getName().startsWith("18"))
				continue;
			KlarfParser18Pojo parser = new KlarfParser18Pojo();
			Optional<KlarfRecord> klarf = kr.parseKlarf(parser, new FileInputStream(klarfF));
			FileWriter fw = new FileWriter(new File("/tmp/" + klarfF.getName() + ".json"));
			new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(fw, klarf.get());
		}
		long endMs = Instant.now().toEpochMilli();
		System.out.println("Reading " + dir.listFiles().length + " took " + (endMs - startMs) + " ms");
	}
}
