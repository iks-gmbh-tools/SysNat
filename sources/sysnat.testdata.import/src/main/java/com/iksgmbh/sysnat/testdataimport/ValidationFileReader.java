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
package com.iksgmbh.sysnat.testdataimport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.common.exception.SysNatValidationException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentContentSearchValidationRule;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentContentSearchValidationRule.ContentRuleType;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentValidationRule;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentContentCompareValidationRule;
import com.iksgmbh.sysnat.testdataimport.domain.DocumentContentCompareValidationRule.ComparisonRuleType;

/**
 * Reads a dat file and parses the content to list of properties.
 * 
 * @author Reik Oberrath
 */
public class ValidationFileReader
{
	static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/DocumentContentValidation", Locale.getDefault());
	static final ResourceBundle BUNDLE_EN = ResourceBundle.getBundle("bundles/DocumentContentValidation", Locale.ENGLISH);

	static final String REGEX_ANY = ".*";
	private static final String SPACE_MASK = "s_p_a_c_e";
	
	private static final String PATTERN_CONTENT_RULE_WHOLE_DOCUMENT = BUNDLE.getString("ContentValidationRuleContains").replace("\"\"", REGEX_ANY).replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_CONTENT_RULE_PAGE = BUNDLE.getString("ContentValidationRuleContainsOnPage").replace("\"\"", REGEX_ANY).replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_CONTENT_RULE_LINE = BUNDLE.getString("ContentValidationRuleContainsOnLine").replace("\"\"", REGEX_ANY).replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_CONTENT_RULE_RELATIVE_PAGE = BUNDLE.getString("ContentValidationRuleContainsOnRelativePage").replaceAll("\"\"", REGEX_ANY).replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_CONTENT_RULE_RELATIVE_LINE = BUNDLE.getString("ContentValidationRuleContainsOnRelativeLine").replaceAll("\"\"", REGEX_ANY).replaceAll("NN", REGEX_ANY).trim();

	private static final String PATTERN_CONTENT_RULE_WHOLE_DOCUMENT_EN = BUNDLE_EN.getString("ContentValidationRuleContains").replace("\"\"", REGEX_ANY).replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_CONTENT_RULE_PAGE_EN = BUNDLE_EN.getString("ContentValidationRuleContainsOnPage").replace("\"\"", REGEX_ANY).replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_CONTENT_RULE_LINE_EN = BUNDLE_EN.getString("ContentValidationRuleContainsOnLine").replace("\"\"", REGEX_ANY).replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_CONTENT_RULE_RELATIVE_PAGE_EN = BUNDLE_EN.getString("ContentValidationRuleContainsOnRelativePage").replaceAll("\"\"", REGEX_ANY).replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_CONTENT_RULE_RELATIVE_LINE_EN = BUNDLE_EN.getString("ContentValidationRuleContainsOnRelativeLine").replaceAll("\"\"", REGEX_ANY).replaceAll("NN", REGEX_ANY).trim();
	
