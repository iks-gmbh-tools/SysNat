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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplates;
import com.iksgmbh.sysnat.common.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.common.utils.ExceptionHandlingUtil;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaFieldData;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;

/**
 * Collects all natural language templates from
 * LanguageTemplateContainer-java-files used for the given test application.
 * 
 * @author Reik Oberrath
 */
public class LanguageTemplateCollector 
{
	private List<JavaFieldData> languageTemplateContainerJavaFields;

	public LanguageTemplateCollector(final List<JavaFieldData> languageTemplateContainerJavaFields) {
		this.languageTemplateContainerJavaFields = languageTemplateContainerJavaFields;
	}

	/**
	 * Searches for all LanguageTemplates contained in all
	 * LanguageTemplatesContainers used in testCaseJavaTemplate and parses them into
	 * LanguageTemplatePattern.
	 * 
	 * @param jUnitTestCaseTemplate
	 *            JUnit file template for the application under test
	 * @return List of NaturalLanguagePatterns found for each
	 *         LanguageTemplatesContainer
	 */
	public static HashMap<Filename, List<LanguageTemplatePattern>> doYourJob(final List<JavaFieldData> languageTemplateContainerJavaFields) {
		return new LanguageTemplateCollector(languageTemplateContainerJavaFields).findAllLanguageTemplates();
	}

	protected HashMap<Filename, List<LanguageTemplatePattern>> findAllLanguageTemplates() 
	{
		// find LanguageTemplate in container classes
		final HashMap<Filename, List<LanguageTemplatePattern>> toReturn = new HashMap<>();
		languageTemplateContainerJavaFields.forEach(
				javaField -> toReturn.put(new Filename(javaField.type.getSimpleName()), 
						                  findLanguageTemplatesIn(javaField)));

		// check for duplicates in the naturalLanguagePattern of the LanguageTemplates
		validate(toReturn);

		return toReturn;
	}

	/**
	 * Searches for LanguageTemplates in NaturalLanguagePattern in
	 * languageTemplatesContainer
	 * 
	 * @param data
	 *            on the java field that represent a LanguageTemplatesContainer
	 * @return List of NaturalLanguagePatterns derived from the LanguageTemplates
	 *         found in the languageTemplatesContainer.
	 */
	protected List<LanguageTemplatePattern> findLanguageTemplatesIn(final JavaFieldData languageTemplateContainerJavaField) 
	{
	    final List<LanguageTemplatePattern> toReturn = new ArrayList<>();

		Method[] methods = languageTemplateContainerJavaField.type.getMethods();
		for (Method method : methods) 
		{
//			System.err.println(languageTemplateContainerJavaField.name + "." + method.getName());
//			if (method.getName().startsWith("clickMainMenuI")) {
//				System.err.println("");
//			}
			
			if (method.isAnnotationPresent(LanguageTemplates.class)) 
			{
				// multiple occurances of LanguageTemplates annotations
				final Filename filename = new Filename(languageTemplateContainerJavaField.type.getSimpleName());
				final String containerFieldName = languageTemplateContainerJavaField.name;
				toReturn.addAll(LanguageTemplatePattern.createFrom(method, filename, containerFieldName));
			} 
			else if (method.isAnnotationPresent(LanguageTemplate.class)) 
			{
				final Filename filename = new Filename(languageTemplateContainerJavaField.type.getSimpleName());
				final String containerFieldName = languageTemplateContainerJavaField.name;
				toReturn.add(new LanguageTemplatePattern(method, filename, containerFieldName));
			}
		}

		return toReturn;
	}


	/**
	 * check for duplicates in the naturalLanguagePattern of the LanguageTemplates
	 */
	private void validate(HashMap<Filename, List<LanguageTemplatePattern>> toReturn) 
	{
		final List<LanguageTemplatePattern> naturalLanguagePatterns = new ArrayList<>();
		Set<Filename> keySet = toReturn.keySet();
		for (Filename key : keySet) {
			naturalLanguagePatterns.addAll(toReturn.get(key));
		}

		while (naturalLanguagePatterns.size() > 1) {
			removeFirstElementIfUniqueOrThrowException(naturalLanguagePatterns);
		}
	}

	private void removeFirstElementIfUniqueOrThrowException(final List<LanguageTemplatePattern> naturalLanguagePatterns) 
	{
		LanguageTemplatePattern firstElement = naturalLanguagePatterns.remove(0);
		naturalLanguagePatterns.forEach(pattern -> compareNaturalLanguagePatterns(pattern, firstElement));
	}

	private void compareNaturalLanguagePatterns(final LanguageTemplatePattern pattern1,
											    final LanguageTemplatePattern pattern2) 
	{
		if (pattern1.isIdentical(pattern2)) {
			ExceptionHandlingUtil.throwClassifiedException(ErrorCode.LANGUAGE_TEMPLATE_PARSING__DUPLICATES,
					pattern1.getMethodInfo(), pattern2.getMethodInfo());
		}
	}

}