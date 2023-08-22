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
package com.iksgmbh.sysnat.common.utils;

public class SysNatConstants 
{
	public static final String SYS_NAT_VERSION = "1.1.0";
	
	public static final String SYSTEM_PROPERTIES_FILENAME = "system.properties";

	/**
	 * The Double Colon is used to specify placeholders for test data values.
	 */
	public static final String DC = "::";
	
	public static final String ASTERIX = "*";
	
	public static final String ROOT_PATH_PLACEHOLDER = "<root.path>";

	public enum TestPhase {PRECONDITION, ARRANGE, ACT, ASSERT, CLEANUP, 
		                   GIVEN, WHEN, THEN, ANGENOMMEN, WENN, DANN,
		                   VORBEREITUNG, DURCHFÜHRUNG, ÜBERPRÜFUNG};

	public enum SysNatMode { Testing, Docing };
	
	public enum ResultLaunchOption { Testing, Docing, Both, None };

	public enum DialogStartTab { Testing, Docing, General };

	public enum DocumentationFormat { PDF, HTML, DOCX };
    
	public enum DocumentationType { SystemDescription, RequirementsReport };

    public enum DocumentationDepth { Maximum, Medium, Minimum };

	public enum BrowserType { CHROME, FIREFOX, EDGE };

	public enum ExecSpeed { QUICK, SCHNELL, NORMAL, SLOW, LANGSAM };

	
	public enum ApplyIgnoreLineDefinitionScope { Doc1, Doc2, BOTH };
	
	public enum ApplicationLoginParameter { LoginId, Password };
	
	public enum SwingStartParameter { InstallDir, LibDirs, JavaStartClass, MainFrameTitle, ConfigFiles };
	public enum WebStartParameter { starturl };
	
	public enum ApplicationType 
	{ 
		Web(WebStartParameter.starturl.name()),  
		Composite(APP_COMPOSITES),
		Swing(SwingStartParameter.InstallDir.name() + "," +
		      SwingStartParameter.LibDirs.name() + "," + 
			  SwingStartParameter.JavaStartClass.name()  + "," +
			  SwingStartParameter.MainFrameTitle.name()  + "," +
			  SwingStartParameter.ConfigFiles.name()); 
		
		private String startParameter; 
	    public String getStartParameter(){return startParameter;}
	    private ApplicationType(String aStartParameter){this.startParameter = aStartParameter;}		
	};	
	
	public enum GuiType { Button, 
		                  TextField,   // single line input field
		                  TextArea,
		                  ComboBox,    // selectbox with dropdown
		                  DateField,
		                  RadioButtonSelection,   
		                  ElementToReadText,
		                  CheckBox,
		                  Table,
		                  Link};
	
   /**
    * Some typical environments used. 
    * Feel free to adapt to your own needs.
    */
	public enum TargetEnvironment { LOCAL,        // IDE of deceloper
		                            DEVELOPMENT,  // Developer team environment used by CI
		                            INTEGRATION,  // Integration environment with communication with other systems
		                            ACCEPTANCE,   // Environment for final acceptance by domain experts
		                            PREPROD,      // Alternative Environment for final acceptance by domain experts
		                            PRODUCTION};  // Environment for end user (Going-Live-Environment)

	public static final String SYSNAT_MODE = "sysnat.mode";
	public static final String CONFIG_FILE_EXTENSION = ".config";
	public static final String TESTING_CONFIG_PROPERTY = DialogStartTab.Testing.name() + CONFIG_FILE_EXTENSION;
	public static final String DOCING_CONFIG_PROPERTY = DialogStartTab.Docing.name() + CONFIG_FILE_EXTENSION;
	public static final String GENERAL_CONFIG_PROPERTY = DialogStartTab.General.name() + CONFIG_FILE_EXTENSION;
	                            
	public static final String REPORT_FILENAME_TEMPLATE = "TestReport TIME_PLACEHOLDER.html";
	public static final String NO_FILTER = "-";
	public static final String COMMENT_IDENTIFIER = "//";
	public static final String QUESTION_IDENTIFIER = "? - ";
	
