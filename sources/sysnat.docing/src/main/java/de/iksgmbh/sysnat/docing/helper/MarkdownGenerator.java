package de.iksgmbh.sysnat.docing.helper;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.DocumentationFormat;
import com.iksgmbh.sysnat.common.utils.SysNatDateUtil;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

import de.iksgmbh.sysnat.docing.DocingRuntimeInfo;
import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;

/**
 * Creates markdown file used later as source with raw information for building target document.
 * @author Reik Oberrath
 */
public class MarkdownGenerator
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Docing", Locale.getDefault());

	public static final String MARKDOWN_FILE = System.getProperty("sysnat.docing.pandoc.dir") 
			                                   + "/SysNatDocing.md";
	public static final String STYLE_FILE = "../sysnat.docing/src/main/resources/mardown-style-info.txt";

	
	public static File doYourJob(TestApplicationDocData xxDocData) {
		return SysNatFileUtil.writeFile(MARKDOWN_FILE, generateFileContent(xxDocData));
	}

	static String generateFileContent(TestApplicationDocData xxDocData)
	{
		StringBuffer sb = new StringBuffer();
		
		SysNatFileUtil.readTextFile(STYLE_FILE).forEach(line -> sb.append(line).append(System.getProperty("line.separator"))); 
		
		addContentHeaderLines(sb, xxDocData.getTestApplicationName());

		xxDocData.getChapterNames().forEach(chapterName -> sb.append(xxDocData.getChapterContent(chapterName)));
		
		return sb.toString().trim();
	}

	private static void addContentHeaderLines(StringBuffer sb,
			                                  String testApplicationName)
	{
		String s = "";
		if (DocingRuntimeInfo.getInstance().getDocFormat() == DocumentationFormat.PDF) {
			s = " " + BUNDLE.getString("and") + " *Prince*";
		}
		
		appendLine(sb, "|   |");
		appendLine(sb, "|:-:|");
		appendLine(sb, "|**" + BUNDLE.getString("SystemDescription") + "**|");
		appendLine(sb, "|" + BUNDLE.getString("for") + "|");
		appendLine(sb, "|***" + testApplicationName + "***|");
		appendLine(sb, "|" + BUNDLE.getString("withHelpFrom") + " *Pandoc* " + s + "|");
		appendLine(sb, "|" + BUNDLE.getString("createdOn") + " " + SysNatDateUtil.getNowDateString() 
		                   + " " + BUNDLE.getString("at") + " " + SysNatDateUtil.getNowTimeString() + "|");

//		appendLine(sb, "***");
//		appendLine(sb, "<h1><center>" + BUNDLE.getString("SystemDescription") + "</center></h1>");
//		appendLine(sb, "<center>**" + BUNDLE.getString("for") + "**</center>");
//		appendLine(sb, "<h1><center>" + testApplicationName + "</center></h1>");
//		appendLine(sb, "<center>" + BUNDLE.getString("withHelpFrom") + " *Pandoc* " + s + "</center>");
//		appendLine(sb, "<center>" + BUNDLE.getString("createdOn") + " " + SysNatDateUtil.getNowDateString() 
//		                + " " + BUNDLE.getString("at") + " " 
//				        + SysNatDateUtil.getNowTimeString() + "</center>");
//		appendLine(sb, "***");
	}

	private static void appendLine(StringBuffer sb, String line) {
		sb.append(line).append(System.getProperty("line.separator"));
	}

}
