package com.iksgmbh.sysnat.helper.generator;

import static org.junit.Assert.assertEquals;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.iksgmbh.sysnat.common.utils.SysNatConstants.ApplicationType;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;

public class IdMappingSnippetDevGenClassLevelTest
{
	@Before
	public  void init()
	{
		IdMappingSnippetDevGen.isFirstGuiType = true;
	}

	// ####################################   W E B  #####################################

	// Button tests
	
	@Test
	public void parsesWebButton_withoutAsButton() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard("<input type=\"button\" id=\"buttonId\" name=\"techinalName\" value=\"Save\">");
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"Save\", createList(\"buttonId\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.Button, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	@Test
	public void parsesWebButton_withoutAsSubmit() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard("<input type=\"submit\" id=\"buttonId\" name=\"techinalName\" value=\"Save\">");
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"Save\", createList(\"buttonId\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.Button, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}
	
	// Radiobutton tests
	
	@Test
	public void parsesWebRadioButton_withoutBoth() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard("<input type=\"radio\" name=\"cityField\" maxlength=\"50\">");
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field with code name: cityField\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.RadioButtonSelection, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	@Test
	public void parsesWebRadioButton_onlyWithDisplayName() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard("<label for=\"cityField\">City<span class=\"asterisk\" id=\"requiredStar_required_parties[0].city\"></label>" + 
		            "<input type=\"radio\" name=\"cityField\" maxlength=\"50\">");
		
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"City\", createList(\"No Technical Id found for field with display name: City\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.RadioButtonSelection, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	@Test
	public void parsesWebRadiobutton_withBoth() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard("<label for=\"cityField\">City<span class=\"asterisk\" id=\"labelId\"></label>" + 
	                 "<input type=\"radio\" name=\"cityField\" maxlength=\"50\" id=\"cityID\">");

		//toClipboard(SysNatFileUtil.readTextFileToString(new File("../sysnat.natural.language.executable.examples/src/test/resources/IdMappingSnippets/radiobuttonHtmlInput.txt")));

		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"City\", createList(\"cityID\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.RadioButtonSelection, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	@Test
	public void parsesWebRadiobutton_withBoth2() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard("<input type=\"radio\" name=\"cityField\" id=\"cityID\">"
 				  + "<label class=\"label\" for=\"cityID\">City</label>");

		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"City\", createList(\"cityID\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.RadioButtonSelection, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}
	
	// Textfield tests
	
	@Test
	public void parsesWebTextfields_withoutBoth() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard("<input type=\"text\" name=\"cityField\" maxlength=\"50\">");
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field with code name: cityField\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.TextField, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	@Test
	public void parsesWebTextfields_onlyWithDisplayName() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard("<label for=\"cityField\">City<span class=\"asterisk\" id=\"requiredStar_required_parties[0].city\"></label>" + 
		            "<input type=\"text\" name=\"cityField\" maxlength=\"50\">");
		
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"City\", createList(\"No Technical Id found for field with display name: City\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.TextField, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	

	@Test
	public void parsesWebTextfields_withBoth() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard("<label for=\"cityField\">City<span class=\"asterisk\" id=\"labelId\"></label>" + 
	                 "<input type=\"text\" name=\"cityField\" maxlength=\"50\" id=\"cityID\">");
		
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"City\", createList(\"cityID\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.TextField, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	@Test
	public void parsesHtmlComboboxes_WithTechnicalIdAndWithDisplayName() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard("<label for=\"fieldCountry\">Country<span class=\"asterisk\" id=\"labelId\"></label>" + 
				    "<select name=\"fieldCountry\"  id=\"CountryID\">");
		
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"Country\", createList(\"CountryID\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.ComboBox, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}
	
	// other fields
	
	@Test
	public void parsesWebExampleFile() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Web;
		toClipboard(SysNatFileUtil.readTextFileToString(new File("../sysnat.natural.language.executable.examples/src/test/resources/IdMappingSnippets/inputHtml.txt")));
		
