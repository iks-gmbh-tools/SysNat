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
package com.iksgmbh.sysnat.guicontrol;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.helper.BrowserStarter;

public class SeleniumGuiController implements GuiControl 
{
	private WebDriver webDriver;
	private ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
	private enum TAGNAME { select, input };
	
	@Override
	public void reloadCurrentPage() {
		webDriver.navigate().refresh();
	}

	@Override
	public boolean openGUI() 
	{
		try {
			webDriver = BrowserStarter.doYourJob();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean init(String targetLoginUrl) 
	{
		boolean ok = false;
		
		if (webDriver == null) {
			ok = openGUI();
		}
		
		if (ok) 
		{
			try {
				webDriver.get(targetLoginUrl);				
			} catch (Exception e) {
				e.printStackTrace();
				ok = false;
			}
		}
		
		return ok;
	}

	@Override
	public void closeGUI() {
		webDriver.quit();
	}
	
	@Override
	public void insertText(String text, String elementIndentifier) {
        inputText( retrieveElement(elementIndentifier), text);
	}

	@Override
	public String getText(String elementIdentifier) 
	{
		final WebElement element = retrieveElement(elementIdentifier);
		
		if (TAGNAME.select.name().equals(element.getTagName())) {
			Select dropdown = new Select(element);
			return dropdown.getAllSelectedOptions().get(0).getText();
		}
		
		if (TAGNAME.input.name().equals(element.getTagName())) {
			return element.getAttribute("value");
		}
		
		return element.getText();
	}
	
	@Override
    public void clickLink(String id) {
    	retrieveElement(id).sendKeys(Keys.RETURN);
    }
	

	@Override
	public boolean isSelected(String elementIndentifier) {
		WebElement checkbox = retrieveElement( elementIndentifier );
		String checked = checkbox.getAttribute("checked");
		return checked != null && checked.equalsIgnoreCase("true");
	}

	@Override
	public void selectRow(String elementIndentifier, int rowNo) {
		throw new RuntimeException("Not yet implemented.");
	}

	@Override
	public String getTableCell(String tableIndentifier, int rowNumber, int columnNumber) 
	{
		int rowIndex = rowNumber-1;
		int columnIndex = columnNumber;
    	WebElement row = retrieveElement(tableIndentifier).findElements(By.xpath("*")).get(rowIndex);
        return row.findElement(By.xpath("td[" + columnIndex + "]")).getText();
	}
	
	@Override
	public void clickTableCell(String tableIdentifier, int rowNo, int columnNo) 
	{
		int rowIndex = rowNo -1;
    	WebElement row = retrieveElement("searchResultsTable_" + rowIndex);
        row.findElement(By.xpath("td[" + columnNo + "]")).click();
	}
	

	@Override
    public String clickElement(final String elementAsString, int timeoutInSeconds)
    {
        WebElement element = retrieveElement(elementAsString, timeoutInSeconds);
        return clickElement(element, elementAsString);
    }

	@Override
    public void clickElement(final String tableName, final String tableEntryText) 
	{
		List<WebElement> findElements = webDriver.findElements(By.className(tableName));
		for (WebElement webElement : findElements) {
			if (webElement.getText().contains(tableEntryText) || webElement.getText().equals(tableEntryText))  {
				webElement.click();
				return;
			}
		}
	}

	@Override
    public String clickElement(final String elementAsString)
    {
        WebElement element = retrieveElement(elementAsString);
        return clickElement(element, elementAsString);
    }
    
    
	@Override
    public boolean isElementAvailable(String elementId) 
	{
		try {			
	        WebElement element = retrieveElement(elementId, 1);
			return element != null;
		} catch (NoSuchElementException e) {
			return false;
		}
    }
    
	@Override
	public boolean isElementReadyToUse(String elementIndentifier) 
	{
		try {			
	        WebElement element = retrieveElement( elementIndentifier );
			return isElementReadyToUse(element);
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	@Override
	public File takeScreenShot() {
		return ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
	}

	@Override
	public void clickTab(String tabIdentifier, String tabName) 
	{
		if (! tabName.equals(getSelectedTabName())) {
			WebElement element = retrieveElement(tabIdentifier);
			clickElement(element, tabName);
		}
	}
	
	@Override
	public String getSelectedTabName() {
		return retrieveElement("tab_navigation").findElement(By.className("selected")).getText();
	}
	
	@Override
	public int getNumberOfColumns(String tableClass) {
		List<WebElement> tableHeaders = webDriver.findElements(By.xpath("//table[@class='" + tableClass + "']/thead/tr/th"));
		return tableHeaders.size();
	}
	
	@Override
	public int getNumberOfRows(String tableClass) {
		List<WebElement> rows = webDriver.findElement(By.id(tableClass)).findElements(By.xpath("*"));
		return rows.size();
		
		// or better that way?
//    	WebElement table = retrieveElement(tableName);
//        List<WebElement> rows = table.findElements(By.tagName("tr"));
//        return rows != null ? rows.size() : 0;
		
	}
	

	@Override
	public void selectRadioButton(String elementIdentifier, int position) {
		webDriver.findElements(By.name( elementIdentifier )).get( position - 1 ).click();
	}
	
	@Override
    public void selectComboboxEntry(String elementIdentifier, int index)  {
        Select dropdown = new Select(retrieveElement(elementIdentifier));
        dropdown.selectByIndex(index);
    }
	

	@Override
	public String getSelectedComboBoxEntry(String elementIdentifier) 
	{
        final List<WebElement> options = retrieveElement(elementIdentifier).findElements(By.xpath("option"));
        for (WebElement webElement : options) {
			if (webElement.getAttribute("selected") != null) {
				return webElement.getText();
			}
		}
        return "";
	}
	

	@Override
    public boolean isEntryInComboboxDropdownAvailable(String elementIdentifier, String value)  
	{
        List<WebElement> elements = retrieveElement(elementIdentifier).findElements(By.tagName("option"));
        for (WebElement webElement : elements) {
			boolean contains = webElement.getAttribute("value").equals(value);
			if (contains) {
				return true;
			}
		}
        return false;
    }	
	
	@Override
    public void selectComboboxEntry(String elementIdentifier, String value)  
	{
        Select dropdown = new Select(retrieveElement(elementIdentifier));
        try {			
        	dropdown.selectByValue(value); // first try value
		} catch (NoSuchElementException e) {
			dropdown.selectByVisibleText( value );  // second try visible text
		}
    }
    
	@Override
	public void inputEmail(String fieldName, String emailAdress) 
	{
        WebElement e = retrieveElement( fieldName );
        inputText(e, emailAdress);
        String text = e.getAttribute("value");
        int pos = emailAdress.indexOf('@');

        if (pos > -1 && ! text.equals(emailAdress))
        {
            // This implementation is a special solution for automated Selenium IE tests:
            // In Selenium tests the sendKeys method fails for the IE to enter a @ (at) symbol which is displayed as q.
            e.clear();
            e.sendKeys(emailAdress.substring(0, pos));
            Actions action = new Actions(webDriver);
            action.keyDown(Keys.CONTROL).keyDown(Keys.ALT).sendKeys(String.valueOf('\u0040')).keyUp(Keys.CONTROL).keyUp(Keys.ALT).perform();
            e.sendKeys(emailAdress.substring(pos+1));
        }
	}

	@Override
	public int getNumberOfLinesInTextArea(String xpath) {
        final String s = getTextForXPath(xpath);
        return s.split("\\n").length;
	}
	
	@Override
	public void waitUntilElementIsAvailable(String elementIdentifier) {
		retrieveElement(elementIdentifier, 60);
	}


	@Override
	public String getCurrentlyActiveWindowTitle() {
		return webDriver.getTitle();
	}

	@Override
	public void switchToLastWindow() {
		switchToWindow(getLastWindowHandle());
	}
	
	@Override
	public List<WebElement> getElements(final String elementIdentifier) {
		return retrieveElement("body").findElements(By.xpath(elementIdentifier));
	}	
		
	@Override
	public String getTableCellText(String tableClassName, int tableNo, int columnNo, int rowNo) 
	{
		int tableIndex = tableNo -1;
		List<WebElement> elements = getElements("//table[@class='" + tableClassName + "']");
		return elements.get(tableIndex).findElement(By.xpath("tbody/tr[" + rowNo + "]/td[" + columnNo + "]")).getText();
	}

	@Override
	public String getTagName(String elementIdentifier) {
		return retrieveElement(elementIdentifier).getTagName();
	}

	@Override
	public boolean isTextCurrentlyDisplayed(String text) 
	{
		List<WebElement> list = webDriver.findElements(By.xpath("//*[contains(normalize-space(text()),'" + text + "')]"));
		return list.size() > 0;
	}

	@Override
	public WebDriver getWebDriver() {
		return webDriver;
	}
	
	@Override
	public void switchToFirstWindow() {
		switchToWindow(getFirstWindowHandle());
	}
	
	
	// ####################################################################################################
	//                          Public but No Interface Methods
	//                        (Selenium specific accesses to GUI)
	// ####################################################################################################


	public String getPageSource() {
		return webDriver.getPageSource();
	}
	
	public String getCurrentUrl() {
		return webDriver.getCurrentUrl();
	}	
	
	public boolean isTabValid(String tabId) 
	{
		List<WebElement> findElements = retrieveElement("tab_navigation").findElements(By.className("valid"));
		for (WebElement webElement : findElements) {
			String elementName = webElement.getText();
			if ( elementName.equals(tabId) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isErrorTab(String tabId) 
	{
		List<WebElement> findElements = retrieveElement("tab_navigation").findElements(By.className("error"));
		for (WebElement webElement : findElements) {
			String elementName = webElement.getText();
			if ( elementName.equals(tabId) ) {
				return true;
			}
		}
		return false;
	}
	
	public int getNumberOfBrowserWindows() {
		return webDriver.getWindowHandles().size();
	}
	
	public List<String> getMenuHeaders() 
	{
		final List<String> toReturn = new ArrayList<String>();
		List<WebElement> findElements = webDriver.findElements(By.className("menuheader"));
		for (WebElement webElement : findElements) {
			toReturn.add(webElement.getText());
		}
		return toReturn;
	}
	

	public void clickDownloadButton() 
	{
		clickElement("download");
//		WebElement printLink= webDriver.findElements(By.linkText("Print")).get(0);
//		JavascriptExecutor js= (JavascriptExecutor) webDriver;
//		js.executeScript("arguments[0].setAttribute(arguments[1],arguments[2])",printLink,"download","");	
	}

	public void closeCurrentTab() 
	{
		String firstWindowHandle = getFirstWindowHandle();
		try {
			new Actions(webDriver).keyDown(Keys.CONTROL).sendKeys("w").keyUp(Keys.CONTROL).perform();
		} catch (Exception e) {
			// ignore
		}
		webDriver.switchTo().window(firstWindowHandle);
	}
	
	public void switchToWindow(String windowID) {
		webDriver.switchTo().window(windowID);
	}
	
	public int getNumberOfOpenTabs() {
		return webDriver.getWindowHandles().size();
	}

	public void clickTableCellLink(String xPathToCell) 
	{
		WebElement retrieveElement = retrieveElement(xPathToCell, 0);
		String attribute = retrieveElement.getAttribute("href");
		if (attribute == null) {
			throw new RuntimeException("Cell contains no clickable link.");
		}
		retrieveElement.click();
	}
	
	public boolean isXPathAvailable(String xPath) {
		final List<WebElement> elements = webDriver.findElements(By.xpath(xPath));
		if (elements.size() == 0) return false;
		return isElementReadyToUse(elements.get(0));
	}

	// ####################################################################################################
	//                             P r i v a t e   M e t h o d s
	// ####################################################################################################
	
	private WebElement retrieveElement(final String elementIdentifier)  
	{
		return retrieveElement(elementIdentifier, executionInfo.getDefaultGuiElementTimeout());
	}	

	private WebElement retrieveElement(final String elementIdentifier, int timeOutInSeconds)  
	{
        WebElement toReturn = null;
		boolean goOn = true;
		int counterWaitState = 0;
		int maxAttemptsToFindElement = 1000 * timeOutInSeconds / executionInfo.getMilliesForWaitState(); 
		
		while (goOn)
		{
			sleep(executionInfo.getMilliesForWaitState());
			
			toReturn = findElement(elementIdentifier);
			if (toReturn != null) {
				goOn = false;
			}
			counterWaitState++;
			
			if (counterWaitState > maxAttemptsToFindElement) {
				goOn = false;
			}
		}

		if (toReturn == null) {
			throw new NoSuchElementException("Das Element mit der technischen Identifizierung " 
		                                      + elementIdentifier + 
		                                      " konnte auf der aktuellen Seite nicht gefunden werden.");
		}

		return toReturn;
	}
	
	
	private WebElement findElement(final String elementIdentifier) 
	{
        WebElement toReturn = null;
        
		try {
			toReturn = webDriver.findElement(By.id(elementIdentifier));
        } catch (Exception e)  {
            // ignore
        }

		if (toReturn == null) 
		{
			try {
				toReturn = webDriver.findElement(By.name(elementIdentifier));
			} catch (Exception e) {
				// ignore
			}
		}
		
		if (toReturn == null) 
		{			
			try {
				toReturn = webDriver.findElement(By.xpath(elementIdentifier));
			} catch (Exception e)  {
				// ignore
			}			
		}
		
		if (toReturn == null) {
			try {
				toReturn = webDriver.findElement(By.tagName(elementIdentifier));
			} catch (Exception e)  {
				// ignore
			}
		}
		
		if (toReturn == null) {
			try {
				toReturn = webDriver.findElement(By.className(elementIdentifier));
			} catch (Exception e)  {
				// ignore
			}
		}
		
		try {
			toReturn = webDriver.findElement(By.linkText(elementIdentifier));
        } catch (Exception e)  {
            // ignore
        }
		
		return toReturn;
	}

    private void sleep(int millis) 
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

	private boolean isElementReadyToUse(WebElement element) {
		return element != null && element.isDisplayed() && element.isEnabled();
	}

    private void inputText(WebElement e, String value)
    {
        e.click();
        e.clear();
        e.sendKeys(value);
    }

    private String getTextForXPath(String xpath)  {
        return retrieveElement(xpath).getText();
    }


    private String clickElement(final WebElement element, String elementAsString)
    {
    	boolean goOn = true;
		int counterWaitState = 0;
		int maxAttemptsToFindElement = executionInfo.getMilliesForWaitState() / executionInfo.getMilliesForWaitState(); 
		
		while (goOn)
		{
			sleep(executionInfo.getMilliesForWaitState());
		
			try {
				element.click();
				return null;
			} catch (Exception e) {
				if (element != null) {
//					((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
//					Actions builder = new Actions(webDriver);
//					builder.moveToElement(element).perform();
				}
			}
			
			counterWaitState ++;
			
			if (counterWaitState > maxAttemptsToFindElement) {
				goOn = false;
			}
		}
        
		return "Das Element <b>" + elementAsString + "</b> wurde gefunden, konnte aber nicht geklickt werden. (Offenbar unsichtbar oder deaktiviert.)";
    }

    private String getLastWindowHandle() 
    {
    	Set<String> windowHandles = webDriver.getWindowHandles();
    	String toReturn = null;
    	for (String handle : windowHandles) {
    		toReturn = handle;
		}
    	
    	return toReturn;    	
    }

    private String getFirstWindowHandle() 
    {
    	Set<String> windowHandles = webDriver.getWindowHandles();
    	for (String handle : windowHandles) {
    		return handle;
		}
    	throw new RuntimeException("Now Window Handle found.");
    }

    
}