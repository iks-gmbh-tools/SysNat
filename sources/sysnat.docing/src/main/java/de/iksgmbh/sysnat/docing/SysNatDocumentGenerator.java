package de.iksgmbh.sysnat.docing;

import java.io.File;
import java.io.IOException;

import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

import de.iksgmbh.sysnat.docing.domain.TestApplicationDocData;
import de.iksgmbh.sysnat.docing.helper.AppDocFileParser;
import de.iksgmbh.sysnat.docing.helper.MarkdownGenerator;
import de.iksgmbh.sysnat.docing.helper.NLXXParser;

public class SysNatDocumentGenerator
{
	public enum TargetFormat { Word_docx, html, pdf };

	public static final String APPLICATION_DOC_FILENAME = "TestApplication.sysdoc";
	
	protected TargetFormat targetFormat = getTargetFormat();
	
	private TestApplicationDocData docData;
	
	
	public void doYourJob(final File sourceFolder)
	{
		docData = parseDocData(sourceFolder);
		final File markdownFile = MarkdownGenerator.doYourJob(docData);
		
		// create document
		File docFile = convert(markdownFile);
		docFile = SysNatFileUtil.copyBinaryFile(docFile, System.getProperty("sysnat.docing.targetDir.dir"));
		
		// view it
		launchFileViewer(docFile);
	}

	protected TestApplicationDocData parseDocData(File sourceFolder)
	{
		final File applicationDocFile = new File(sourceFolder, SysNatDocumentGenerator.APPLICATION_DOC_FILENAME);
		docData = new AppDocFileParser().doYourJob(applicationDocFile);
		new NLXXParser().doYourJob(docData); // enrich docData with chatpters of behaviours
		return docData;
	}

	protected File convert(File markdownFile)
	{
		String[] arguments = { "pandocExePath",
				               "arg2" // ...
		};
		
		try {
			Runtime.getRuntime().exec( arguments );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new SysNatDocingException("Error calling Pandoc!", e);
		}
		return null;
	}

	private void launchFileViewer(File docFile)
	{
		switch (targetFormat) 
		{
		 	case Word_docx: launchWordFileViewer(docFile);
		 	                return;
		 	case html:      launchHtmlFileViewer(docFile);
                            return;
		 	case pdf:       launchPdfFileViewer(docFile);
                            return;
		}
		
		throw new SysNatDocingException("Unsupported target format: " + targetFormat.name());
	}

	private void launchWordFileViewer(File docFile)
	{
		// TODO Auto-generated method stub
		
	}

	private void launchHtmlFileViewer(File docFile)
	{
		// TODO Auto-generated method stub
		
	}

	private void launchPdfFileViewer(File docFile)
	{
		// TODO Auto-generated method stub
		
	}

	private TargetFormat getTargetFormat()
	{
		return TargetFormat.html;
	}
}
