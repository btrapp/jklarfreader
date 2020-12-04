package com.btrapp.jklarfreader.impl;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.objects.KlarfRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

public class KlarfQuickTest {
  public static void main(String[] args) {
    if (args == null || (args.length < 1) || (args.length > 2)) {
      System.err.println(
          "Usage: java -jar jklarfreader-#.#.#.jar /path/to/klarf  (optional: /path/to/json)");
      System.exit(1);
    }
    File klarfFile = new File(args[0]);
    if (!klarfFile.canRead()) {
      System.err.println("Can't read " + klarfFile.getAbsolutePath());
      System.exit(1);
    }
    File jsonFile = null;
    if (args.length == 2) {
      jsonFile = new File(args[1]);
      if (jsonFile.canRead()) {
        System.err.println(
            "Error - json output file " + jsonFile.getAbsolutePath() + " already exists, exiting.");
        System.exit(1);
      }
    }

    try (FileInputStream fis = new FileInputStream(klarfFile); ) {
      Optional<KlarfRecord> kro = KlarfReader.parseKlarf(new KlarfParser18Pojo(), fis);
      if (kro.isPresent()) {
        System.out.println("Klarf file read successfully");
        if (jsonFile != null) {
          // We want to write a json file
          ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
          writer.writeValue(jsonFile, kro.get());
        }
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
