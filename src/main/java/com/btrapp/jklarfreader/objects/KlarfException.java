package com.btrapp.jklarfreader.objects;

/**
 * An exception for when something's wrong with the Klarf format itself (like a list length is
 * wrong)
 *
 * @see com.btrapp.jklarfreader.objects.KlarfContentException for when the format is correct but the
 *     contents don't follow a business logic rule (like if you say ResultTimestamp is expected to
 *     be a String of 2 items, but only 1 is present)
 * @author btrapp
 */
public class KlarfException extends Exception {
  private static final long serialVersionUID = -6451200672730727155L;

  public enum ExceptionCode {
    GenericError,
    ListFormat,
    NumberFormat,
    UnsupportedKlarfVersion
  }

  private ExceptionCode code = ExceptionCode.GenericError;
  private int lineNumber = -1;

  public KlarfException(String msg) {
    this(msg, null, ExceptionCode.GenericError);
  }

  public KlarfException(String msg, KlarfTokenizer kt, ExceptionCode code) {
    super(
        msg
            + ((kt == null)
                ? ""
                : " (At line " + kt.getLineNumber() + ": " + kt.getCurrentLine() + ")"));
    this.code = code;
    this.lineNumber = kt.getLineNumber();
  }

  public ExceptionCode getCode() {
    return code;
  }

  public int getLineNumber() {
    return lineNumber;
  }
}
