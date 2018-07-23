package com.iksgmbh.sysnat.utils;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SysNatUtilTest 
{

	@Test
	public void removesLinesToBeIgnoreByPrefix() throws IOException 
	{
		// arrange
		final List<String> lines = new ArrayList<>();
		lines.add("aXXX");
		lines.add("bXXX");
		lines.add("abXXX");
		lines.add("abcXXX");
		
		final List<String> prefixesToIgnore = new ArrayList<>();
		prefixesToIgnore.add("ab");
		prefixesToIgnore.add("aX");

		// act
		final List<String> result = SysNatUtil.removeLinesToBeIgnoreByPrefix(lines, prefixesToIgnore);
		
		// assert
		assertFalse("Lines contain unexpected entries", result.contains("abXXX"));
		assertFalse("Lines contain unexpected entries", result.contains("abcXXX"));
	}

}
