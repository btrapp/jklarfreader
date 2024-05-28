package com.btrapp.jklarfreader;

import com.btrapp.jklarfreader.objects.KlarfReader18;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The static class used to kickoff a Klarf reading session
 *
 * <p>ex: Optional&lt;KlarfRecord&gt; klarf = KlarfReader.parseKlarf(new KlarfParser18Pojo(), is);
 *
 * @author btrapp
 */
public class KlarfReader {
  public enum KlarfFormat {
    UNSUPPORTED_FORMAT,
    V1_0,
    V1_2,
    V1_8
  }

  private KlarfReader() {}

  public static <T> Optional<T> parseKlarf(KlarfParserIf18<T> parser, InputStream is)
      throws Exception {
    return new KlarfReader18<T>(parser).readKlarf(is);
  }

  /**
   * Tries to determine if this looks *anything* like the expected Klarf format
   *
   * @param file The file to parse
   * @return the KlarfFormat enum
   * @throws IOException if any error
   */
  public static KlarfFormat findKlarfFormat(File file) throws IOException {
    // Record FileRecord "1.8"
    // FileVersion 1 2
    final Pattern pattern18 = Pattern.compile("Record\\s+FileRecord.*1\\.8.*");
    final Pattern pattern12 = Pattern.compile("FileVersion\\s+1\\s+2.*");
    final Pattern pattern10 = Pattern.compile("FileVersion\\s+1\\s+0.*");
    Matcher m;
    String line;
    int lineCount = 0;
    try (InputStream fis = Files.newInputStream(file.toPath())) {
      try (BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
        while ((line = br.readLine()) != null) {
          line = line.trim();
          m = pattern18.matcher(line);
          if (m.matches()) {
            return KlarfFormat.V1_8;
          }
          m = pattern12.matcher(line);
          if (m.matches()) {
            return KlarfFormat.UNSUPPORTED_FORMAT;
          }
          m = pattern10.matcher(line);
          if (m.matches()) {
            return KlarfFormat.UNSUPPORTED_FORMAT;
          }
          lineCount++;
          if (lineCount > 50) {
            // If we haven't found the record start in the first 50 lines, we probably
            // aren't going
            // to find it at all.
            return KlarfFormat.UNSUPPORTED_FORMAT;
          }
        }
      }
    }
    return KlarfFormat.UNSUPPORTED_FORMAT;
  }
}
