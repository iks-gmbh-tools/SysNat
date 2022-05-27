package com.iksgmbh.sysnat.helper.generator;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.helper.CommandLibraryCreator;
import com.iksgmbh.sysnat.helper.MethodAnnotationSuggestionBuilder;
import com.iksgmbh.sysnat.utils.NaturalLanguageAnalyseUtil;

/**
 * Development helper to generate new method for an new natural language instruction.
 * 
 * Adapt the value of the constants to your needs and execute this class as Java application.
 * 
 * Note: Java and Properties files that exist will not be overwritten!
 * 
 * @author Reik Oberrath
 */
public class NaturalLanguageMethodDevGen
{
	public enum DataType { NLExpression, MethodeName, ReportMessage, TestApplication, MethodParameter}
	public static final String PARAMETER_SEPARATOR = "|#|";
	public static final String ARGUMENT_IDENTIFIER = "<arg";
	
	
	public static String doYourJob(LinkedHashMap<DataType, String> data) 
	{
		try {
			String code = "    " + createCode(data).replace(System.getProperty("line.separator"), System.getProperty("line.separator") + "    ");
			String testAppName = data.get(DataType.TestApplication);
			File targetFile = injectInTarget(code, testAppName);
			CommandLibraryCreator.updateFor(testAppName, targetFile);	
			return "Code successfully injected in " + targetFile.getName() + System.getProperty("line.separator") +
					"List of existing instructions in directory 'help' updated!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Error: " + e.getMessage();
		}
	}

	private static File injectInTarget(String code, String testAppName)
	{
		File javaFile = TestApplicationDevGen.buildLanguageTemplatesBasicsJavaFile(testAppName);
		String content = SysNatFileUtil.readTextFileToString(javaFile);
		int pos = content.lastIndexOf("}");
		String newContent = content.substring(0, pos).trim() + System.getProperty("line.separator") + System.getProperty("line.separator")
		                    + code + System.getProperty("line.separator") + System.getProperty("line.separator") 
		                    + "}";
		SysNatFileUtil.writeFile(javaFile, newContent);
		return javaFile;
	}

	public static String createPreview(LinkedHashMap<DataType, String> data) {
		return createCode(data).replace("<", "&lt;").replace(">", "&gt;");
	}

	private static String createCode(LinkedHashMap<DataType, String> data)
	{
		String nlExpression = data.get(DataType.NLExpression);
		
		if (nlExpression == null || nlExpression.trim().isEmpty()) {
			throw new RuntimeException(DataType.NLExpression.name() + " not set!");
		} 
		
		String annotationValueString = MethodAnnotationSuggestionBuilder.buildAnnotationValue(nlExpression);
		String returnValueString = MethodAnnotationSuggestionBuilder.buildReturnValueString(nlExpression);
		String paramsString = buildParamsString(data.get(DataType.MethodParameter));
		String methodeName = getMethodName(data);
		String testDataReferenceTransformationLine = buildTestDataReferenceTransformationLineSuggestion(data);
		String todoComment = buildTodoComment(annotationValueString);
		String returnObjectLines = buildReturnObjectLines(annotationValueString);
		String standardReportMessage = buildstandardReportMessage(data, annotationValueString);
		
		return "@LanguageTemplate(value = \"" + annotationValueString + "\")" + System.getProperty("line.separator") 
			   + "public " + returnValueString + " " + methodeName + "(" + paramsString + ")" + System.getProperty("line.separator")
			   + "{" + System.getProperty("line.separator") 
			   + testDataReferenceTransformationLine + returnObjectLines + todoComment + standardReportMessage
			   + "}";	
	}

	private static String buildstandardReportMessage(LinkedHashMap<DataType, String> data, String annotationValueString)
	{
		if (annotationValueString.contains("<>")) {
			return "";
		}
		return "    executableExample.addReportMessage(\"" + getReportMessage(data, annotationValueString) + "\");" + System.getProperty("line.separator");
	}

	private static String buildReturnObjectLines(String annotationValueString)
	{
		if (annotationValueString.contains("<>")) {
			return "    Object toStore = null; // TODO create this object - SysNat will store automatically the returned object at runtime "
					+ System.getProperty("line.separator") + "    String displayName = toStore.toString(); // adapt if needed "
					+ System.getProperty("line.separator") + "    executableExample.addReportMessage(\"Test object <b>\" + displayName + \"</b> has been stored.\");"
					+ System.getProperty("line.separator") + "    return toStore;"
					+ System.getProperty("line.separator");
		}
		return "";
	}

