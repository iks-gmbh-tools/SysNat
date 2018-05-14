package com.iksgmbh.sysnat.utils;

public class SysNatConstants 
{
	public static final String SYS_NAT_VERSION = "1.0";
	
	public enum BrowserType { CHROME, IE, FIREFOX };
	
	public enum GuiType { Button, 
		                  TextField,   // single line input field
		                  ComboBox,    // selectbox with dropdown
		                  ElementToReadText};
	
	public enum TargetEnv { LOCAL,        // IDE of deceloper
		                    DEVELOPMENT,  // Developer team environment used by CI
		                    INTEGRATION,  // Integration environment with communication with other systems
		                    ACCEPTANCE,   // Environment for final acceptance by domain experts
		                    PRODUCTION};  // Environment for end user (Going-Live-Environment)
		                    
	public enum AppUnderTest {IksOnline, HelloWorldSpringBoot};
	
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
	
	public static final String SYS_OUT_SEPARATOR = "--------------------------------------------------------------------";

}
