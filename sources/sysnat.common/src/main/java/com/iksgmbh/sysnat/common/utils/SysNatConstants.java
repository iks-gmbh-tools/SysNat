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
	
	/**
	 * The Double Colon is used to specify placeholders for test data values.
	 */
	public static final String DC = "::";
	
	public static final String ROOT_PATH_PLACEHOLDER = "<root.path>";

	public enum TestPhase {PRECONDITION, ARRANGE, ACT, ASSERT, CLEANUP, 
		                   GIVEN, WHEN, THEN, 
		                   VORBEREITUNG, DURCHFÜHRUNG, ÜBERPRÜFUNG};

	public enum BrowserType { CHROME, IE, FIREFOX, FIREFOX_45_9 };

	public enum WebLoginParameter { URL, LOGINID, PASSWORD };
	
	public enum GuiType { Button, 
		                  TextField,   // single line input field
		                  ComboBox,    // selectbox with dropdown
		                  RadioButtonSelection,   
		                  ElementToReadText,
		                  CheckBox};
	
	public enum TargetEnv { LOCAL,        // IDE of deceloper
		                    DEVELOPMENT,  // Developer team environment used by CI
		                    INTEGRATION,  // Integration environment with communication with other systems
		                    ACCEPTANCE,   // Environment for final acceptance by domain experts
		                    PRODUCTION};  // Environment for end user (Going-Live-Environment)
	
	public static final String REPORT_FILENAME_TEMPLATE = "TestReport TIME_PLACEHOLDER.html";
	public static final String NO_FILTER = "-";
	public static final String COMMENT_IDENTIFIER = "//";
	public static final String QUESTION_IDENTIFIER = "? - ";
		
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
	

	public static final String METHOD_CALL_IDENTIFIER_TEST_PARAMETER_DEFINITION = ".applyTestParameter(";
	public static final String METHOD_CALL_IDENTIFIER_BEHAVIOUR_DECLARATION = ".declareXXGroupForBehaviour(";
	public static final String METHOD_CALL_IDENTIFIER_BDD_KEYWORD_USAGE = ".setBddKeyword(";
	public static final String METHOD_CALL_IDENTIFIER_START_XX = ".startNewXX(";
	public static final String METHOD_CALL_IDENTIFIER_FILTER_DEFINITION = ".defineAndCheckExecutionFilter(";

	
}