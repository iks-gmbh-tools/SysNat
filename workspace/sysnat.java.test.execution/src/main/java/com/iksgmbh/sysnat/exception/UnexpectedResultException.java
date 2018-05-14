package com.iksgmbh.sysnat.exception;

public class UnexpectedResultException extends SysNatException 
{
	private static final long serialVersionUID = 1L;
	
	public UnexpectedResultException(String message) {
		super(message);
	}

	public UnexpectedResultException() {
		super("");
	}


}
