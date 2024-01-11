package com.btrapp.jklarfreader.objects;

import com.btrapp.jklarfreader.objects.KlarfException.ExceptionCode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class KlarfTokenizer implements AutoCloseable {
  private int lineNumber = 1;
  private BufferedReader br;
  private String token = null;
  private Boolean quotedFlag = null;
  private String currentLine = "";
  private List<String> tokens = new ArrayList<>();
  private List<Boolean> quotedFlags = new ArrayList<>();

  KlarfTokenizer(InputStream is) {
    this.br = new BufferedReader(new InputStreamReader(is));
  }

  protected String getCurrentLine() {
    return currentLine;
  }

  protected boolean nextToken() throws IOException {
    if (!tokens.isEmpty()) {
      token = tokens.remove(0);
      quotedFlag = quotedFlags.remove(0);
      return true;
    }
    if (br != null) {
      boolean found = false;
      while (!found) { // Read until we have a non-blank line
        currentLine = br.readLine();
        lineNumber++;
        if (currentLine == null) found = true;
        else {
          currentLine = currentLine.trim();
          if (!currentLine.isEmpty()) {
            found = true;
          }
        }
      }
      if (currentLine == null) {
        // End of file reached
        br.close();
        br = null;
        return false;
      }

      // Tokenize on commas, whitespace, and {}s, keeping the separator tokens (" and
      // ;)
      StringTokenizer st = new StringTokenizer(currentLine, ", \t{}\";", true);
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if (token.equals("\"")) {
          StringBuilder quotedString = new StringBuilder();
          boolean endQuoteNotFound = true;
          while (st.hasMoreTokens() && endQuoteNotFound) {
            token = st.nextToken();
            if (token.equals("\"")) {
              endQuoteNotFound = false;
            } else {
              quotedString.append(token);
            }
          }
          tokens.add(quotedString.toString());
          quotedFlags.add(Boolean.TRUE);
        } else {
          // Not a quoted string. Trim it and remove blanks
          token = token.trim();
          if (!token.isBlank()) {
            tokens.add(token);
            quotedFlags.add(Boolean.FALSE);
          }
        }
      }
    }
    if (!tokens.isEmpty()) {
      token = tokens.remove(0);
      quotedFlag = quotedFlags.remove(0);
      return true;
    }
    return false;
  }

  /**
   * The next token
   *
   * @return
   */
  public String val() {
    return token;
  }

  /**
   * T/F if this field is quoted or not
   *
   * @return
   */
  public boolean isQuoted() {
    return quotedFlag.booleanValue();
  }

  /**
   * Skips ahead to your requested string, **case insensitively**
   *
   * @param str the string to parse
   * @throws IOException if the buffer fails
   * @throws KlarfException if the format doesn't match the klarf standard
   */
  public void skipTo(String str) throws IOException, KlarfException {
    while (!str.equalsIgnoreCase(val())) {
      // We've already read the 1st {
      if (!nextToken()) {
        throw new KlarfException(str + " not found", this, ExceptionCode.GenericError);
      }
    }
  }

  public int getLineNumber() {
    return lineNumber;
  }

  @Override
  public void close() throws Exception {
    if (br != null) {
      br.close();
    }
  }

  public String nextVal() throws IOException {
    nextToken();
    return val();
  }

  public Integer nextIntVal() throws IOException, KlarfException {
    nextToken();
    return intVal();
  }

  private boolean isNa(String s) {
    return (s.equalsIgnoreCase("NA"));
  }

  public Integer intVal() throws KlarfException {
    String str = val();
    if (isNa(str)) {
      return null;
    }
    try {
      return Integer.valueOf(str);
    } catch (NumberFormatException nfe) {
      throw new KlarfException(
          "Value '" + str + "' was expected to be an integer but was not.",
          this,
          ExceptionCode.NumberFormat);
    }
  }

  public Float floatVal() throws KlarfException {
    String str = val();
    if (isNa(str)) return null;
    try {
      return Float.valueOf(str);
    } catch (NumberFormatException nfe) {
      throw new KlarfException(
          "Value '" + str + "' was expected to be an float but was not.",
          this,
          ExceptionCode.NumberFormat);
    }
  }
}
