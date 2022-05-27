package com.iksgmbh.sysnat.guicontrol;

import java.util.List;

public interface WebGuiControl extends GuiControl
{
	// page/window methods
	void loadPage(String url);
	void reloadCurrentPage();
	String getPageSource();
	String getCurrentUrl();
	void switchToHomeFrame();
	int getNumberOfBrowserWindows();	
	
	// xpath methods
	String getXPathForElementWithGuiText(String textPart1, String textPart2, String avoidText);
	String getXPathForElementWithGuiText(String text);
	void clickTableCellLink(String xPathToCell);
	boolean isXPathAvailable(String xPath);

	// tab methods
	void closeCurrentTab();
	boolean isTabValid(String tabId);
	boolean isErrorTab(String tabId);	
	int getNumberOfOpenTabs();

	// misc methods
	List<String> getMenuHeaders();	
	void clickDownloadButton();
	
	

}
