package com.btrapp.jklarfreader.integration;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import com.btrapp.jklarfreader.objects.KlarfParserTest;
import com.btrapp.jklarfreader.objects.KlarfRecord;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * A simple CLI to convert a klarf directly to JSON
 *
 * @author btrapp
 */
public class KlarfToJson {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: KlarfToJson.java /path/to/input.klarf /path/to/output.json");
			System.exit(1);
		}
		File inputKlarf = new File(args[0]);
		if (!inputKlarf.canRead()) {
			System.err.println("Can't read the input Klarf " + inputKlarf.getAbsolutePath());
		}
		File outputJson = new File(args[1]);
		try (FileInputStream fis = new FileInputStream(inputKlarf)) {
			Optional<KlarfRecord> klarf = KlarfReader.parseKlarf(new KlarfParser18Pojo(), fis);
			if (klarf.isPresent()) {
				ObjectWriter jsonWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
				jsonWriter.writeValue(outputJson, klarf.get());
			} else {
				System.err.println("Error reading " + inputKlarf.getAbsolutePath());
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Wrote " + outputJson.getAbsolutePath());
		System.exit(0);
	}

	@Test
	void testJsonKlarfSerDeser() {
		try {
			JsonMapper mapper = JsonMapper.builder().build();
			KlarfRecord k1 = KlarfReader.parseKlarf(new KlarfParser18Pojo(),
					KlarfParserTest.class.getResourceAsStream("/com/btrapp/jklarfreader/objects/simple18.klarf"))
					.orElseThrow();
			assertNotNull(k1, "Read klarf");
			String json = mapper.writeValueAsString(k1);
			assertFalse(json.isEmpty());
			KlarfRecord k2 = mapper.readValue(json, KlarfRecord.class);
			assertNotNull(k2);
			assertEquals(k1.getLists().size(),k2.getLists().size(),"list lens match");
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

	}
}
