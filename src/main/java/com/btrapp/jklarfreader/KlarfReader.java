package com.btrapp.jklarfreader;

import com.btrapp.jklarfreader.impl.KlarfParser12Pojo;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import com.btrapp.jklarfreader.objects.KlarfException;
import com.btrapp.jklarfreader.objects.KlarfException.ExceptionCode;
import com.btrapp.jklarfreader.objects.KlarfReader12;
import com.btrapp.jklarfreader.objects.KlarfReader18;
import com.btrapp.jklarfreader.objects.KlarfRecord;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
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
   * Determine appropriate version and parse klarf. Pass allowedFormats to only parse certain
   * versions or leave empty to accept all supported versions.
   *
   * @param is
   * @param allowedFormats
   * @return
   * @throws Exception
   */
  public static Optional<KlarfRecord> parseKlarf(
      BufferedInputStream is, KlarfFormat... allowedFormats) throws Exception {
    // TODO maybe move KlarfReader12 and 18 into the KlarfParserPojo
    KlarfFormat kf = findKlarfFormat(is);
    if (allowedFormats != null && allowedFormats.length > 0) {
      if (!Arrays.asList(allowedFormats).contains(kf)) {
        throw new KlarfException(
            "Unsupported parsing of " + kf.toString(), null, ExceptionCode.UnsupportedKlarfVersion);
      }
    }
    // is.getChannel().position(0);//done in findKlarfFormat instead - this only works with
    // FileInputStream
    switch (kf) {
      case V1_2:
        return new KlarfReader12<>(new KlarfParser12Pojo()).readKlarf(is);
      case V1_8:
        return new KlarfReader18<>(new KlarfParser18Pojo()).readKlarf(is);
      default:
        throw new Exception("Unsupported parsing of " + kf.toString());
    }
  }

  /**
   * Tries to determine if this looks *anything* like the expected Klarf format
   *
   * @param file The file to parse
   * @return the KlarfFormat enum
   * @throws IOException if any error
   */
  public static KlarfFormat findKlarfFormat(File file) throws IOException {
    try (BufferedInputStream fis = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
      return findKlarfFormat(fis);
    }
  }

  /**
   * this method does not close the inputstream
   *
   * @param is
   * @return
   * @throws IOException
   */
  public static KlarfFormat findKlarfFormat(BufferedInputStream is) throws IOException {
    // Record FileRecord "1.8"
    // FileVersion 1 2
    is.mark(1000);
    final Pattern pattern18 = Pattern.compile("Record\\s+FileRecord.*1\\.8.*");
    final Pattern pattern12 = Pattern.compile("FileVersion\\s+1\\s+[21].*"); // matches 1 1 or 1 2
    final Pattern pattern10 = Pattern.compile("FileVersion\\s+1\\s+0.*");
    Matcher m;
    String line;
    int lineCount = 0;
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    while ((line = br.readLine()) != null) {
      line = line.trim();
      m = pattern18.matcher(line);
      if (m.matches()) {
        is.reset();
        return KlarfFormat.V1_8;
      }
      m = pattern12.matcher(line);
      if (m.matches()) {
        is.reset();
        return KlarfFormat.V1_2;
      }
      m = pattern10.matcher(line);
      if (m.matches()) {
        is.reset();
        return KlarfFormat.UNSUPPORTED_FORMAT;
      }
      lineCount++;
      if (lineCount > 50) {
        // If we haven't found the record start in the first 50 lines, we probably
        // aren't going
        // to find it at all.
        is.reset();
        return KlarfFormat.UNSUPPORTED_FORMAT;
      }
    }
    is.reset();
    return KlarfFormat.UNSUPPORTED_FORMAT;
  }
}