	public static final String TEST_APPLICATION_SETTING_KEY = "TestApplication";
	public static final String TEST_ENVIRONMENT_SETTING_KEY = "TestEnvironment";
	public static final String TEST_EXECUTION_FILTER_SETTING_KEY = "TestExecutionFilter";
	public static final String TEST_BROWSER_SETTING_KEY = "TestBrowser";
	public static final String TEST_EXECUTION_SPEED_SETTING_KEY = "TestExecutionSpeed";
	public static final String TEST_REPORT_NAME_SETTING_KEY = "TestReportName";
	public static final String TEST_ARCHIVE_DIR_SETTING_KEY = "TestReportArchiveDir";

	public static final String DOC_APPLICATION_SETTING_KEY = "DocApplication";
	public static final String DOC_TYPE_SETTING_KEY = "DocType";
	public static final String DOC_DEPTH_SETTING_KEY = "DocDepth";
	public static final String DOV_ENVIRONMENT_SETTING_KEY = "DocEnvironment";
	public static final String DOC_FORMAT_SETTING_KEY = "DocFormat";
	public static final String DOC_NAME_SETTING_KEY = "DocName";
	public static final String DOC_ARCHIVE_DIR_SETTING_KEY = "DocArchiveDir";

	public static final String USE_SYSNAT_DIALOG_SETTING_KEY = "UseSysNatDialog";
	public static final String DIALOG_START_TAB_SETTING_KEY = "DialogStartTab";
	public static final String RESULT_LAUNCH_OPTION_SETTING_KEY = "ResultLaunchOption";
	
	public static final String GREEN_HTML_COLOR = "#00B050";
	public static final String YELLOW_HTML_COLOR = "#CDE301";
	public static final String ORANGE_HTML_COLOR = "#FF6E00";
	public static final String RED_HTML_COLOR = "#FF0000";
	public static final String WHITE_HTML_COLOR = "#FFFFFF";
	public static final String BLUE_HTML_COLOR = "#0000FF";
	public static final String BLACK_HTML_COLOR = "#000000";
	
	public static final String SYS_OUT_SEPARATOR = "--------------------------------------------------------------------";

	public static final String TEST_PARAMETER = "Test-Parameter";
	public static final String TEST_DATA = "TestData";
	public static final String LINE_SEPARATOR = "<Line Separator>";
	public static final String TEST_PHASE = "Test-Phase";	

	public static final String METHOD_CALL_IDENTIFIER_TEST_PARAMETER_DEFINITION = ".applyTestParameter(";
	public static final String METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION = ".declareXXGroupForBehaviour(";
	public static final String METHOD_CALL_IDENTIFIER_BDD_KEYWORD_USAGE = ".setBddKeyword(";
	public static final String METHOD_CALL_IDENTIFIER_START_XX = ".startNewXX(";
	public static final String METHOD_CALL_IDENTIFIER_FILTER_DEFINITION = ".defineAndCheckExecutionFilter(";
	public static final String METHOD_CALL_IDENTIFIER_TEST_DATA = ".setTestData(";
	public static final String METHOD_CALL_IDENTIFIER_SET_ACTIVE_STATE = ".setActiveState(";
	public static final String METHOD_CALL_IDENTIFIER_STORE_TEST_OBJECT = ".storeTestObject";
	

	public static final String TOOLTIP_IDENTIFIER = "ToolTip:";
	public static final String COMMENT_CONFIG_IDENTIFIER = "#";
	public static final String SCRIPT_SUFFIX = "Script";
	public static final String SCRIPT_DIR = "scripts";
	public static final String ENV_DISPLAY_NAME = "DisplayName";

	public static final String APP_COMPOSITES = "Composites";
	public static final String ENVIRONMENT_SPECIFIC_TEST_VALUE = "<env>";
	public static final String TAGS = "Tags:";

	public static final String SYSNAT_DUMMY_TEST_RUN = "sysnat.dummy.test.run"; // for test purpose
}