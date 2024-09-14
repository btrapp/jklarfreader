package com.btrapp.jklarfreader.impl;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.objects.KlarfList;
import com.btrapp.jklarfreader.objects.KlarfRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Just a quick test to try reading a klarf and possibly output as json
 *
 * @author btrapp
 */
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

    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(klarfFile)); ) {
      // Optional<KlarfRecord> kro = KlarfReader.parseKlarf(new KlarfParser18Pojo(), fis);
      Optional<KlarfRecord> kro = KlarfReader.parseKlarf(bis);
      if (kro.isPresent()) {
        System.out.println("Klarf file read successfully");
        // KlarfReader.debugPrintKlarfRecord(kro.get(), 0);
        if (jsonFile != null) {
          // We want to write a json file
          ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
          writer.writeValue(jsonFile, kro.get());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void debugPrintKlarfRecord(KlarfRecord kr, int listCountToDisplay) {
    System.out.println("name id " + kr.getName() + " " + kr.getId());
    for (Entry<String, List<String>> e : kr.getFields().entrySet()) {
      System.out.println("  field " + e.getKey() + " " + e.getValue().toString());
    }
    for (KlarfList kl : kr.getLists()) {
      System.out.println("  list " + kl.getName() + " " + kl.size());
      for (int i = 0; i < kl.size(); i++) {
        if (i < listCountToDisplay || listCountToDisplay == -1) {
          // just show the first few rows
          System.out.print("    ");
          Map<String, Object> rowMap = kl.asRowMap(i);
          for (Entry<String, Object> e : rowMap.entrySet()) {
            System.out.print(e.getKey() + " " + e.getValue().toString() + "; ");
          }
          System.out.println();
        } else {
          break;
        }
      }
    }
    for (KlarfRecord skr : kr.getRecords()) {
      debugPrintKlarfRecord(skr, listCountToDisplay);
    }
  }
}
