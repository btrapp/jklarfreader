package com.btrapp.jklarfreader.objects;

public class KlarfException extends Exception {
	private static final long serialVersionUID = -6451200672730727155L;

	public enum ExceptionCode {
		GenericError, ListFormat, NumberFormat, UnsupportedKlarfVersion
	}

	private ExceptionCode code = ExceptionCode.GenericError;

	public KlarfException(String msg) {
		this(msg, null, ExceptionCode.GenericError);
	}

	public KlarfException(String msg, KlarfTokenizer kt, ExceptionCode code) {
		super(msg + ((kt == null) ? "" : " (At line " + kt.getLineNumber() + ": " + kt.getCurrentLine() + ")"));
		this.code = code;
	}

	public ExceptionCode getCode() {
		return code;
	}
}
