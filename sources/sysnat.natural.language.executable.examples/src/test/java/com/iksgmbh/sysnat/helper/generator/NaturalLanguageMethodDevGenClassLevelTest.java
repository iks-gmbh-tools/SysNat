package com.iksgmbh.sysnat.helper.generator;

import java.io.File;
import java.util.LinkedHashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.generator.NaturalLanguageMethodDevGen.DataType;
import com.iksgmbh.sysnat.helper.generator.utils.SysNatDevGenTestUtil;
import com.iksgmbh.sysnat.utils.NaturalLanguageAnalyseUtil;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
@SuppressWarnings("unused")
public class NaturalLanguageMethodDevGenClassLevelTest
{
	private static File testDir = SysNatDevGenTestUtil.testDir;
	
	private File targetFile;

	@BeforeClass
	public static void setup() {
		TestApplicationDevGenClassLevelTest.setup();
	}
	
	@Before
	public void init() {
		testDir.delete();
		targetFile = new File(testDir, "java\\com\\iksgmbh\\sysnat\\language_templates\\testapp\\LanguageTemplatesBasics_TestApp.java");
		SysNatFileUtil.writeFile(targetFile, "{" + System.getProperty("line.separator") + "}");
	}
	
	@Test
	@Parameters(method = "createTestDataArray")
	public void buildNaturalLanguageInsstructionJavaMethod(LinkedHashMap<DataType, String> generationData, String expectedFile) 
	{
		NaturalLanguageMethodDevGen.doYourJob(generationData);
		SysNatDevGenTestUtil.assertFileContent(SysNatFileUtil.readTextFile(targetFile), expectedFile);
		if (expectedFile.contains("Complex.txt")) { // the last of the many !
			File file = new File("../sysnat.natural.language.executable.examples/target/unittest/help/ExistingNLInstructions_TestApp.html");
			SysNatDevGenTestUtil.assertFileContent(SysNatFileUtil.readTextFile(file), "NaturalLanguageMethodGenerator/helpFile.html");
		}
	}


	// #############################################################################################################

	private Object[] createTestDataArray() 
    {
        final Object[] params = {
	        new Object[] {buildGenerationData_Simple(),                     "NaturalLanguageMethodGenerator/simpleMethod.txt"},
	        new Object[] {buildGenerationData_OneDataValueParameter(),      "NaturalLanguageMethodGenerator/OneDataValueParameterMethod.txt"},
	        new Object[] {buildGenerationData_OneTestObjectParameter(),     "NaturalLanguageMethodGenerator/OneTestObjectParameterMethod.txt"},
   	        new Object[] {buildGenerationData_TwoTestObjectParameters(),    "NaturalLanguageMethodGenerator/TwoTestObjectParametersMethod.txt"},
	        new Object[] {buildGenerationData_TwoDataValuesParameters(),    "NaturalLanguageMethodGenerator/TwoDataValuesParametersMethod.txt"},
	        new Object[] {buildGenerationData_TwoDataValueTwoTestObjects(), "NaturalLanguageMethodGenerator/TwoDataValueTwoTestObjectsMethod.txt"},
	        new Object[] {buildGenerationData_ReturnValue(),                "NaturalLanguageMethodGenerator/ReturnValue.txt"},
	        new Object[] {buildGenerationData_Mixture(),                    "NaturalLanguageMethodGenerator/Mixture.txt"},
	        new Object[] {buildGenerationData_Complex(),                    "NaturalLanguageMethodGenerator/Complex.txt"},
		};
        return params;
   }


	private LinkedHashMap<DataType, String> buildGenerationData_Complex()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		
		data.put(DataType.NLExpression, "Do something with 'thisObject' and \"thisValue\" then return <myresult> for 'thatObject' with \"thatValue\".");
		data.put(DataType.MethodeName, "doSomethingComplex");
		data.put(DataType.ReportMessage, "Something done with <arg1> and <arg2>, returned ? for <arg3> and <arg4>.");
		data.put(DataType.TestApplication, "TestApp");
		data.put(DataType.MethodParameter, "anObject=thisObject" + NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR + 
		                                   "aValue=thisValue" + NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR +
		                                   "anotherObject=thatObject" + NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR + 
		                                   "anotherValue=thatValue");
		
