package com.iksgmbh.sysnat.testdataimport.domain;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import com.iksgmbh.sysnat.common.exception.SysNatValidationException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplyIgnoreLineDefinitionScope;

/**
 * A document may be validated by comparing its content with another document's content (a Should-Be-Document).
 * Typically, there a number of details that has to be ignored for such a comparison,
 * e.g. a order numbers or a order date value. 
 * For this purpose of ignorance a number of DocumentsComparisonValidationRules can be formulated
 * in natural language (in a validation file) that define details that will be ignored for
 * content comparison. Those natural language instructions will be parsed by the ValidationFileReader 
 * into DocumentsComparisonValidationRules. Those rules will used to construct a PdfCompareIgnoreConfig 
 * instance later on in the test execution.
 * 
 * @author Reik Oberrath
 */
public class DocumentContentCompareValidationRule implements DocumentValidationRule
{
	public enum ComparisonRuleType { ShouldBeFile,   // rule to define the should-be-file used for comparison
		                             Dateformat,     // rule to ignore lines that represent or start with or end with a date value defined by a pattern e.g. DD.mm.YYYY
		                             Substring,      // rule to ignore lines that contain a certain value
		                             Prefix,         // rule to ignore lines that start a certain value
		                             Regex,          // rule to ignore lines that match a certain regular expression
		                             IgnoreBetween,  // rule to ignore chars between to identifier values in all lines
		                             LineDefinition  // rule to ignore lines that are defined by a <page number>-<line number>-<scope> combination, e.g. 5-21-1
		                                             //         scope is either 1 (apply to first document only)
			                                         //         or 2 (apply to second document only)
			                                         //         or BOTH (apply to both documents compared)
		                           };
	
	private ComparisonRuleType type;
	
	/**
	 * A date format, substring, prefix, regex or linedefinition
	 */
	private String value;

	public DocumentContentCompareValidationRule(String aType, String aValue)
	{
		this.type = ComparisonRuleType.valueOf(aType);
		
		if (type == ComparisonRuleType.Dateformat) {
			new SimpleDateFormat(aValue);  // throws an error if dateformat is invalid
			value = aValue;
		} 
		else if (type == ComparisonRuleType.Regex) 
		{
			try {
				Pattern.compile(aValue);  // throws an error if regex is invalid
			} catch (Exception e) {
				throw new SysNatValidationException("The value " + aValue + " used in a validation file represents no valid Regex-Expression.");
			}
			value = aValue;
		} else {
			this.value = aValue;
		}
	}

	public String getValue()
	{
		return value;
	}

	public ComparisonRuleType getType()
	{
		return type;
	}

	@Override
	public String toString()
	{
		return "DocumentContentCompareValidationRule [type=" + type + ", value=" + value.toString() + "]";
	}

	public static String buildLineDefinition(String aLineNo, String aPageNo, String aScope)
	{
		// check validity
		ApplyIgnoreLineDefinitionScope scope = ApplyIgnoreLineDefinitionScope.valueOf(aScope);
		int pageNo = Integer.valueOf(aPageNo);
		return buildLineDefinitionIdentifier(scope, pageNo, aLineNo);
	}

	private static String buildLineDefinitionIdentifier(ApplyIgnoreLineDefinitionScope scope,
	                                                    int pageNo,
	                                                    String lineNo)
	{
		// must match DocumentCompareIgnoreConfig.buildLineDefinitionIdentifier
		return scope.name() + ":" + pageNo + ":" + lineNo;
	}	
}