	private static final String PATTERN_COMPARISON_RULE_IDENTIFIER = BUNDLE.getString("ComparisonRuleIdentifier");
	private static final String PATTERN_COMPARISON_RULE_SHOULDBEFILE = BUNDLE.getString("ComparisonRuleShouldBeFile").replaceAll("\"\"", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_DATEFORMAT = BUNDLE.getString("ComparisonRuleDateformat").replaceAll("\"\"", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_PREFIX = BUNDLE.getString("ComparisonRulePrefix").replaceAll("\"\"", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_SUBSTRING = BUNDLE.getString("ComparisonRuleSubstring").replaceAll("\"\"", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_REGEX = BUNDLE.getString("ComparisonRuleRegex").replaceAll("\"\"", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_LINE1 = BUNDLE.getString("ComparisonRuleLine1").replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_LINE2 = BUNDLE.getString("ComparisonRuleLine2").replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_LINEBOTH = BUNDLE.getString("ComparisonRuleLineBoth").replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_IGNORE_BEWTEEN = BUNDLE.getString("ComparisonRuleIgnoreBetween").replaceAll("NN", REGEX_ANY).trim();
	
	private static final String PATTERN_COMPARISON_RULE_IDENTIFIER_EN = BUNDLE_EN.getString("ComparisonRuleIdentifier");
	private static final String PATTERN_COMPARISON_RULE_SHOULDBEFILE_EN = BUNDLE_EN.getString("ComparisonRuleShouldBeFile").replaceAll("\"\"", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_DATEFORMAT_EN = BUNDLE_EN.getString("ComparisonRuleDateformat").replaceAll("\"\"", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_PREFIX_EN = BUNDLE_EN.getString("ComparisonRulePrefix").replaceAll("\"\"", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_SUBSTRING_EN = BUNDLE_EN.getString("ComparisonRuleSubstring").replaceAll("\"\"", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_REGEX_EN = BUNDLE_EN.getString("ComparisonRuleRegex").replaceAll("\"\"", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_LINE1_EN = BUNDLE_EN.getString("ComparisonRuleLine1").replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_LINE2_EN = BUNDLE_EN.getString("ComparisonRuleLine2").replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_LINEBOTH_EN = BUNDLE_EN.getString("ComparisonRuleLineBoth").replaceAll("NN", REGEX_ANY).trim();
	private static final String PATTERN_COMPARISON_RULE_IGNORE_BEWTEEN_EN = BUNDLE_EN.getString("ComparisonRuleIgnoreBetween").replaceAll("NN", REGEX_ANY).trim();
	
	private List<String> lines;
	private boolean compareValidationMode;

	ValidationFileReader(final File aDataFile) 
	{
		lines = extractDataLines(SysNatFileUtil.readTextFile(aDataFile));
		compareValidationMode = lines.stream()
				                     .filter( line -> line.startsWith(PATTERN_COMPARISON_RULE_IDENTIFIER)
				                                   || line.startsWith(PATTERN_COMPARISON_RULE_IDENTIFIER_EN))
                     	             .findFirst().isPresent();
		
		
	}

	public static List<DocumentValidationRule> doYourJob(final File file) {
		return new ValidationFileReader(file).parseValidationRules();
	}
	
    private List<DocumentValidationRule> parseValidationRules()
	{
		if (compareValidationMode) {
			return parseCompareValidationRules();

		}
		return parseSearchValidationRules();
	}

	/**
     * Parses a validation file that configures the comparison of two documents
     *  
     * @param file that contains the validation rules as natural language instructions
     * @return List<DocumentsComparisonValidationRule>
     */
	private List<DocumentValidationRule> parseCompareValidationRules()
	{
		final List<DocumentValidationRule> toReturn = new ArrayList<>();

		for (String line: lines) {
			parseLineToCompareValidationRule(line.trim(), toReturn);
		}

		return toReturn;
	}

	/**
     * Parses a validation file that is used to verify content of a document 
     * by searching for an expected text.
     *  
     * @param file that contains the validation rules as natural language instructions
     * @return List<DocumentValidationRule>
     */
	private List<DocumentValidationRule> parseSearchValidationRules()
	{
		final List<DocumentValidationRule> toReturn = new ArrayList<>();

		for (String line: lines) {
			parseLineToSearchValidationRule(line, toReturn);
		}

		return toReturn;
	}
	
	
    void parseLineToCompareValidationRule(String line, List<DocumentValidationRule> toReturn)
	{
		try {
			DocumentContentCompareValidationRule rule = toComparisonValidationRule(line);
			if (rule == null) throwSysNatValidationExceptionFor(line);
			toReturn.add(rule);
		}
		catch (SysNatValidationException e) 
		{
			throw e;
		} 
		catch (Exception e) 
		{
			//e.printStackTrace();
			throwSysNatValidationExceptionFor(line);
		}
	}


	private void throwSysNatValidationExceptionFor(String line)
	{
		String errorMessage = BUNDLE.getString("InvalidValidationRuleMessage");
		errorMessage = errorMessage.replace("XY", line);
		System.err.println("Error parsing Validation Rule: " + line);
		System.err.println("Possible Reasons are");
		System.err.println("- A quotation mark is missing for an alphanumeric values.");
		System.err.println("- A space is missing to separate values embedded in the rule.");
		System.err.println("- An alphanumeric value is used instead of an numeric one.");
		System.err.println("- The rule is completely unknown.");
		throw new SysNatValidationException(errorMessage);
	}

	void parseLineToSearchValidationRule(String line, List<DocumentValidationRule> toReturn)
	{
		try {
			DocumentContentSearchValidationRule rule = toSearchValidationRule(line);
			if (rule == null) throwSysNatValidationExceptionFor(line);
			toReturn.add(rule);
		}
		catch (SysNatValidationException e) 
		{
			throw e;
		}
		catch (Exception e) 
		{
			//e.printStackTrace();
			throwSysNatValidationExceptionFor(line);
		}
	}
	
	
	private DocumentContentCompareValidationRule toComparisonValidationRule(String line)
	{
		final List<String> ruleRawData = new ArrayList<>();

		if (line.matches(PATTERN_COMPARISON_RULE_SHOULDBEFILE)) {
			ruleRawData.add(ComparisonRuleType.ShouldBeFile.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_SHOULDBEFILE).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		} else if (line.matches(PATTERN_COMPARISON_RULE_SHOULDBEFILE_EN)) {
			ruleRawData.add(ComparisonRuleType.ShouldBeFile.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_SHOULDBEFILE_EN).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		}

		if (line.matches(PATTERN_COMPARISON_RULE_DATEFORMAT)) {
			ruleRawData.add(ComparisonRuleType.Dateformat.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_DATEFORMAT).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		} else if (line.matches(PATTERN_COMPARISON_RULE_DATEFORMAT_EN)) {
			ruleRawData.add(ComparisonRuleType.Dateformat.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_DATEFORMAT_EN).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		}
		
		if (line.matches(PATTERN_COMPARISON_RULE_PREFIX)) {
			ruleRawData.add(ComparisonRuleType.Prefix.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_PREFIX).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		} else if (line.matches(PATTERN_COMPARISON_RULE_PREFIX_EN)) {
			ruleRawData.add(ComparisonRuleType.Prefix.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_PREFIX_EN).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		}

		if (line.matches(PATTERN_COMPARISON_RULE_SUBSTRING)) {
			ruleRawData.add(ComparisonRuleType.Substring.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_SUBSTRING).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		} else if (line.matches(PATTERN_COMPARISON_RULE_SUBSTRING_EN)) {
			ruleRawData.add(ComparisonRuleType.Substring.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_SUBSTRING_EN).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		}
		
		if (line.matches(PATTERN_COMPARISON_RULE_REGEX)) {
			ruleRawData.add(ComparisonRuleType.Regex.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_REGEX).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		} else if (line.matches(PATTERN_COMPARISON_RULE_REGEX_EN)) {
			ruleRawData.add(ComparisonRuleType.Regex.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_REGEX_EN).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		}

		if (line.matches(PATTERN_COMPARISON_RULE_LINE1)) {
			List<String> values = extractValues(line, PATTERN_COMPARISON_RULE_LINE1);
			String lineDef = DocumentContentCompareValidationRule.buildLineDefinition(values.get(0), values.get(1), "Doc1");
			return new DocumentContentCompareValidationRule(ComparisonRuleType.LineDefinition.name(), lineDef);
		} else if (line.matches(PATTERN_COMPARISON_RULE_LINE1_EN)) {
			List<String> values = extractValues(line, PATTERN_COMPARISON_RULE_LINE1_EN);
			String lineDef = DocumentContentCompareValidationRule.buildLineDefinition(values.get(0), values.get(1), "Doc1");
			return new DocumentContentCompareValidationRule(ComparisonRuleType.LineDefinition.name(), lineDef);
		}
		
		if (line.matches(PATTERN_COMPARISON_RULE_LINE2)) {
			List<String> values = extractValues(line, PATTERN_COMPARISON_RULE_LINE2);
			String lineDef = DocumentContentCompareValidationRule.buildLineDefinition(values.get(0), values.get(1), "Doc2");
			return new DocumentContentCompareValidationRule(ComparisonRuleType.LineDefinition.name(), lineDef);
		} else if (line.matches(PATTERN_COMPARISON_RULE_LINE2_EN)) {
			List<String> values = extractValues(line, PATTERN_COMPARISON_RULE_LINE2_EN);
			String lineDef = DocumentContentCompareValidationRule.buildLineDefinition(values.get(0), values.get(1), "Doc2");
			return new DocumentContentCompareValidationRule(ComparisonRuleType.LineDefinition.name(), lineDef);
		}

		if (line.matches(PATTERN_COMPARISON_RULE_LINEBOTH)) {
			List<String> values = extractValues(line, PATTERN_COMPARISON_RULE_LINEBOTH);
			String lineDef = DocumentContentCompareValidationRule.buildLineDefinition(values.get(0), values.get(1), "BOTH");
			return new DocumentContentCompareValidationRule(ComparisonRuleType.LineDefinition.name(), lineDef);
		} else if (line.matches(PATTERN_COMPARISON_RULE_LINEBOTH_EN)) {
			List<String> values = extractValues(line, PATTERN_COMPARISON_RULE_LINEBOTH_EN);
			String lineDef = DocumentContentCompareValidationRule.buildLineDefinition(values.get(0), values.get(1), "BOTH");
			return new DocumentContentCompareValidationRule(ComparisonRuleType.LineDefinition.name(), lineDef);
		}
		
		if (line.matches(PATTERN_COMPARISON_RULE_IGNORE_BEWTEEN)) {
			ruleRawData.add(ComparisonRuleType.IgnoreBetween.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_IGNORE_BEWTEEN).get(0) + "|#|" 
			              + extractValues(line, PATTERN_COMPARISON_RULE_IGNORE_BEWTEEN).get(1);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		} else if (line.matches(PATTERN_COMPARISON_RULE_IGNORE_BEWTEEN)) {
			ruleRawData.add(ComparisonRuleType.IgnoreBetween.name());
			String value = extractValues(line, PATTERN_COMPARISON_RULE_IGNORE_BEWTEEN_EN).get(0);
			ruleRawData.add(value);
			return new DocumentContentCompareValidationRule(ruleRawData.get(0), ruleRawData.get(1));
		}
			
		
		return null;
	}


	List<String> extractValues(String originalLine, String originalPattern)
	{
		List<String> toReturn = new ArrayList<String>();
		String line = maskSpacesInAlphanumericValues(originalLine);
		String pattern = originalPattern;
		
		while (pattern.contains(REGEX_ANY)) 
		{
			int pos = pattern.indexOf(REGEX_ANY);
			String patternPart = pattern.substring(0, pos);
			pos = patternPart.length();
			line = line.substring(pos).trim();
			pattern = pattern.substring(pos).trim();
			pos = line.indexOf(" ");
			if (pos != -1) 
			{
				// value is separated by space
				String value = unmaskSpacesInAlphanumericValues(line.substring(0, pos));
				toReturn.add(value);
				line = line.substring(pos).trim();
				pos = pattern.indexOf(" ");
				pattern = pattern.substring(pos).trim();
			} 
			else 
			{				
				// value is not separated by space
				patternPart = pattern.substring(REGEX_ANY.length());
				pos = patternPart.length();
				String value = unmaskSpacesInAlphanumericValues(line.substring(0, line.length()-pos));
				toReturn.add(value);
				pattern = patternPart;
			}
		}

		return toReturn;
	}
	
	private String unmaskSpacesInAlphanumericValues(String value) 
	{
		value = value.replaceAll(SPACE_MASK, " ");
		if (value.startsWith("\"")) value = value.substring(1, value.length()-1);
		return value;
	}

	private String maskSpacesInAlphanumericValues(String originalLine)
	{
		String line = originalLine;
		int pos = line.indexOf("\"");
		if (pos == -1) return originalLine;
		String prefix = line.substring(0, pos);
		line = line.substring(pos+1);
		pos = line.indexOf("\"");
		String value = line.substring(0, pos);
		String maskedValue = value.replaceAll(" ", SPACE_MASK);
		originalLine = originalLine.replace(value, maskedValue);
		String restOfLine = line.substring(pos+1);
		
		return prefix + "\"" + maskedValue + "\"" + maskSpacesInAlphanumericValues(restOfLine);
	}


	private DocumentContentSearchValidationRule toSearchValidationRule(String line)
	{
		final List<String> ruleRawData = new ArrayList<>();

		if (line.matches(PATTERN_CONTENT_RULE_WHOLE_DOCUMENT)) {
			ruleRawData.add(ContentRuleType.Contains.name());
			List<String> values = extractValues(line, PATTERN_CONTENT_RULE_WHOLE_DOCUMENT);
			ruleRawData.add(values.get(0));
			return toContentValidationRule(ruleRawData);
		} else if (line.matches(PATTERN_CONTENT_RULE_WHOLE_DOCUMENT_EN)) {
			ruleRawData.add(ContentRuleType.Contains.name());
			List<String> values = extractValues(line, PATTERN_CONTENT_RULE_WHOLE_DOCUMENT_EN);
			ruleRawData.add(values.get(0));
			return toContentValidationRule(ruleRawData);
		}		
		
		if (line.matches(PATTERN_CONTENT_RULE_PAGE) && ! line.contains("in Zeile") && ! line.contains("mit dem Text")) {
			ruleRawData.add(ContentRuleType.ContainsOnPage.name());
			List<String> values = extractValues(line, PATTERN_CONTENT_RULE_PAGE);
			ruleRawData.addAll(values);
			return toContentValidationRule(ruleRawData);
		} else if (line.matches(PATTERN_CONTENT_RULE_PAGE_EN) && ! line.contains("in line") && ! line.contains("with sequence")) {
			ruleRawData.add(ContentRuleType.ContainsOnPage.name());
			List<String> values = extractValues(line, PATTERN_CONTENT_RULE_PAGE_EN);
			ruleRawData.addAll(values);
			return toContentValidationRule(ruleRawData);
		}		

		if (line.matches(PATTERN_CONTENT_RULE_LINE) && line.contains("in Zeile") && ! line.contains("mit dem Text")) {
			ruleRawData.add(ContentRuleType.ContainsInLine.name());
			List<String> values = extractValues(line, PATTERN_CONTENT_RULE_LINE);
			ruleRawData.addAll(values);
			return toContentValidationRule(ruleRawData);
		} else if (line.matches(PATTERN_CONTENT_RULE_LINE_EN) && line.contains("in line") && ! line.contains("with sequence")) {
			ruleRawData.add(ContentRuleType.ContainsInLine.name());
			List<String> values = extractValues(line, PATTERN_CONTENT_RULE_LINE_EN);
			ruleRawData.addAll(values);
			return toContentValidationRule(ruleRawData);
		}
		
		if (line.matches(PATTERN_CONTENT_RULE_RELATIVE_PAGE) && ! line.contains("in Zeile")) {
			ruleRawData.add(ContentRuleType.ContainsOnRelativePage.name());
			List<String> values = extractValues(line, PATTERN_CONTENT_RULE_RELATIVE_PAGE);
			ruleRawData.addAll(values);
			return toContentValidationRule(ruleRawData);
		} else if (line.matches(PATTERN_CONTENT_RULE_RELATIVE_PAGE_EN) && ! line.contains("in line")) {
			ruleRawData.add(ContentRuleType.ContainsOnRelativePage.name());
			List<String> values = extractValues(line, PATTERN_CONTENT_RULE_RELATIVE_PAGE_EN);
			ruleRawData.addAll(values);
			return toContentValidationRule(ruleRawData);
		}

		if (line.matches(PATTERN_CONTENT_RULE_RELATIVE_LINE)) {
			ruleRawData.add(ContentRuleType.ContainsInRelativeLine.name());
			List<String> values = extractValues(line, PATTERN_CONTENT_RULE_RELATIVE_LINE);
			ruleRawData.addAll(values);
			return toContentValidationRule(ruleRawData);
		} else if (line.matches(PATTERN_CONTENT_RULE_RELATIVE_LINE_EN)) {
			ruleRawData.add(ContentRuleType.ContainsInRelativeLine.name());
			List<String> values = extractValues(line, PATTERN_CONTENT_RULE_RELATIVE_LINE_EN);
			ruleRawData.addAll(values);
			return toContentValidationRule(ruleRawData);
		}
		
		return null;
	}
	
	private DocumentContentSearchValidationRule toContentValidationRule(List<String> ruleRawData)
	{
		switch (ruleRawData.size())
		{
			case 2: return new DocumentContentSearchValidationRule(ruleRawData.get(1));
			case 3: String pageInfo = ruleRawData.get(1);
			        try {
			        	int pageNumber = Integer.valueOf(pageInfo);
			        	return new DocumentContentSearchValidationRule(ruleRawData.get(2), pageNumber);
			        } catch (NumberFormatException e) {
			        	return new DocumentContentSearchValidationRule(ruleRawData.get(2), pageInfo);
			        }
			case 4: int lineNumber = Integer.valueOf(ruleRawData.get(2));
				    pageInfo = ruleRawData.get(1);
			        try {
			        	int pageNumber = Integer.valueOf(pageInfo);
			        	return new DocumentContentSearchValidationRule(ruleRawData.get(3), pageNumber, lineNumber);
			        } catch (NumberFormatException e) {
			        	return new DocumentContentSearchValidationRule(ruleRawData.get(3), pageInfo, lineNumber);
			        }
		}
		
		throw new SysNatValidationException("");
	}


	private List<String> extractDataLines(List<String> fileContent) 
	{
		final List<String> dataLines = new ArrayList<>();
		
		fileContent.stream().filter(line -> ! line.isEmpty())
		                    .filter(line -> ! line.trim().startsWith("#"))
		                    .forEach(line -> dataLines.add(line));
		
		return dataLines;
	}
}