		return data;
	}

	private LinkedHashMap<DataType, String> buildGenerationData_Mixture()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		
		data.put(DataType.NLExpression, "Do something with 'this' and \"that\" and return <toReturn>.");
		data.put(DataType.MethodeName, "doSomethingWith");
		data.put(DataType.ReportMessage, "Something done with <arg1> and <arg2> and return ? .");
		data.put(DataType.TestApplication, "TestApp");
		data.put(DataType.MethodParameter, "aValue=this" + NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR + "anObject:that");
		
		return data;
	}
	
	
	private LinkedHashMap<DataType, String> buildGenerationData_ReturnValue()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		
		data.put(DataType.NLExpression, "Store <thisObject>.");
		data.put(DataType.MethodeName, "store");
		data.put(DataType.ReportMessage, "Test object stored ?.");
		data.put(DataType.TestApplication, "TestApp");
		data.put(DataType.MethodParameter, "");
		
		return data;
	}

	private LinkedHashMap<DataType, String> buildGenerationData_TwoDataValueTwoTestObjects()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		
		data.put(DataType.NLExpression, "Do something with \"thisValue\", 'thisObject', \"thatValue\" and 'thatObject'.");
		data.put(DataType.MethodeName, "doSomethingWith");
		data.put(DataType.ReportMessage, "Something done with <arg1>, <arg2>, <arg3> and <arg4>.");
		data.put(DataType.TestApplication, "TestApp");
		data.put(DataType.MethodParameter, "value1=this" + NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR + 
				                           "value2:thisObject" + NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR + 
				                           "value3=that" + NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR + 
				                           "value4:thatObject");
		
		return data;
	}
	
	
	private LinkedHashMap<DataType, String> buildGenerationData_TwoDataValuesParameters()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		
		data.put(DataType.NLExpression, "Do something with \"this\" and \"that\".");
		data.put(DataType.MethodeName, "doSomethingWith");
		data.put(DataType.ReportMessage, "Something done with <arg1> and <arg2>.");
		data.put(DataType.TestApplication, "TestApp");
		data.put(DataType.MethodParameter, "aValue=this" + NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR + "anotherValue=that");
		
		return data;
	}
	

	private LinkedHashMap<DataType, String> buildGenerationData_TwoTestObjectParameters()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		
		data.put(DataType.NLExpression, "Do something with 'this' and 'that'.");
		data.put(DataType.MethodeName, "doSomethingWith");
		data.put(DataType.ReportMessage, "Something done with <arg1> and <arg2>.");
		data.put(DataType.TestApplication, "TestApp");
		data.put(DataType.MethodParameter, "anObject:this" + NaturalLanguageMethodDevGen.PARAMETER_SEPARATOR + "anotherObject:this");
		
		return data;
	}

	private LinkedHashMap<DataType, String> buildGenerationData_OneTestObjectParameter()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		
		data.put(DataType.NLExpression, "Do something with 'thisTestObject'.");
		data.put(DataType.MethodeName, "doSomethingWith");
		data.put(DataType.ReportMessage, "Something done with <arg1>.");
		data.put(DataType.TestApplication, "TestApp");
		data.put(DataType.MethodParameter, "aTestObject" + NaturalLanguageAnalyseUtil.TEST_OBJECT_OPERATOR + "thisTestObject");
		
		return data;
	}

	private LinkedHashMap<DataType, String> buildGenerationData_OneDataValueParameter()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		
		data.put(DataType.NLExpression, "Do something with \"this value\".");
		data.put(DataType.MethodeName, "doSomethingWith");
		data.put(DataType.ReportMessage, "Something done with <arg1>.");
		data.put(DataType.TestApplication, "TestApp");
		data.put(DataType.MethodParameter, "aValue" + NaturalLanguageAnalyseUtil.DATA_VALUE_OPERATOR + "this value");
		
		return data;
	}
	
	
	private LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> buildGenerationData_Simple()
	{
		LinkedHashMap<NaturalLanguageMethodDevGen.DataType, String> data = new LinkedHashMap<>();
		
		data.put(DataType.NLExpression, "Do something.");
		data.put(DataType.MethodeName, "doSomething");
		data.put(DataType.ReportMessage, "Something done.");
		data.put(DataType.TestApplication, "TestApp");
		data.put(DataType.MethodParameter, "");
		
		return data;
	}	

	
}
