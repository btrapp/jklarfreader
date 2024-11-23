package com.btrapp.jklarfreader.util;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.KlarfReader.KlarfFormat;
import com.btrapp.jklarfreader.objects.KlarfRecord;
import com.btrapp.jklarfreader.objects.KlarfWriter18;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

/** read a 1.2 KLARF and write a 1.8 KLARF */
public class Klarf12To18Converter {
  private static String usage =
      "Arguments expected: -klarfIn /path/to/read/1.2_klarf -klarfOut /path/to/write/1.8_klarf";
  private static String klarfIn = "";
  private static String klarfOut = "";

  public static void main(String[] args) {
    if (args.length == 0 | args.length != 4) {
      System.err.println(usage);
      System.exit(1);
    }
    for (int i = 0; i < args.length; i += 2) {
      String argKey = args[i];
      String argValue = args[i + 1];
      if ("-klarfIn".equals(argKey)) {
        klarfIn = argValue;
      }
      if ("-klarfOut".equals(argKey)) {
        klarfOut = argValue;
      }
    }
    if (klarfIn.isBlank() || klarfOut.isBlank()) {
      System.err.println(usage);
      System.exit(1);
    }
    File klarfFileIn = new File(klarfIn);
    if (!klarfFileIn.exists()) {
      System.err.println(klarfIn + " does not exist");
      System.exit(1);
    }
    File klarfFileOut = new File(klarfOut);
    if (klarfFileOut.exists()) {
      System.err.println(klarfOut + " alread exists.  Delete it first.");
      System.exit(1);
    }
    try (FileInputStream fis = new FileInputStream(klarfFileIn);
        BufferedInputStream bis = new BufferedInputStream(fis)) {
      KlarfRecord klarfRecord = KlarfReader.parseKlarf(bis, KlarfFormat.V1_2).orElse(null);
      if (klarfRecord == null) {
        System.err.println("Can read klarf");
        System.exit(1);
      }
      try (FileWriter fw = new FileWriter(klarfFileOut);
          BufferedWriter bw = new BufferedWriter(fw)) {
        KlarfWriter18 kw = new KlarfWriter18();
        kw.writeKlarf(klarfRecord, bw);
        System.out.println("Wrote 1.8 KLARF to " + klarfOut);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
