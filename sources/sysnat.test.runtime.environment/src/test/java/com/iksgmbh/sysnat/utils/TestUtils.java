package com.iksgmbh.sysnat.utils;

import java.util.ArrayList;
import java.util.List;

import com.iksgmbh.sysnat.common.utils.SysNatStringUtil;

/**
 * Utility used only for test purpose.
 * 
 * @author Reik Oberrath
 */
public class TestUtils {

	public static String cutMainPath(String multiLineInput) {
		List<String> splitResult = SysNatStringUtil.split(multiLineInput, System.getProperty("line.separator"));
		List<String> toReturn = new ArrayList<>();
		splitResult.forEach(line -> toReturn.add(checkLineToCutPath(":", "sources", line)));
		return SysNatStringUtil.listToString(toReturn, System.getProperty("line.separator"));
	}


	private static String checkLineToCutPath(String cutStart, String cutEnd, String line) 
	{
		int pos1 = line.indexOf(cutStart);
		int pos2 = line.indexOf(cutEnd);
		if (pos1 > -1 && pos2 > -1 && pos2 > pos1) {
			line = line.substring(0,pos1) + cutStart + "<pathCut>" + line.substring(pos2);
		}
		return line;
	}

}
