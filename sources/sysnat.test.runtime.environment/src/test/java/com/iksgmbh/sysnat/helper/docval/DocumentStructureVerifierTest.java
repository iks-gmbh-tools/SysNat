package com.iksgmbh.sysnat.helper.docval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.helper.docval.domain.DocumentStructure;

public class DocumentStructureVerifierTest
{
	private static final String testPFD = "../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/EBook.pdf";
	private static final String testStructureInfo = "../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/TestStructureInfo.dat";

	@Test
	public void readsStructureInfo()
	{
		// arrange
		DocumentStructureVerifier verifier = new DocumentStructureVerifier(testPFD);
		
		// act
		verifier.initStructureInfo(testStructureInfo);
		List<DocumentStructure> result = verifier.getKnownDocumentStructures();
		
		// assert
		assertNotNull(result);
		assertEquals("Number of defined document structures", 2, result.size());
		assertEquals("Number of document parts", 6, result.get(0).size());
		assertEquals("Part Name", "Sample", result.get(0).getName());
		assertEquals("Part ID", "Structure1", result.get(0).getId());
		assertEquals("Number of document parts", 11, result.get(1).size());
		assertEquals("Part Name", "CompleteBook", result.get(1).getName());
		assertEquals("Part ID", "Structure2", result.get(1).getId());
	}

	@Test
	public void verifiesStructureWithSuccess()
	{
		// act
		String result1 = DocumentStructureVerifier.doYourJob(testPFD, testStructureInfo, "Structure1");
		String result2 = DocumentStructureVerifier.doYourJob(testPFD, testStructureInfo, "Sample");
		
		// assert
		assertEquals("Result", DocumentStructureVerifier.OK_VERIFIED, result1);
		assertEquals("Result", DocumentStructureVerifier.OK_VERIFIED, result2);
	}

	@Test
	public void verifiesStructureWithFailure()
	{
		// act
		String result1 = DocumentStructureVerifier.doYourJob(testPFD, testStructureInfo, "Structure2");
		String result2 = DocumentStructureVerifier.doYourJob(testPFD, testStructureInfo, "CompleteBook");
		
		// assert
		final String expectedReport = SysNatFileUtil.readTextFileToString("../sysnat.test.runtime.environment/src/test/resources/expectedReports/" +
                        "PdfStructureDifferenceReport.txt"); 

		assertEquals("Result", expectedReport.trim(), result1.trim());
		assertEquals("Result", expectedReport.trim(), result2.trim());
	}

	@Test
	public void throwsExceptionForMissingPart()
	{
		// arrange
		final String missingPartStructureInfo = "../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/"
				+ "MissingPartStructureInfo.dat";

		try {
			// act
			DocumentStructureVerifier.doYourJob(testPFD, missingPartStructureInfo, "Structure2");
			fail("Expected exception not thrown!");
		} catch (SysNatException e) {
			// assert
			assertEquals("error message", "No document part found with name or id <b>Part3</b> in <b>../sysnat.test.runtime.environment/src/test/resources/pdfStructureTestData/"
					+ "MissingPartStructureInfo.dat</b>.", e.getMessage());
		}	
	}
	
	
}
