package de.iksgmbh.sysnat.docing;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.helper.HtmlLauncher;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.ResultLaunchOption;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;
import de.iksgmbh.sysnat.docing.helper.AppDocFileParser;
import de.iksgmbh.sysnat.docing.helper.MarkdownGenerator;
import de.iksgmbh.sysnat.docing.helper.NLXXParser;

public class SysNatDocumentGenerator
{
	public static final String APPLICATION_DOC_FILENAME = "TestApplication.sysdoc";
	
	protected SysNatConstants.DocumentationFormat targetFormat;
	
	private TestApplicationDocData docData;
	
	public static void doYourJob(String applicationUnderTest) 
	{
		String dirPath = System.getProperty("sysnat.executable.examples.source.dir");
		File sourceDir = new File(dirPath, applicationUnderTest);
		new SysNatDocumentGenerator().generate(sourceDir);
	}
	
	public void generate(final File sourceFolder)
	{
		targetFormat = getTargetFormat();
		docData = parseDocData(sourceFolder);
		final File markdownFile = MarkdownGenerator.doYourJob(docData);
		
		// create document
		File docFile = convert(markdownFile);
		String targetDir = DocingRuntimeInfo.getInstance().getDocumentTargetDir();
		docFile = SysNatFileUtil.copyFileToTargetDir(docFile, targetDir);
		
		// view it
		ResultLaunchOption resultLaunchOption = DocingRuntimeInfo.getInstance().getResultLaunchOption();
		if (resultLaunchOption == ResultLaunchOption.Both || resultLaunchOption == ResultLaunchOption.Docing ) {
			launchFileViewer(docFile);
		}
	}

	protected TestApplicationDocData parseDocData(File sourceFolder)
	{
		final File applicationDocFile = new File(sourceFolder, SysNatDocumentGenerator.APPLICATION_DOC_FILENAME);
		docData = new AppDocFileParser().doYourJob(applicationDocFile);
		new NLXXParser().doYourJob(docData); // enrich docData with chapters of behaviours
		return docData;
	}

	protected File convert(File markdownFile)
	{
		String pandocDir = SysNatFileUtil.findAbsoluteFilePath(System.getProperty("sysnat.docing.pandoc.dir"));

		String format = getTargetFormat().name();
		String filename = System.setProperty(SysNatConstants.DOC_NAME_SETTING_KEY, "ExampleResultDocument");
		
		DefaultExecutor exec = new DefaultExecutor();
		exec.setWorkingDirectory(new File(pandocDir));
		CommandLine cl = CommandLine.parse(pandocDir + "/SysNatDocing.bat " + filename + " " + format);

		try {
			int exitvalue = exec.execute(cl);
			if (exitvalue != 0) {
				throw new SysNatDocingException("Unexpected error calling SysNatDocing.bat via CommandLine.");
			}
		} catch (SysNatDocingException e) {
			throw e;
		} catch (Exception e) {
			throw new SysNatDocingException(e.getMessage(), e);
		}
		
		File toReturn = new File(pandocDir, filename + "." + format);
		
		if ( ! toReturn.exists() ) {
			throw new SysNatDocingException("Markdown file has not been converted to target format.");
		}
		
		return toReturn;
	}

	private void launchFileViewer(File docFile)
	{
		if ( ! docFile.exists()) {
			throw new SysNatException("Cannot find expected Document File '" + docFile.getAbsolutePath() + "'.");
		}
		
		switch (targetFormat) 
		{
		 	case DOCX: launchWordFileViewer(docFile);
		 	           return;
		 	case HTML: launchHtmlFileViewer(docFile);
                       return;
		 	case PDF:  launchPdfFileViewer(docFile);
                       return;
		}
		
		throw new SysNatDocingException("Unsupported target format: " + targetFormat.name());
	}

	private void launchWordFileViewer(File docFile) {
		try {
			Desktop.getDesktop().open(docFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new SysNatException("Cannot open file '" + docFile.getAbsolutePath() + "'.");
		}
	}

	private void launchHtmlFileViewer(File docFile) {
		HtmlLauncher.doYourJob(docFile);
	}

	private void launchPdfFileViewer(File docFile) {
		HtmlLauncher.doYourJob(docFile);
	}

	private SysNatConstants.DocumentationFormat getTargetFormat()
	{
		String toReturn = System.getProperty(SysNatConstants.DOC_FORMAT_SETTING_KEY);
		if (toReturn == null) {
			System.err.println("No Document Format defined. Using default: HTML");
			return SysNatConstants.DocumentationFormat.HTML;
		}
		
		String property = System.getProperty(SysNatConstants.DOC_FORMAT_SETTING_KEY).toUpperCase();
		try {
			return SysNatConstants.DocumentationFormat.valueOf(property);
		} catch (Exception e) {
			System.err.println("Unknown Document Format defined (" + property + "). Using default: HTML");
			return SysNatConstants.DocumentationFormat.HTML; // default
		}
	}
}
