package com.btrapp.jklarfreader.integration;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import com.btrapp.jklarfreader.objects.KlarfRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

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
}
