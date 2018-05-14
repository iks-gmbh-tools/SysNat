package com.iksgmbh.sysnat.helper;

import java.util.List;

public class TestUtil 
{
	public static boolean doesMessagesContain(List<String> reportMessages, String toFind) 
	{
		for (String message : reportMessages) {
			if (message.contains(toFind) || message.equals(toFind)) {
				return true;
			}
		}
		return false;
	}
}
