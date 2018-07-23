package com.iksgmbh.sysnat.helper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
	static final String CONTAINER_NAME_UNDERLINE = "-------------------------------------------------------";

	public static void doYourJob(HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection) 
	{
		final String fileContent = buildFileContent(languageTemplateCollection);
		String commandLibraryFilename = System.getProperty("sysnat.help.command.list.file");
		if (commandLibraryFilename == null) {
			System.err.println("Warning: cannor save Command Library File because its filename is not specified in the execution properties.");
		} else {
			commandLibraryFilename = commandLibraryFilename
					.replace("<testapp>", GenerationRuntimeInfo.getInstance().getTestApplicationName());
			SysNatFileUtil.writeFile(commandLibraryFilename, fileContent);
		}
	}

	static String buildFileContent(HashMap<Filename, List<LanguageTemplatePattern>> languageTemplateCollection) 
	{
		final StringBuffer sb = new StringBuffer();
		final Set<Filename> keySet = languageTemplateCollection.keySet();
		final List<Filename> keys = new ArrayList<>(keySet);
		keys.sort( new Comparator<Filename>() {
			@Override public int compare(Filename o1, Filename o2) {
				return o1.value.compareTo(o2.value);
			}
		} );
		
		keys.forEach( key -> addAllPatterns(key.value, languageTemplateCollection.get(key), sb));
		return sb.toString().trim();
	}

	private static void addAllPatterns(String filename, List<LanguageTemplatePattern> patternList, StringBuffer sb) 
	{
		sb.append(System.getProperty("line.separator"));
		sb.append("Instructions read from " + filename);
		sb.append(System.getProperty("line.separator"));
		sb.append(CONTAINER_NAME_UNDERLINE);
		sb.append(System.getProperty("line.separator"));
		
		patternList.sort(new Comparator<LanguageTemplatePattern>() {
			@Override public int compare(LanguageTemplatePattern o1, LanguageTemplatePattern o2) {
				return o1.getAnnotationValue().compareTo(o2.getAnnotationValue());
			}
		});
		
		patternList.forEach(pattern -> addPattern(pattern, sb));
		
	}

	private static void addPattern(LanguageTemplatePattern pattern, StringBuffer sb) {
		sb.append(pattern.getAnnotationValue());
		sb.append(System.getProperty("line.separator"));
	}
}
