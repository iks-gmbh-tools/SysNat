package com.iksgmbh.sysnat.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class SysNatLocaleConstants 
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Constants", Locale.getDefault());
	
	public static final String PICTURE_PROOF = BUNDLE.getString("PICTURE_PROOF");
	public static final String CATEGORY_BILDNACHWEIS = BUNDLE.getString("with") + PICTURE_PROOF;
	public static final String ERROR_KEYWORD = BUNDLE.getString("Error");	
	public static final String TECHNICAL_ERROR_TEXT = BUNDLE.getString("TECHNICAL_ERROR_TEXT");
	public static final String ASSERT_ERROR_TEXT = BUNDLE.getString("ASSERT_ERROR_TEXT");
	public static final String ARRANGE_KEYWORD = BUNDLE.getString("ARRANGE_KEYWORD");
	public static final String ACT_KEYWORD = BUNDLE.getString("ACT_KEYWORD");
	public static final String ASSERT_KEYWORD = BUNDLE.getString("ASSERT_KEYWORD");
	public static final String CLEANUP_KEYWORD = BUNDLE.getString("CLEANUP_KEYWORD");
	public static final String YES_KEYWORD = BUNDLE.getString("Yes") + ".";
	public static final String NO_KEYWORD = BUNDLE.getString("No") + "!";
	public static final String FROM_FILENAME = BUNDLE.getString("FROM_FILENAME");
	public static final String FROM_PACKAGE = BUNDLE.getString("FROM_PACKAGE");
	public static final String NON_KEYWORD = BUNDLE.getString("NON") + "-";
}