		String expected = SysNatFileUtil.readTextFileToString(new File("../sysnat.natural.language.executable.examples/src/test/resources/IdMappingSnippets/inputHtmlExpected.txt"));
		assertEquals("ID Mapping", expected.trim() , IdMappingSnippetDevGen.doYourJob().trim());
	}

	
	
	// ####################################   S W I N G  #####################################
	
	@Test
	public void parsesSwingTextfields_withoutBoth() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Swing;
		toClipboard("private JTextField txtTestReportName, txtTestReportArchiveDir;");
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: txtTestReportName\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: txtTestReportArchiveDir\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.TextField, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	@Test
	public void parsesSwingTextfields_onlyWithDisplayName() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Swing;
		toClipboard("private JTextField txtTestReportName, txtTestReportArchiveDir;"
				  + "lblTestReportName = new Label();"
				  + "lblTestReportName.setText(\"Report Name:\");"
				  + "lblTestReportArchiveDir = new Label(\"Archive Directory:\");");
		
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"Report Name:\", createList(\"No Technical Id found for field: txtTestReportName\"));\r\n" + 
				                   "        idMappings.put(\"Archive Directory:\", createList(\"No Technical Id found for field: txtTestReportArchiveDir\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.TextField, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	@Test
	public void parsesSwingTextfields_onlyWithTechnicalId() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Swing;
		toClipboard("private JTextField txtTestReportName, txtTestReportArchiveDir;"
				  + "txtTestReportName.setName(\"TestReportNameId\");"
				  + "txtTestReportArchiveDir.setName(\"TestReportArchiveDirId\");");
		
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"TestReportNameId\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"TestReportArchiveDirId\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.TextField, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}
	
	@Test
	public void parsesSwingTextfields_WithBoth() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Swing;
		toClipboard("private JTextField txtTestReportName;"
				  + "txtTestReportName.setName(\"TestReportNameId\");"
				  + "lblTestReportName = new Label();"
				  + "lblTestReportName.setText(\"Report Name:\");");

		
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"Report Name:\", createList(\"TestReportNameId\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.TextField, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	
	@Test
	public void parsesSwingComboboxes() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Swing;
		toClipboard("private JComboBox<String> cbTestReportName, cbxTestReportArchiveDir;");
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: cbTestReportName\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: cbxTestReportArchiveDir\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.ComboBox, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}

	
	@Test
	public void parsesSwingComboboxes_WithTechnicalIdAndWithDisplayName() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Swing;
		toClipboard("private JComboBox<String> cbxTestReportName, cbxTestReportArchiveDir;"
				  + "lblTestReportName = new Label();"
				  + "lblTestReportName.setText(\"Report Name:\");"
				  + "lblTestReportArchiveDir = new Label(\"Archive Directory:\");"
				  + "cbxTestReportName.setName(\"TestReportNameId\");"
				  + "cbxTestReportArchiveDir.setName(\"TestReportArchiveDirId\");");
		
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"Report Name:\", createList(\"TestReportNameId\"));\r\n" + 
				                   "        idMappings.put(\"Archive Directory:\", createList(\"TestReportArchiveDirId\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.ComboBox, idMappings);", 
				                   IdMappingSnippetDevGen.doYourJob());
	}
	

	@Test
	public void parsesSwingExampleFile() throws Exception
	{
		IdMappingSnippetDevGen.defaultCodeType = ApplicationType.Swing;
		toClipboard(SysNatFileUtil.readTextFileToString(new File("../sysnat.natural.language.executable.examples/src/test/resources/IdMappingSnippets/inputSwingCode.txt")));
		
		assertEquals("ID Mapping", "        HashMap<String, List<String>> idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: txtPageObjectName\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: txtEventTriggerName_forth\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: txtEventTriggerName_back\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: txtPageObjectTo_forth\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: txtPageObjectFrom_back\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: txtPageObjectWait_forth\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: txtPageObjectWait_back\"));\r\n" + 
				                   "        idMappingCollection.put(GuiType.TextField, idMappings);\r\n" + 
				                   "\r\n" + 
				                   "        idMappings = new HashMap<String, List<String>>();\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: cbxTestApplication\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: cbxChangeEventType_forth\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: cbxChangeEventType_back\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: cbxPageObjectFrom_forth\"));\r\n" + 
				                   "        idMappings.put(\"?\", createList(\"No Technical Id found for field: cbxPageObjectTo_back\"));\r\n" +
				                   "        idMappingCollection.put(GuiType.ComboBox, idMappings);", 
				               	   IdMappingSnippetDevGen.doYourJob());
	}
	
	
	private void toClipboard(String s) 
	{
		StringSelection selection = new StringSelection(s);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
	}

}
