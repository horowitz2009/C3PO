package com.horowitz.seaport;

public class GameErrorException extends Exception {
	private int code;

	public int getCode() {
		return code;
	}

	public GameErrorException(int code) {
		this(code, null, null);
	}

	public GameErrorException(int code, String msg, Exception cause) {
		super(msg, cause);
		this.code = code;

	}

	public GameErrorException(String msg) {
		this(0, msg, null);
	}

}
