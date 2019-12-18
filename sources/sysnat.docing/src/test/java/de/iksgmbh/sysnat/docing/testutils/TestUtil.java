package de.iksgmbh.sysnat.docing.testutils;

import static org.junit.Assert.assertTrue;

public class TestUtil
{
	public final static String TEST_DOC_DATA_PATH = "../sysnat.docing/src/test/resources/testDocData";

	public static void assertErrorMessageContains(Exception e, String expectedText)
	{
		boolean result = e.getMessage().contains(expectedText);
		assertTrue("Unexpected error message!", result);
	}
	

}
