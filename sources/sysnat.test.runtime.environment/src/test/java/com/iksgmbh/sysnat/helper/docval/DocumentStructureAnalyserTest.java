package com.iksgmbh.sysnat.helper.docval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentPart;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentStructure;

public class DocumentStructureAnalyserTest
{
	private static final String testPFD = "../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/EBook.pdf";
	private static final String testStructureInfo = "../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/TestStructureInfo.dat";

	@Test
	public void readsStructureInfo()
	{
		// arrange
		DocumentStructureAnalyser analyser = new DocumentStructureAnalyser(testPFD);
		
		// act
		analyser.initStructureInfo(testStructureInfo);
		List<DocumentPart> result = analyser.getKnownDocumentParts();
		
		// assert
		assertNotNull(result);
		assertEquals("Number of parts", 11, result.size());
		assertEquals("Number of pages", 3, result.get(0).getNumberOfPages());
		assertEquals("Number of pages", 1, result.get(1).getNumberOfPages());
		assertEquals("Number of pages", 2, result.get(2).getNumberOfPages());
		assertEquals("Number of pages", 3, result.get(3).getNumberOfPages());
		assertEquals("Number of pages", 1, result.get(4).getNumberOfPages());
		assertEquals("Number of pages", 1, result.get(5).getNumberOfPages());
		assertEquals("Number of pages", 1, result.get(6).getNumberOfPages());
		assertEquals("Number of pages", 1, result.get(7).getNumberOfPages());
		assertEquals("Number of pages", 1, result.get(8).getNumberOfPages());
		assertEquals("Number of pages", 3, result.get(9).getNumberOfPages());
		assertEquals("Number of pages", 2, result.get(10).getNumberOfPages());
	}
	
	
	@Test
	public void buildsDocumentStructureFromPDF()
	{
		// act
		DocumentStructure result = DocumentStructureAnalyser.doYourJob(testPFD, testStructureInfo);
		
		// assert
		assertNotNull(result);
		assertEquals("Number of parts", 6, result.getNumberOfParts());
		List<String> partIds = result.getOrderedPartIds();
		
		assertEquals("Part ID", "Part1", partIds.get(0));
		assertEquals("Part Name", "Intro", result.getPartName(partIds.get(0)));
		assertEquals("Number of Pages", 3, result.getNumberOfPages(partIds.get(0)));
		
		assertEquals("Part ID", "Part2", partIds.get(1));
		assertEquals("Part Name", "Contents", result.getPartName(partIds.get(1)));
		assertEquals("Number of Pages", 1, result.getNumberOfPages(partIds.get(1)));
		
		assertEquals("Part ID", "Part3", partIds.get(2));
		assertEquals("Part Name", "Preface", result.getPartName(partIds.get(2)));
		assertEquals("Number of Pages", 2, result.getNumberOfPages(partIds.get(2)));
		
		assertEquals("Part ID", "Part4", partIds.get(3));
		assertEquals("Part Name", "1. How to define the term 'definition'?", result.getPartName(partIds.get(3)));
		assertEquals("Number of Pages", 3, result.getNumberOfPages(partIds.get(3)));
		
		assertEquals("Part ID", "Part10", partIds.get(4));
		assertEquals("Part Name", "7. How to detect life?", result.getPartName(partIds.get(4)));
		assertEquals("Number of Pages", 3, result.getNumberOfPages(partIds.get(4)));
		
		assertEquals("Part Name", "Part11", partIds.get(5));
		assertEquals("Part Name", "Postface", result.getPartName(partIds.get(5)));
		assertEquals("Number of Pages", 2, result.getNumberOfPages(partIds.get(5)));
	}

	@Test
	public void buildsDocumentStructureWithUnknownPages()
	{
		// arrange
		String incompleteTestStructureInfo = "../sysnat.test.runtime.environment/src/test/resources/"
				                            + "pdfStructureTestData/IncompleteTestStructureInfo.dat";
		
		// act
		DocumentStructure result = DocumentStructureAnalyser.doYourJob(testPFD, incompleteTestStructureInfo);
		
		// assert
		final String expectedStructureText = SysNatFileUtil.readTextFileToString("../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/" +
                "PdfStructureWithUnknownParts.txt"); 

		assertEquals("PDF structure", expectedStructureText, result.toString());
	}
	
	@Test
	public void throwsExceptionForExcelFile()
	{
		// arrange
		final String missingPartStructureInfo = "../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/"
				+ "MissingExcelFileStructureInfo.dat";

		try {
			// act
			DocumentStructureVerifier.doYourJob(testPFD, missingPartStructureInfo, "Structure2");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Cannot read non-existing file 'C:\\SysNat\\sources\\sysnat.test.runtime.environment\\..\\sysnat.test.runtime.environment\\src\\test\\resources\\pdfStructureTestData\\"
					+ "MissingExcelFileStructureInfo.dat'.",
					e.getMessage());
		}	
	}

	@Test
	public void throwsExceptionForExcelFileDefinition()
	{
		// arrange
		final String missingPartStructureInfo = "../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/"
				+ "MissingExcelFileDefinitionStructureInfo.dat";

		try {
			// act
			DocumentStructureVerifier.doYourJob(testPFD, missingPartStructureInfo, "Structure2");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "Error reading property in <b>C:\\SysNat\\sources\\sysnat.test.runtime.environment\\..\\sysnat.test.runtime.environment\\src\\test\\resources\\pdfStructureTestData\\"
					+ "MissingExcelFileDefinitionStructureInfo.dat</b>, line 1 \"<b>This file has intentionally no meaningful content!\"<b>.",
					e.getMessage());
		}	
	}
	
}
