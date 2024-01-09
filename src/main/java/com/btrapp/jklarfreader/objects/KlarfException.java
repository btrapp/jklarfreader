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

  /**
   * Type of Klarf error
   *
   * @author btrapp
   */
  public enum ExceptionCode {
    GenericError,
    ListFormat,
    NumberFormat,
    UnsupportedKlarfVersion
  }

  private final ExceptionCode code;
  private final int lineNumber;

  /**
   * Klarf formatting error
   *
   * @param msg the message to tell people about
   */
  public KlarfException(String msg) {
    this(msg, null, ExceptionCode.GenericError);
  }

  /**
   * Error including the token/code info
   *
   * @param msg the message
   * @param kt the tokenizer
   * @param code the type of code
   */
  public KlarfException(String msg, KlarfTokenizer kt, ExceptionCode code) {
    super(
        msg
            + ((kt == null)
                ? ""
                : " (At line " + kt.getLineNumber() + ": " + kt.getCurrentLine() + ")"));
    this.code = code;
    this.lineNumber = (kt == null) ? -1 : kt.getLineNumber();
  }

  /**
   * @return the code
   */
  public ExceptionCode getCode() {
    return code;
  }

  /**
   * @return the line of the problem
   */
  public int getLineNumber() {
    return lineNumber;
  }
}
