/*
 * Copyright 2018 IKS Gesellschaft fuer Informations- und Kommunikationssysteme mbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iksgmbh.sysnat.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import com.iksgmbh.sysnat.GenerationRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatFileUtil;
import com.iksgmbh.sysnat.domain.Filename;
import com.iksgmbh.sysnat.domain.LanguageTemplatePattern;

/**
 * Creates the file configured with property 'sysnat.help.command.list.file'
 * It contains list of all natural language instructions available for a test application.

 * @author Reik Oberrath
 */
public class CommandLibraryCreator 
{
	static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/UserMessages", Locale.getDefault());

	private static final String LIBRARY_PAGE_TEMPLATE = 
			"../sysnat.testcase.generation/src/main/resources/htmlTemplates/LanguageTemplateLibraryPage.htm.Template." 
            + Locale.getDefault().getLanguage() + ".txt";

	private HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection;
	private int containerCounter = 0;;
	
	
	
	CommandLibraryCreator(HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection) {
		this.languageTemplateCollection = languageTemplateCollection;
	}

	public static void doYourJob(HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection) 
	{
		new CommandLibraryCreator(languageTemplateCollection).createLibrary();
	}

	public void createLibrary() 
	{
		final String fileContent = buildFileContent();
		String commandLibraryFilename = System.getProperty("sysnat.help.command.list.file");
		if (commandLibraryFilename == null) {
			System.err.println("Warning: Cannot save Command Library File because its filename is not specified in the execution properties.");
		} else {
			commandLibraryFilename = commandLibraryFilename.replace("<testapp>", 
					                 GenerationRuntimeInfo.getInstance().getTestApplicationName());
			SysNatFileUtil.writeFile(commandLibraryFilename, fileContent);
		}
	}

	String buildFileContent() 
	{
		final String templateText = SysNatFileUtil.readTextFileToString(LIBRARY_PAGE_TEMPLATE);
		final StringBuffer sb = new StringBuffer();
		final Set<Filename> keySet = languageTemplateCollection.keySet();
		final List<Filename> files = new ArrayList<>(keySet);

		files.sort( new Comparator<Filename>() {
			@Override public int compare(Filename o1, Filename o2) {
				return o1.value.compareTo(o2.value);
			}
		} );
		
		files.forEach( key -> addPatternsOfContainer(key.value, languageTemplateCollection.get(key), sb) );
		
		addEmptyHtmlLine(sb);

		return templateText.replace("LANGUAGE_TEMPLATE_LIBRARY_PLACEHOLDER", sb.toString().trim());
	}

	private void addPatternsOfContainer(String filename, List<LanguageTemplatePattern> patternList, StringBuffer sb) 
	{
		containerCounter++;
		
		addEmptyHtmlLine(sb);
		
		sb.append("<span style='font-size:14.0pt'>")
		  .append(System.getProperty("line.separator"))
		  .append(BUNDLE.getString("LanguageTemplatesFrom") + filename + ": ")
		  .append(System.getProperty("line.separator"))
		  .append("</span>");

		addEmptyHtmlLine(sb);
		
		patternList.sort(new Comparator<LanguageTemplatePattern>() {
			@Override public int compare(LanguageTemplatePattern o1, LanguageTemplatePattern o2) {
				return o1.getAnnotationValue().compareTo(o2.getAnnotationValue());
			}
		});
		
		patternList.forEach(pattern -> addPattern(pattern, sb));
		
		
		if (containerCounter < languageTemplateCollection.size()) {
			addEmptyHtmlLine(sb);
			addEmptyHtmlLine(sb);
			sb.append(System.getProperty("line.separator")).append("<hr>");
		}
	}

	private void addPattern(LanguageTemplatePattern pattern, StringBuffer sb) 
	{
		addEmptyHtmlLine(sb);
		sb.append(System.getProperty("line.separator")).append(pattern.getAnnotationValue());
	}

	private void addEmptyHtmlLine(StringBuffer sb) {
		sb.append(System.getProperty("line.separator")).append("<br>");
	}

}