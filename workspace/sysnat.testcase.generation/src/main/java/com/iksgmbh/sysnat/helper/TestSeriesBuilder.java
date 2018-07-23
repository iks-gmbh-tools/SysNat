package com.iksgmbh.sysnat.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.JavaCommand;
import com.iksgmbh.sysnat.testdataimport.TestDataImporter;

/**
 * Searches the found test cases for parameterized ones.
 * Such a test case represents virtual one that has to be
 * transformed in a real one by inserting a dataset used 
 * as test data during execution. For each dataset found
 * for the provided test parameter a separate real test case
 * is created.
 * 
 * @author Reik Oberrath
 */
public class TestSeriesBuilder 
{
	private static final String TEST_PARAMETER_IDENTIFIER_METHOD_CALL = ".applyTestParameter(";
	
	private HashMap<Filename, List<JavaCommand>> javaCommandCollectionRaw;
	private HashMap<Filename, List<JavaCommand>> javaCommandCollection = new HashMap<>();
	private HashMap<Filename, String> testParameter = new HashMap<>();
	private TestDataImporter testDataImporter;
	private String nameOfCurrentFile;
	
	private TestSeriesBuilder(final HashMap<Filename, List<JavaCommand>> aJavaCommandCollectionRaw) {
		this.javaCommandCollectionRaw = aJavaCommandCollectionRaw;
		this.testDataImporter = new TestDataImporter(GenerationRuntimeInfo.getInstance().getTestdataDir());
	}
	
	public static HashMap<Filename, List<JavaCommand>> doYourJob(final HashMap<Filename, List<JavaCommand>> aJavaCommandCollectionRaw) {
		return new TestSeriesBuilder(aJavaCommandCollectionRaw).buildTestCaseSeriesIfParameterized();
	}
	
	private HashMap<Filename, List<JavaCommand>> buildTestCaseSeriesIfParameterized() 
	{
		javaCommandCollectionRaw.keySet().stream().filter(this::isTestCaseParameterized)
		                                          .forEach(this::buildTestCaseSeries);
		return javaCommandCollection;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void buildTestCaseSeries(final Filename filename)
	{
		final List<JavaCommand> commands = javaCommandCollectionRaw.get(filename);
		final String testParameterValue = testParameter.get(filename);
		final Hashtable<String, Properties> loadedDatasets = 
		                testDataImporter.loadTestdata(testParameterValue);
		final List<String> dataSetNames = new ArrayList(loadedDatasets.keySet());
		nameOfCurrentFile = filename.value;
		dataSetNames.forEach(dataSetName -> buildTestCase(dataSetName, commands)); 
	}
	
	private void buildTestCase(final String dataSetName, 
			                   final List<JavaCommand> commands) 
	{
		final List<JavaCommand> newCommands = new ArrayList<>();
		String testId = "";
		
		for (JavaCommand javaCommand : commands) {
			if (javaCommand.value.contains( "startNewTestCase" )) {
				testId = extractTestId(javaCommand.value);
				newCommands.add(new JavaCommand("languageTemplatesCommon.startNewTestCase(\"" + testId + "_" + dataSetName + "\");"));
			} else if (javaCommand.value.contains(TEST_PARAMETER_IDENTIFIER_METHOD_CALL)) {
					newCommands.add(new JavaCommand("languageTemplatesCommon.importTestData(\"" + dataSetName + "\");"));
			} else {
				newCommands.add(javaCommand);
			}
		}

		int pos = nameOfCurrentFile.lastIndexOf('/');
		String packagePath = "";
		if (pos > -1) packagePath = nameOfCurrentFile.substring(0, pos) + "/";
		final String filenameWithPath = packagePath + testId + "_" + dataSetName + ".java";
		javaCommandCollection.put(new Filename(filenameWithPath), newCommands); // no transformation needed
	}

	private String extractTestId(String command) 
	{
		final int pos1 = command.indexOf('"');
		final int pos2 = command.lastIndexOf('"');
		String toReturn = command.substring(pos1+1, pos2);
		if ("<filename>".equals(toReturn) || "<Dateiname>".equals(toReturn)) {
			toReturn = SysNatStringUtil.cutExtension(nameOfCurrentFile);
			int pos = toReturn.replaceAll("\\\\", "/").lastIndexOf("/") + 1;
			toReturn = toReturn.substring(pos);
		}
		return toReturn;
	}

	private boolean isTestCaseParameterized(final Filename filename)
	{
		final List<JavaCommand> commands = javaCommandCollectionRaw.get(filename);
		
		for (JavaCommand javaCommand : commands) 
		{
			if (javaCommand.value.contains( TEST_PARAMETER_IDENTIFIER_METHOD_CALL )) {
				testParameter.put(filename, extractTestParameter(javaCommand.value));
				return true;
			}
		}
		
		javaCommandCollection.put(filename, commands); // no transformation needed
		return false;
	}

	private String extractTestParameter(String value) 
	{
		final int pos = value.indexOf(TEST_PARAMETER_IDENTIFIER_METHOD_CALL) + TEST_PARAMETER_IDENTIFIER_METHOD_CALL.length() + 1;
		return value.substring(pos, value.length()-3).trim();
	}
}
