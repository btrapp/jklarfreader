package com.btrapp.jklarfreader.objects;

/**
 * An exception for when the content of the Klarf doesn't match the pattern your business logic
 * expects (ex: getField(x) is an array of 3 but you only allow an array of 2) Mostly thrown by the
 * reqField(...) methods
 *
 * @author btrapp
 */
public class KlarfContentException extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * Throws an exception with the given message
   *
   * @param msg the message to display
   */
  public KlarfContentException(String msg) {
    super(msg);
  }
}
