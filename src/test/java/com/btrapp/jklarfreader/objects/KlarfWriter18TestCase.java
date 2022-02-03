package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

public class KlarfWriter18TestCase {
  @Test
  public void testKlarfRecordIdWriting() throws IOException {

    KlarfRecord krWithId = new KlarfRecord("MyName", "MyId");

    KlarfWriter18 kw = new KlarfWriter18();
    StringWriter sw = new StringWriter();
    kw.writeKlarf(krWithId, sw);
    assertTrue(sw.toString().contains("Record MyName \"MyId\""));
  }

  @Test
  public void testKlarfRecordNoIdWriting() throws IOException {

    KlarfRecord krWithId = new KlarfRecord("MyName", "");

    KlarfWriter18 kw = new KlarfWriter18();
    StringWriter sw = new StringWriter();
    kw.writeKlarf(krWithId, sw);
    // System.out.println(sw.toString());
    assertTrue(
        sw.toString()
            .contains(
                "Record MyName {")); // Be sure we're not writing a blank id of Record MyName "" {
  }
}
