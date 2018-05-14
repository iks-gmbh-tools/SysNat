package com.iksgmbh.sysnat.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.iksgmbh.sysnat.annotation.LanguageTemplate;
import com.iksgmbh.sysnat.annotation.LanguageTemplateContainer;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaFieldData;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;
import com.iksgmbh.sysnat.exception.SysNatException.ErrorCode;
import com.iksgmbh.sysnat.utils.ExceptionHandlingUtil;

/**
 * Collects all natural language templates from LanguageTemplateContainer-java-files used for the given test application.
 * 
 * @author Reik Oberrath
 */
public class LanguageTemplateCollector 
{
	private Class<?> testCaseJavaTemplate;

	public LanguageTemplateCollector(final Class<?> aTestCaseJavaTemplate) {
		this.testCaseJavaTemplate = aTestCaseJavaTemplate;
	}

	/**
	 * Searches for all LanguageTemplates contained in all LanguageTemplatesContainers used in testCaseJavaTemplate
	 * and parses them into LanguageTemplatePattern.
	 * 
	 * @param testCaseJavaTemplate JUnit file template for the application under test
	 * @return List of NaturalLanguagePatterns found for each LanguageTemplatesContainer
	 */
	public static HashMap<Filename, List<LanguageTemplatePattern>> doYourJob(final Class<?> testCaseJavaTemplate) 
	{
		return new LanguageTemplateCollector(testCaseJavaTemplate).findAllLanguageTemplates();
	}

	protected HashMap<Filename, List<LanguageTemplatePattern>> findAllLanguageTemplates() 
	{
		// step A: find languageTemplateContainer in java template
		final List<JavaFieldData> languageTemplateContainerJavaFields = findLanguageTemplatesContainerIn(testCaseJavaTemplate);
		
		
		// step B: find LanguageTemplate in container classes
		final HashMap<Filename, List<LanguageTemplatePattern>> toReturn = new HashMap<>();
		languageTemplateContainerJavaFields.forEach(javaField -> toReturn.put(new Filename(javaField.type.getSimpleName()), 
				                                                              findLanguageTemplatesIn(javaField)));

		// step C: check for duplicates in the naturalLanguagePattern of the LanguageTemplates
		validate(toReturn);

		return toReturn;
	}
	
	/**
	 * Searches for LanguageTemplates in NaturalLanguagePattern in languageTemplatesContainer
	 * @param data on the java field that represent a LanguageTemplatesContainer
	 * @return List of NaturalLanguagePatterns derived from the LanguageTemplates found in the languageTemplatesContainer.
	 */
	protected List<LanguageTemplatePattern> findLanguageTemplatesIn(final JavaFieldData languageTemplateContainerJavaField) 
	{
		final List<LanguageTemplatePattern> toReturn = new ArrayList<>();
		
		Method[] methods = languageTemplateContainerJavaField.type.getMethods();
		for (Method method : methods) 
		{
			if (method.isAnnotationPresent(LanguageTemplate.class)) 
			{
				final Filename filename = new Filename(languageTemplateContainerJavaField.type.getSimpleName());
				final String containerFieldName = languageTemplateContainerJavaField.name;
				toReturn.add(new LanguageTemplatePattern(method, filename, containerFieldName));
			}
		}
		
		return toReturn;
	}
	
	
	/**
	 * Searches for LanguageTemplatesContainers in testCaseJavaTemplate
	 * @param testCaseJavaTemplate
	 * @return List of LanguageTemplatesContainers found in testCaseJavaTemplate
	 */
	private List<JavaFieldData> findLanguageTemplatesContainerIn(Class<?> testCaseJavaTemplate) 
	{
		final List<JavaFieldData> toReturn = new ArrayList<>();
		Field[] fields = testCaseJavaTemplate.getDeclaredFields();
		for (Field field : fields) 
		{
			if (field.isAnnotationPresent(LanguageTemplateContainer.class)) {
				toReturn.add(new JavaFieldData(field.getName(), field.getType()));
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

	private void removeFirstElementIfUniqueOrThrowException(List<LanguageTemplatePattern> naturalLanguagePatterns) 
	{
		LanguageTemplatePattern firstElement = naturalLanguagePatterns.get(0);
		naturalLanguagePatterns.remove(0);
		naturalLanguagePatterns.forEach(pattern -> compareNaturalLanguagePatterns(pattern, firstElement));
	}


	private void compareNaturalLanguagePatterns(final LanguageTemplatePattern pattern1,
			                                    final LanguageTemplatePattern pattern2) 
	{
		if ( pattern1.isIdentical(pattern2)) {
			ExceptionHandlingUtil.throwClassifiedException(ErrorCode.LANGUAGE_TEMPLATE_PARSING__DUPLICATES,
                    pattern1.getMethodInfo(), pattern2.getMethodInfo());
		}
	}

}