	private static String buildTodoComment(String annotationValueString)
	{
		if (annotationValueString.contains("<>")) {
			return "";
		}
		return "    // TODO Implement here code that corresponds to the Language Template's promise." + System.getProperty("line.separator");
	}

	private static String getMethodName(LinkedHashMap<DataType, String> data)
	{
		String methodeName = data.get(DataType.MethodeName);
		if (methodeName == null || methodeName.trim().isEmpty()) {
			methodeName = "replaceByAnExpressiveMethodName";
		} else {
			methodeName = SysNatStringUtil.firstCharToLowerCase(methodeName);
		}
		return methodeName;
	}

	private static String buildTestDataReferenceTransformationLineSuggestion(LinkedHashMap<DataType, String> data)
	{
		String testDataReferenceTransformationLine = "";
		if (! data.get(DataType.MethodParameter).isEmpty()) 
		{
			List<String> splitResult = SysNatStringUtil.split(data.get(DataType.MethodParameter), PARAMETER_SEPARATOR);
			String result = splitResult.stream().filter(e -> e.contains(NaturalLanguageAnalyseUtil.DATA_VALUE_OPERATOR)).findFirst().orElse(null);
			if (result != null) 
			{				
				String firstDataValueArgument = SysNatStringUtil.firstCharToLowerCase(result.split("=")[0]);
				testDataReferenceTransformationLine = "    // String value = executableExample.getTestDataValue(" + firstDataValueArgument + ");" 
						+ " // comment in if needed" + System.getProperty("line.separator");
			}
		}
		return testDataReferenceTransformationLine;
	}

	private static String getReportMessage(LinkedHashMap<DataType, String> data, String annotationValueString)
	{
		String reportMessage = data.get(DataType.ReportMessage);
		if (reportMessage == null || reportMessage.trim().isEmpty()) {
			reportMessage = "Executed: " + annotationValueString;
		}
		
		while (reportMessage.contains(ARGUMENT_IDENTIFIER)) {
			reportMessage = replaceNextArgumentPlaceholder(reportMessage, data.get(DataType.MethodParameter));
		}

		return reportMessage;
	}

	private static String replaceNextArgumentPlaceholder(String reportMessage, String parameterString)
	{
		int pos = reportMessage.indexOf(ARGUMENT_IDENTIFIER);
		String s = reportMessage.substring(pos + ARGUMENT_IDENTIFIER.length());
		pos = s.indexOf(">");
		s = s.substring(0, pos);
		int num;
		try {
			num = Integer.valueOf(s);
		} catch (Exception e) {
			  throw new RuntimeException("Argument with invalid mnumber in report message detected: " + reportMessage, e);
		}
		
		List<String> parameter = SysNatStringUtil.split(parameterString, PARAMETER_SEPARATOR);
		String value = parameter.get(num-1);
		String key;
		if (value.contains(NaturalLanguageAnalyseUtil.TEST_OBJECT_OPERATOR)) {
			key = SysNatStringUtil.toListOfLines(value, NaturalLanguageAnalyseUtil.TEST_OBJECT_OPERATOR).get(0);
		} else {
			key = SysNatStringUtil.toListOfLines(value, NaturalLanguageAnalyseUtil.DATA_VALUE_OPERATOR).get(0);
		}
		return reportMessage.replace(ARGUMENT_IDENTIFIER + num + ">", "<b>\" + " + key + " + \"</b>");
	}

	private static String buildParamsString(String parameters)
	{
		if (parameters.isEmpty()) return "";
		
		List<String> splitResult = SysNatStringUtil.split(parameters, PARAMETER_SEPARATOR);
		String argumentsString = "";
		
		for (String keyvalue : splitResult) 
		{
			String type = "String";
			int pos = keyvalue.indexOf(NaturalLanguageAnalyseUtil.DATA_VALUE_OPERATOR);
			if (pos == -1) {
				pos = keyvalue.indexOf(NaturalLanguageAnalyseUtil.TEST_OBJECT_OPERATOR);
				type = "Object";
			}
			String key = keyvalue.substring(0, pos);
			argumentsString += type + " " + SysNatStringUtil.firstCharToLowerCase(key) + ", ";
		}
		
		argumentsString = argumentsString.substring(0, argumentsString.length() - 2);
		
		return argumentsString;
	}

}
