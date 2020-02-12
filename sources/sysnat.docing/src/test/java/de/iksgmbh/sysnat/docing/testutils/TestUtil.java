package de.iksgmbh.sysnat.docing.testutils;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class TestUtil
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Docing", Locale.getDefault());

	public final static String TEST_DOC_DATA_PATH = "../sysnat.docing/src/test/resources/testDocData";

	public static void assertErrorMessageContains(Exception e, String expectedText)
	{
		boolean result = e.getMessage().contains(expectedText);
		assertTrue("Unexpected error message!", result);
	}
	
	public static String removeDateLine(List<String> lines)
	{
		StringBuffer sb = new StringBuffer();
		lines.stream().filter(line -> ! line.contains(BUNDLE.getString("createdOn")))
				      .forEach(line -> sb.append(line).append(System.getProperty("line.separator")));
		return sb.toString().trim();
	}
}
