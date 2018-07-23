package com.iksgmbh.sysnat.helper;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.exception.UnsupportedGuiEventException;

public class ExceptionHandler 
{

	public static SysNatException throwSysNatException(String errorMessage) {
		System.err.println(errorMessage);
		throw new SysNatException(errorMessage);
	}

	
	public static void doYourJob(String errorMessage, Exception e) {
		e.printStackTrace();
		throwSysNatException(errorMessage);
	}


	public static void doYourJob(Exception e) {
		e.printStackTrace();
		throwSysNatException("Unexpected Problem: " + e.getMessage());
	}

	public static SysNatException createNewUnsupportedGuiEventException(String errorMessage) {
		System.err.println(errorMessage);
		return new UnsupportedGuiEventException(errorMessage);
	}
	
}
