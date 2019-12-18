package de.iksgmbh.sysnat.docing.helper;

import java.io.File;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;

/**
 * Creates markdown file used later as source with raw information for building target document.
 * @author Reik Oberrath
 */
public class MarkdownGenerator
{
	public static final String MARKDOWN_FILE = System.getProperty("sysnat.docing.pandoc.dir") 
			                                   + "/SysNatDocing.md";

	public static File doYourJob(TestApplicationDocData xxDocData) {
		return SysNatFileUtil.writeFile(MARKDOWN_FILE, generateFileContent(xxDocData));
	}

	private static String generateFileContent(TestApplicationDocData xxDocData)
	{
		// TODO 
		return "Test";
	}

}
