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
package com.iksgmbh.sysnat.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.common.helper.FileFinder;
import com.iksgmbh.sysnat.domain.JavaFieldData;

/**
 * Searches for LanguageTemplatesContainers in jUnitTestCaseTemplate
 * and returns the information about the instance fields of those containers.
 */
public class LanguageTemplateContainerFinder 
{	

	public static List<JavaFieldData> findLanguageTemplateContainers(final String testApplicationName) 
	{
		final List<JavaFieldData> toReturn = new ArrayList<>();
		final HashMap<String, Class<?>> containerCandidates = findCandidates(testApplicationName);
		containerCandidates.forEach((fieldName, type) -> isLanguageTemplatesContainer(fieldName, type, toReturn));
		
		toReturn.sort(new Comparator<JavaFieldData>() {
			   // sort in any deterministic order to have stable results for automated tests
			   @Override public int compare(JavaFieldData o1, JavaFieldData o2) {
			        return o1.name.compareTo(o2.name);
			    }
		});
		
		return toReturn;
	}

	private static HashMap<String, Class<?>> findCandidates(final String testApplicationName) 
	{
		final HashMap<String, Class<?>> toReturn = new HashMap<>();
		final String applicationSpecificDirecory = System.getProperty("sysnat.languageTemplateContainer.source.dir") + "/" + testApplicationName.toLowerCase();
		if (applicationSpecificDirecory.startsWith("null")) {
			RuntimeException e = new RuntimeException("Property sysnat.languageTemplateContainer.source.dir not set!");
			e.printStackTrace();
			throw e;
		}
		final List<File> result = FileFinder.searchFilesRecursively(applicationSpecificDirecory, ".java");

		final String commonDirectory = System.getProperty("sysnat.languageTemplateContainer.source.dir") + "/common";
		final List<File> result2 = FileFinder.searchFilesRecursively(commonDirectory, ".java");
		result.addAll(result2);
		
		result.forEach(file -> addClassIfNoPageObject(toReturn, file));
		
		if (result.size() == 0) {
			System.err.println("Warning: No LanguageTemplateContainer found!");
		}
		
		return toReturn;
	}

	private static void addClassIfNoPageObject(HashMap<String, Class<?>> toReturn, File file) 
	{
		if (file.getAbsolutePath().toLowerCase().contains("pageobject")) {
			return;
		}
		final String className = buildTestClassName(file);
		final String fieldName = buildFieldName(file);
		try {
			final Class<?> loadedClass = Class.forName(className);
			toReturn.put(fieldName, loadedClass);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static String buildFieldName(final File file) 
	{
		String fieldName = file.getName();
		int pos = fieldName.lastIndexOf(".");
		fieldName = fieldName.substring(0, pos);
		
		String firstChar = fieldName.substring(0, 1);
		return firstChar.toLowerCase() + fieldName.substring(1);
	}

	private static String buildTestClassName(File file) {
		String className = file.getAbsolutePath();
		int pos = className.indexOf("com");
		className = className.substring(pos);
		pos = className.lastIndexOf(".");
		className = className.substring(0, pos);
		className = className.replaceAll("\\\\", "\\.");
		return className;
	}

	private static void isLanguageTemplatesContainer(final String fieldName, 
			                                         final Class<?> type,
			                                         final List<JavaFieldData> result) 
	{
		if (type.isAnnotationPresent(LanguageTemplateContainer.class)) {
			result.add(new JavaFieldData(fieldName, type));
		}
	}

}