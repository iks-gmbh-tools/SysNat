/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		final List<String> result = SysNatTestRuntimeUtil.removeLinesToBeIgnoreByPrefix(lines, prefixesToIgnore);
		
		// assert
		assertFalse("Lines contain unexpected entries", result.contains("abXXX"));
		assertFalse("Lines contain unexpected entries", result.contains("abcXXX"));
	}

}