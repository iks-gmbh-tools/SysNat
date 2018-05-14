package com.iksgmbh.sysnat.language_templates;

import java.util.List;

/**
 * Contains methods to be implemented in the LanguageTemplate file that is specific to the TestApplication.
 * but must be available in common code.
 * 
 * @author Reik Oberrath
 */
public interface LanguageTemplates
{
	public void doLogin(String... loginData);
	
	public void doLogout();

	/**
	 * Returns true if the page is visible
	 * that is displayed after successfull login
	 */
	public boolean isOverviewPageVisible();

	boolean isLoginPageVisible();
	
	/**
	 * Used to reset the GUI after each test in order to assure that the next test
	 * will start from same initial point which the page displayed after login.
	 * To do this, popups may be closed that remain evenually open.
	 */
	public void gotoStartPage();
	
	public List<String> getScriptDirectories();

}
