package com.iksgmbh.sysnat.exception;

import com.iksgmbh.sysnat.common.exception.SysNatException;

public class NaturalLanguageSyntaxException extends SysNatException 
{
	private static final long serialVersionUID = 1L;
	
	public NaturalLanguageSyntaxException(String message) {
		super(message);
	}

}
