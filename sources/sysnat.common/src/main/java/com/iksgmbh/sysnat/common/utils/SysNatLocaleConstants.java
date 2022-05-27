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
package com.iksgmbh.sysnat.common.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public class SysNatLocaleConstants 
{
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bundles/Constants", Locale.getDefault());
	private static final ResourceBundle BUNDLE_EN = ResourceBundle.getBundle("bundles/Constants", Locale.ENGLISH);
	
	public static final String PICTURE_PROOF = BUNDLE.getString("PICTURE_PROOF");
	public static final String CATEGORY_BILDNACHWEIS = BUNDLE.getString("with") + PICTURE_PROOF;
	public static final String ERROR_KEYWORD = BUNDLE.getString("Error");	
	public static final String PROBLEM_KEYWORD = BUNDLE.getString("Problem");	
	public static final String TECHNICAL_ERROR_TEXT = BUNDLE.getString("TECHNICAL_ERROR_TEXT");
	public static final String ASSERT_ERROR_TEXT = BUNDLE.getString("ASSERT_ERROR_TEXT");
	public static final String ARRANGE_KEYWORD = BUNDLE.getString("ARRANGE_KEYWORD");
	public static final String ACT_KEYWORD = BUNDLE.getString("ACT_KEYWORD");
	public static final String ASSERT_KEYWORD = BUNDLE.getString("ASSERT_KEYWORD");
	public static final String CLEANUP_KEYWORD = BUNDLE.getString("CLEANUP_KEYWORD");
	public static final String YES_KEYWORD = BUNDLE.getString("Yes") + ".";
	public static final String NO_KEYWORD = BUNDLE.getString("No") + "!";
	public static final String NON_KEYWORD = BUNDLE.getString("NON") + "-";
	public static final String AND_KEYWORD = BUNDLE.getString("And");
	public static final String AND_KEYWORD_EN = BUNDLE_EN.getString("And");

	public static final String SCENARIO_KEYWORD = BUNDLE.getString("SCENARIO");
	public static final String SCENARIO_KEYWORD_EN = BUNDLE_EN.getString("SCENARIO");
	public static final String BEHAVIOUR_KEYWORD = BUNDLE.getString("BEHAVIOUR");
	public static final String BEHAVIOUR_KEYWORD_EN = BUNDLE_EN.getString("BEHAVIOUR");
	public static final String FEATURE_KEYWORD = BUNDLE.getString("FEATURE");
	public static final String FEATURE_KEYWORD_EN = BUNDLE_EN.getString("FEATURE");
	public static final String NAME_KEYWORD = BUNDLE.getString("NAME");
	public static final String NAME_KEYWORD_EN = BUNDLE_EN.getString("NAME");
	public static final String CHAPTER_KEYWORD = BUNDLE.getString("CHAPTER");
	public static final String CHAPTER_KEYWORD_EN = BUNDLE_EN.getString("CHAPTER");

	public static final String PLACEHOLDER_FILENAME = BUNDLE.getString("PLACEHOLDER_FILENAME");
	public static final String PLACEHOLDER_FILENAME_EN = BUNDLE_EN.getString("PLACEHOLDER_FILENAME");
	public static final String PLACEHOLDER_FOLDERNAME = BUNDLE.getString("PLACEHOLDER_FOLDERNAME");
	public static final String PLACEHOLDER_FOLDERNAME_EN = BUNDLE_EN.getString("PLACEHOLDER_FOLDERNAME");
	public static final String PLACEHOLDER_PACKAGE = BUNDLE.getString("PLACEHOLDER_PACKAGE");
	public static final String PLACEHOLDER_PACKAGE_EN = BUNDLE_EN.getString("PLACEHOLDER_PACKAGE");

	public static final String POSSIBLE_VALUE_IDENTIFIER = BUNDLE.getString("POSSIBLE_VALUE_IDENTIFIER");
}