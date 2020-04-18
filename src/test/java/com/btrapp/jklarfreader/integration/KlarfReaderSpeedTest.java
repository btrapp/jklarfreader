package com.btrapp.jklarfreader.integration;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.KlarfReader.KlarfFormat;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import com.btrapp.jklarfreader.objects.KlarfException;
import com.btrapp.jklarfreader.objects.KlarfList;
import com.btrapp.jklarfreader.objects.KlarfRecord;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Optional;

/**
 * Just used to see how fast we can run a bunch of klarfs through here
 *
 * @author btrapp
 */
public class KlarfReaderSpeedTest {
  public static void main(String[] args) throws Exception {
    if (args[0] == null) {
      System.err.println("Please pass a path to a directory of Klarf files");
      System.exit(1);
    }
    File testKlarfDir = new File(args[0]);
    int fileCount = 0;
    int defectCount = 0;

    Instant start = Instant.now();
    for (File f : testKlarfDir.listFiles()) {
      try (InputStream is = Files.newInputStream(f.toPath())) {
        Optional<KlarfRecord> klarf = KlarfReader.parseKlarf(new KlarfParser18Pojo(), is);
        if (!klarf.isPresent()) {
          // Is this a 1.8 klarf?
          System.err.println("COuldn't read " + f.getAbsolutePath());
        } else {
          fileCount++;
          KlarfRecord klarfRecord = klarf.get();
          // List<String> fileTimestamp =
          // klarfRecord.findField("FileTimestamp").orElse(Collections.emptyList());
          for (KlarfRecord lotRecord : klarfRecord.findRecordsByName("LotRecord")) {
            for (KlarfRecord waferRecord : lotRecord.findRecordsByName("WaferRecord")) {
              for (KlarfList defectList : waferRecord.findListsByName("DefectList")) {
                defectCount += defectList.size();
              }
            }
          }
        }
      } catch (KlarfException ke) {
        if (KlarfReader.findKlarfFormat(f) == KlarfFormat.UNSUPPORTED_FORMAT) {
          System.out.println(
              "File " + f.getAbsolutePath() + " doesn't appear to be the right format");
        } else {
          System.err.println("COuldn't read " + f.getAbsolutePath());
        }
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    long deltaMs = Instant.now().toEpochMilli() - start.toEpochMilli();
    System.out.println(
        "Read " + fileCount + " klarfs with " + defectCount + " defects in " + deltaMs + " ms");
  }
}
