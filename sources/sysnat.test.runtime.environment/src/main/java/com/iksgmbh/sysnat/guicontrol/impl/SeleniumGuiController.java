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
package com.iksgmbh.sysnat.guicontrol.impl;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.exception.SysNatException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants.BrowserType;
import com.iksgmbh.sysnat.common.utils.SysNatDateUtil;
import com.iksgmbh.sysnat.guicontrol.WebGuiControl;
import com.iksgmbh.sysnat.helper.BrowserStarter;

public class SeleniumGuiController extends AbstractGuiControl implements WebGuiControl 
{
	@SuppressWarnings("unused")
	private Robot robot;
	private WebDriver webDriver;
	private ExecutionRuntimeInfo executionInfo = ExecutionRuntimeInfo.getInstance();
	private String initUrl;

	private enum TAGNAME { select, input };

	@Override
	public Object getGuiHandle() {
		return webDriver;
	}
	
	@Override
	public void reloadGui() {
		goToStartPage();
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
	public boolean init(Map<String, String> startParameter) 
	{
		boolean ok = false;
		initUrl = startParameter.get("starturl");
		
		if (webDriver == null) {
			ok = openGUI();
		}
		
		if (ok) 
		{
			try {
				webDriver.get(initUrl);
			} catch (Exception e) {
				ok = false;
			}
		}
		
		if (ok) 
		{
			try {
				if (isNativeLoginDialogDisplayed()) {
					ok = true;	
				} else {
					webDriver.findElement(By.tagName("body"));
				}
			} catch (Exception e) {
				ok = false;
			}
		}
		
		return ok;
	}
	
	public boolean isNativeLoginDialogDisplayed() 
	{
		if (BrowserStarter.getCurrentBrowserType() == BrowserType.CHROME) {
			return webDriver.getTitle().equals("");
		}
		try {			
			WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofMillis(10));      
			Alert alert = wait.until(ExpectedConditions.alertIsPresent());     
			//alert.authenticateUsing(new UserAndPassword(username, password));
			//driver.switchTo().alert().sendKeys("username" + Keys.TAB + "password");
			//driver.switchTo().alert().accept();
			//((HasAuthentication) driver).register(UsernameAndPassword.of("username", "pass"));
			return alert.getText().equals("Diese Website fordert Sie auf, sich anzumelden.");
		} catch (Exception e) {
			return false;
		}
	}


	@Override
	public void closeGUI()
	{
		try {
			webDriver.close();
		} catch (Exception e) {
			// ignore exception
		}

		try {
			webDriver.quit();
		} catch (Exception e) {
			// ignore exception
		}
	}	

	@Override
	public boolean isTextFieldEnabled(String elementIndentifier) {
		return retrieveElement(elementIndentifier).isEnabled();
	}
	
	@Override
	public boolean isTextFieldEditable(String elementIndentifier) 
	{
	    try{
	    	WebElement element = retrieveElement(elementIndentifier);
	    	String currentText = element.getText();
	    	element.clear();
	    	element.sendKeys("#|t");
	    	boolean toReturn = element.getAttribute("value").equals("#|t");
	    	element.clear();
	    	element.sendKeys(currentText);
	        return toReturn;
	    } catch (Exception e) {
	        return false;
	    }		
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
	public boolean isSelected(String elementIndentifier) {
		WebElement checkbox = retrieveElement( elementIndentifier );
		String checked = checkbox.getAttribute("checked");
		return checked != null && checked.equalsIgnoreCase("true");
	}
	
	@Override
	public boolean isCheckBoxTicked(String chbId) {
	   WebElement element = retrieveElement(chbId);
	   return element.isSelected();
	}

	public String getTableCell(String tableIndentifier, int rowNumber, int columnNumber) 
	{
        return getTableCellContent(tableIndentifier, rowNumber, columnNumber).toString();
	}
	
	@Override
	public Object getTableCellContent(String tableIndentifier, int rowNumber, int columnNumber) 
	{
		int rowIndex = rowNumber-1;
		int columnIndex = columnNumber;
    	WebElement row = retrieveElement(tableIndentifier).findElements(By.xpath("*")).get(rowIndex);
        return row.findElement(By.xpath("td[" + columnIndex + "]")).getText();
	}
	    
    
	@Override
	public boolean isElementAvailable(String elementIdentifier, int timeoutMillis, boolean onlyEnabled)
	{
		String attributeName = null;
		if (elementIdentifier.contains("==")) {
			String[] splitResult = elementIdentifier.split("==");
			if (splitResult.length == 2) {
				attributeName = splitResult[0];
				elementIdentifier = splitResult[1];
			}
		}
		try {			
	        WebElement element = retrieveElement(attributeName, elementIdentifier, timeoutMillis);
	        if (onlyEnabled) {
	        	if (element == null) return false; 
	        	return element.isEnabled();
	        }
			return element != null;
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
    
	/**
	 * Checks availability by clicking the element.
	 * @param element
	 * @return
	 */
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
	
	/**
	 * Checks availability by asserting that it is displayed and enabled.
	 * @param elementIndentifier
	 * @return
	 */
	public boolean isElementReady(String elementIndentifier)
	{
		try {			
	        WebElement element = retrieveElement( elementIndentifier );
			return isElementReady(element);
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	@Override
	public File takeScreenShot() {
		return ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
	}	
	
	@Override
	public String getSelectedTabName() {
		return retrieveElement("tab_navigation").findElement(By.className("selected")).getText();
	}
	
	@Override
	public int getNumberOfColumnsInTable(String tableClass) {
		List<WebElement> tableHeaders = webDriver.findElements(By.xpath("//table[@class='" + tableClass + "']/thead/tr/th"));
		return tableHeaders.size();
	}
	
	@Override
	public int getNumberOfRowsInTable(int i) {
		throw new RuntimeException("Not yet implemented.");
	}

	@Override
	public int getNumberOfRowsInTable(String tableClass) {
		List<WebElement> rows = webDriver.findElement(By.id(tableClass)).findElements(By.xpath("*"));
		return rows.size();
		
		// or better that way?
//    	WebElement table = retrieveElement(tableName);
//        List<WebElement> rows = table.findElements(By.tagName("tr"));
//        return rows != null ? rows.size() : 0;
		
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
	public int getNumberOfLinesInTextArea(String xpath) {
        final String s = getTextForXPath(xpath);
        return s.split("\\n").length;
	}
	
	@Override
	public void waitUntilEnabledElementIsAvailable(String elementIdentifier) 
	{
		Date startTime = new Date();
		long minimumMillisToWait = 15000;
		WebElement element = retrieveElement(elementIdentifier, 1000);

		while (true) 
		{			
			if (element.isDisplayed() && element.isEnabled()) {
				return;
			}
			if (minimumMillisToWait > SysNatDateUtil.getDiffInMillis(startTime, new Date())) {
				throw new SysNatException("Element <b>" + elementIdentifier + "<b> has not come available in " + (minimumMillisToWait/1000) + " seconds.");
			}
		}
		
		/*
		 This implementation above does not work. The following do also not work:
		 WebElement element = retrieveElement(elementIdentifier, 1000);
		 WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofMillis(15000));
	     wait.until(ExpectedConditions.elementToBeClickable(element));
	     wait.until(ExpectedConditions.visibilityOf(element));
		 */
	
	}

	@Override
	public void waitUntilEnabledElementIsAvailable(String elementIdentifier, int timeoutInSeconds) {
		retrieveElement(elementIdentifier, timeoutInSeconds * 1000);
	}

	public void waitUntilElementIsAvailable(String attributeName, String elementIdentifier, int timeoutInSeconds) {
		retrieveElement(attributeName, elementIdentifier, timeoutInSeconds * 1000);
	}
	
	public boolean isElementAvailable(String attributeName, String elementIdentifier)
	{
		return ! findMatchingElements(attributeName, elementIdentifier).isEmpty();
	}

	@Override
	public String getCurrentlyActiveWindowTitle() {
		return webDriver.getTitle();
	}

	@Override
	public List<WebElement> getElements(final String elementIdentifier) {
		return retrieveElement("body").findElements(By.xpath(elementIdentifier));
	}	


	@Override
	public List<String> getTableHeaders(String tableIndentifier)
	{
		// TODO Auto-generated method stub
		return null;
	}

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
		for (WebElement we: list) {
			if (we.isDisplayed()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean waitToDisappear(String text, int maxSecondsToWait)
	{
		try {
			long startTimeInMillis = new Date().getTime();

			while (true)
			{
				sleep(executionInfo.getMillisToWaitForAvailabilityCheck());

				if ( ! isTextCurrentlyDisplayed(text) ) {
					System.out.println("###########");
					System.out.println("waitToDisappear cancelled by text not available - " + text);
					System.out.println("###########");
					return true;
				}

				long nowInMillis = new Date().getTime();
				if (nowInMillis - startTimeInMillis > executionInfo.getDefaultGuiElementTimeout()) {
					System.out.println("###########");
					System.out.println("waitToDisappear cancelled by Timeout - " + text);
					System.out.println("###########");
					return false;
				}
			}
		}
		catch (Exception e) {
			System.out.println("###########");
			System.out.println("waitToDisappear cancelled by Exception - " + text);
			System.out.println("###########");
			return true;
		}
	}


	@Override
	public int getNumberOfOpenApplicationWindows() {
		return webDriver.getWindowHandles().size();
	}
	
	// ####################################################################################################
	//                         GUI MANIPULATING METHODS 
	// ####################################################################################################

	@Override
	public void switchToWindow(String windowID) {
		webDriver.switchTo().window(windowID);
	}

	@Override
	public void switchToFirstWindow() {
		switchToWindow(getFirstWindowHandle());
	}

	@Override
	public void maximizeWindow() {
		webDriver.manage().window().maximize();
		//webDriver.manage().window().fullscreen(); diff to maximize ?
	}

	@Override
	public void minimizeWindow() {
		webDriver.manage().window().setPosition(new org.openqa.selenium.Point(-2000, 0));  // workaround
	}

	@Override
	public void switchToLastWindow() {
		switchToWindow(getLastWindowHandle());
	}
		
	@Override
	public void enterTextInTextField(String text, String elementIndentifier) 
	{
		Date startTime = new Date();
        inputText( retrieveElement(elementIndentifier), text);
        checkExecutionSpeed(startTime);
	}
	
	@Override
    public void selectComboboxEntry(String elementIdentifier, String value)  
	{
		Date startTime = new Date();
        Select dropdown = new Select(retrieveElement(elementIdentifier));
        try {			
        	dropdown.selectByValue(value); // first try value
		} catch (NoSuchElementException e) {
			dropdown.selectByVisibleText( value );  // second try visible text
		}
        checkExecutionSpeed(startTime);
    }
	
	@Override
    public void selectComboboxEntry(String elementIdentifier, int index)  
	{
		Date startTime = new Date();
        Select dropdown = new Select(retrieveElement(elementIdentifier));
        dropdown.selectByIndex(index);
        checkExecutionSpeed(startTime);
    }	
    
	@Override
	public void inputEmail(String fieldName, String emailAdress) 
	{
		Date startTime = new Date();
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
        checkExecutionSpeed(startTime);
	}

	@Override
	public void clickRadioButton(String elementIdentifier, int position) 
	{
		Date startTime = new Date();
		webDriver.findElements(By.name( elementIdentifier )).get( position - 1 ).click();
        checkExecutionSpeed(startTime);
	}
	


	@Override
    public boolean clickMenuItem(final String elementAsString) 
	{
		Date startTime = new Date();
		String result = clickElement(elementAsString, 1000);
        checkExecutionSpeed(startTime);
		return result == null;
    }
	
	
	@Override
    public void clickElement(final String tableName, final String tableEntryText) 
	{
		Date startTime = new Date();
		List<WebElement> findElements = webDriver.findElements(By.className(tableName));
		for (WebElement webElement : findElements) {
			if (webElement.getText().contains(tableEntryText) || webElement.getText().equals(tableEntryText))  {
				webElement.click();
				break;
			}
		}
        checkExecutionSpeed(startTime);
	}
	
	
	@Override
	public void clickTableCell(String tableIdentifier, int rowNo, int columnNo) 
	{
		Date startTime = new Date();
		int rowIndex = rowNo -1;
    	WebElement row = retrieveElement("searchResultsTable_" + rowIndex);
        row.findElement(By.xpath("td[" + columnNo + "]")).click();
        checkExecutionSpeed(startTime);
	}
	

	@Override
	public void clickTableRow(String tableIdentifier, int rowNo) {
		clickTableCell(tableIdentifier, rowNo, 1);
	}
	

	@Override
	public void doubleClickTableCell(String tableIdentifier, int rowNo, int columnNo) 
	{
		Date startTime = new Date();
		int rowIndex = rowNo -1;
    	WebElement row = retrieveElement("searchResultsTable_" + rowIndex);
        WebElement element = row.findElement(By.xpath("td[" + columnNo + "]"));
        element.click();
        element.click();  // TODO does this really work ???
        checkExecutionSpeed(startTime);
	}

	@Override
    public String clickElement(final String elementAsString, int timeoutInMillis)
    {
        WebElement element = retrieveElement(elementAsString, timeoutInMillis);
        return clickElement(element, elementAsString);
    }

	@Override
	public void clickButton(String guiTextOrTechnicalId) {
		clickElement(guiTextOrTechnicalId, 1000);
	}

	@Override
    public String clickElement(final String elementAsString)
    {
        WebElement element = retrieveElement(elementAsString);
        return clickElement(element, elementAsString);
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
	public void clickTableCellLink(String xPathToCell) 
	{
		Date startTime = new Date();
		WebElement retrieveElement = retrieveElement(xPathToCell);
		String attribute = retrieveElement.getAttribute("href");
		if (attribute == null) {
			throw new RuntimeException("Cell contains no clickable link.");
		}
		retrieveElement.click();
        checkExecutionSpeed(startTime);
	}
	
    private String clickElement(final WebElement element, String elementAsString)
    {
    	boolean goOn = true;
		Date startTime = new Date();
		long startTimeInMillis = startTime.getTime();
		
		while (goOn)
		{
			sleep(executionInfo.getMillisToWaitForAvailabilityCheck());
		
			try {
				element.click();

				return null;
			} catch (Exception e) {
				if (element != null) {
					((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true);", element);
					Actions builder = new Actions(webDriver);
					builder.moveToElement(element).perform();
				}
			}
			
			long nowInMillis = new Date().getTime();
			if (nowInMillis - startTimeInMillis > executionInfo.getDefaultGuiElementTimeout()) {
				goOn = false;
			}
		}
        
        checkExecutionSpeed(startTime);
		return "Das Element <b>" + elementAsString + "</b> wurde gefunden, konnte aber nicht geklickt werden. (Offenbar unsichtbar oder deaktiviert.)";
    }

	

	@Override
	public boolean assureTickInCheckBox(String chbId) 
	{
		Date startTime = new Date();
		boolean ok = false;
	    if ( ! isCheckBoxTicked(chbId) ) {
	      retrieveElement(chbId).click();
	      ok = true;
	    }
        checkExecutionSpeed(startTime);
	    return ok;
	}

	@Override
	public boolean assureNoTickInCheckBox(String chbId) 
	{
		Date startTime = new Date();
		boolean ok = false;
	    if ( isCheckBoxTicked(chbId) ) {
	      retrieveElement(chbId).click();
	      ok = true;
	    }
        checkExecutionSpeed(startTime);
	    return ok;
	}


	@Override
	public void clickRowInTable(String elementIndentifier, int rowNo) {
		throw new RuntimeException("Not yet implemented.");
	}

	@Override
	public void clickRowInTable(int index, int rowNo) {
		throw new RuntimeException("Not yet implemented.");
	}

	@Override
    public void clickLink(String id) {
		clickLink(id, id, 1);
	}

	@Override
    public void clickLink(String idToClick, String idToScrollIntoView) {
		clickLink(idToClick, idToScrollIntoView, 1);
	}

	@Override
    public void clickLink(String idToClick, int positionOfOccurrence) {
		clickLink(idToClick, idToClick, positionOfOccurrence);
	}

	@Override
    public void clickLink(String idToClick, String idToScrollIntoView, int positionOfOccurrence) 
	{
		Date startTime = new Date();
		if (positionOfOccurrence < 1) {
			throw new SysNatException("Programmer problem: positionOfOccurrence must not be less than 1.");
		}
		
		List<WebElement> matchingElements = findMatchingElements(idToClick);
		
		if (matchingElements.size() == 0) {
			throw new SysNatException("Link <b>" + idToClick + "</b> has not been found on current page.");
		}

		if (positionOfOccurrence > matchingElements.size()) {
			throw new SysNatException("So many occurences (" + positionOfOccurrence + ") of " + idToClick + " has not been found on current page.");
		}

		int index = positionOfOccurrence-1;
		WebElement element = matchingElements.get(index);

//    	if (webDriver instanceof InternetExplorerDriver) 
//    	{
//			JavascriptExecutor js = (JavascriptExecutor) webDriver;
//			js.executeScript("return arguments[0].click();", element);
//    		
//    		//getRobot().keyPress(KeyEvent.VK_ENTER);
//    		return;
//    	}
//	
		
    	try {
			WebElement elementToScrollIntoView = element;
			if ( ! idToScrollIntoView.equals(idToClick) ) {
				elementToScrollIntoView = retrieveElement(idToScrollIntoView);
			}
			JavascriptExecutor js = (JavascriptExecutor) webDriver;
			js.executeScript("arguments[0].scrollIntoView();", elementToScrollIntoView );

			if (webDriver instanceof InternetExplorerDriver) 
			{
				js.executeScript("return arguments[0].click();", element);
			} else {
				element.click();
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
//		
//    	WebElement element = retrieveElement(id);
//    	
//    	try {
//    		Actions act=new Actions(webDriver);
//    		act.moveToElement(element).click().perform();
//    		return;
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    	}
//    	
//    	try {
//    		element.click();
//    		return;
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    	}
//
//    	try {
//    		element.sendKeys(Keys.RETURN);
//    		return;
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    	}
//    	
//    	if (webDriver instanceof InternetExplorerDriver) {
//    		getRobot().keyPress(KeyEvent.VK_ENTER);
//	    }
    	
        checkExecutionSpeed(startTime);
	}
	
	// ####################################################################################################
	//                        Selenium specific methods
	// ####################################################################################################


	@Override
	public void loadPage(String url)
	{
		try {
			if (url !=null) webDriver.get(url);
		} catch (InvalidArgumentException e) {
			throw new SysNatException("Die URL <b>" + url + "</b> ist keine gültige Webadresse.");
		}
	}

	@Override
	public void reloadCurrentPage()
	{
		try {
			webDriver.navigate().refresh();
		} catch (Exception e) {
			System.err.println("Error reloading current page. Presumably the endpoint is not available.");
		}
	}
	
	public void goToStartPage()
	{
		try {
			webDriver.navigate().to(initUrl);
		} catch (Exception e) {
			System.err.println("Error reloading current page. Presumably the endpoint is not available.");
		}
	}

	@Override
	public String getPageSource() {
		return webDriver.getPageSource();
	}
	
	@Override
	public String getCurrentUrl() {
		return webDriver.getCurrentUrl();
	}	
	
	@Override
	public void switchToHomeFrame() {
		webDriver.switchTo().defaultContent();
	}
	
	@Override
	public String getXPathForElementWithGuiText(String textPart1, String textPart2, String avoidText)
	{
		List<WebElement> matches1 = webDriver.findElements(By.xpath("//*[contains(text(),'" + textPart1 + "')]"));
		List<WebElement> matches2 = webDriver.findElements(By.xpath("//*[contains(text(),'" + textPart2 + "')]"));
		for (WebElement match : matches1) {
			if ( matches2.contains(match)) {
			    if ( ! match.getText().contains(avoidText)) {
                    return generateXPATH(match);
                }
			}
		}
		return "<not found>";
	}
	
	@Override
	public String getXPathForElementWithGuiText(String text)
	{
		List<WebElement> toAvoid = webDriver.findElements(By.xpath("//*[contains(normalize-space(text()),'" + text + ":" + "')]"));
		List<WebElement> matches = webDriver.findElements(By.xpath("//*[contains(normalize-space(text()),'" + text + "')]"));
		List<WebElement> list = new ArrayList<>();
		for (WebElement match : matches) {
			if ( ! toAvoid.contains(match)) {
				list.add(match);
			}
		}
		if (list.size() > 1) {
			throw new RuntimeException("Text " + text + " is nicht eindeutig!");
		};

		if (list.size() == 0) {
			return "<not found>";
		}
		return generateXPATH(list.get(0));
	}

	@Override
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
	
	@Override
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
	
	@Override
	public int getNumberOfBrowserWindows() {
		return webDriver.getWindowHandles().size();
	}
	
	@Override
	public List<String> getMenuHeaders() 
	{
		final List<String> toReturn = new ArrayList<String>();
		List<WebElement> findElements = webDriver.findElements(By.className("menuheader"));
		for (WebElement webElement : findElements) {
			toReturn.add(webElement.getText());
		}
		return toReturn;
	}
	
	@Override
	public void clickDownloadButton() 
	{
		clickElement("download");
//		WebElement printLink= webDriver.findElements(By.linkText("Print")).get(0);
//		JavascriptExecutor js= (JavascriptExecutor) webDriver;
//		js.executeScript("arguments[0].setAttribute(arguments[1],arguments[2])",printLink,"download","");	
	}

	@Override
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
		
	@Override
	public int getNumberOfOpenTabs() {
		// TODO does this work with selenium 3.8
		return webDriver.getWindowHandles().size();
	}
	
	@Override
	public boolean isXPathAvailable(String xPath) {
		final List<WebElement> elements = webDriver.findElements(By.xpath(xPath));
		if (elements.size() == 0) return false;
		return elements.get(0).isDisplayed() && elements.get(0).isEnabled();
	}
	

	@Override
	public void waitForDialogToClose(String dialogTitle)
	{
		// TODO Auto-generated method stub
	}
	

	// ####################################################################################################
	//                             P r i v a t e   M e t h o d s
	// ####################################################################################################
	
	private WebElement retrieveElement(final String elementIdentifier)  {
		return retrieveElement(elementIdentifier, executionInfo.getDefaultGuiElementTimeout() * 1000);
	}


	/**
	 * 
	 * @param elementIdentifier
	 * @param timeOutInMillis
	 * @return
	 */
	private WebElement retrieveElement(String elementIdentifier, int timeOutInMillis)
	{
		String attributeName = null;
		if (elementIdentifier.contains("==")) {
			String[] splitResult = elementIdentifier.split("==");
			if (splitResult.length == 2) {
				attributeName = splitResult[0];
				elementIdentifier = splitResult[1];
			}
		}
		return retrieveElement(attributeName, elementIdentifier, timeOutInMillis);
	}

	
	private WebElement retrieveElement(final String attributeName, final String elementIdentifier, int timeOutInMillis) 
	{
		String toSearchFor = elementIdentifier;
		String toSearchForExtension = "";

		if (elementIdentifier.contains("::"))
		{
			String[] splitResult = elementIdentifier.split("::");
			toSearchFor = splitResult[0];
			toSearchForExtension = splitResult[1];
		}

		List<WebElement> matches = null;
		boolean goOn = true;
		long startTimeInMillis = new Date().getTime();
		
		while (goOn)
		{
			sleep(executionInfo.getMillisToWaitForAvailabilityCheck());
			
			if (attributeName == null) {
				matches = findMatchingElements(toSearchFor);
			} else {
				matches = findMatchingElements(attributeName, toSearchFor);
			}
			
			if (matches.size() > 0 ) {
				goOn = false;
			}

			long nowInMillis = new Date().getTime();
			if (nowInMillis - startTimeInMillis > timeOutInMillis) {
				goOn = false;
			}
		}
		
		if (matches.size() == 0) {
			throw new NoSuchElementException("Das Element mit der technischen Identifizierung <b>"
					+ elementIdentifier +
					"</b> konnte auf der aktuellen Seite nicht gefunden werden.");
		}


		if (matches.size() == 1) {
			return matches.get(0);
		}

		for (WebElement element : matches) {
			if (element.getAttribute("id").equals(toSearchFor)
				||
				element.getAttribute("id").equals(toSearchForExtension))
			{
				return element;
			}
		}

		throw new NoSuchElementException("Das Element mit der technischen Identifizierung "
				+ elementIdentifier +
				" ist nicht eindeutig identifizierbar.");
	}
	
	private List<WebElement> findMatchingElements(final String attributeName, final String elementIdentifier)
	{
        List<WebElement> toReturn = new ArrayList<>();
        
		try {
			toReturn = webDriver.findElements(By.xpath("//img[@" + attributeName + "=\"" + elementIdentifier + "\"]"));
        } catch (Exception e)  {
            // ignore
        }

		if (toReturn.size() == 0) {			
			try {
				toReturn = webDriver.findElements(By.xpath("//div[@" + attributeName + "=\"" + elementIdentifier + "\"]"));
			} catch (Exception e)  {
				// ignore
			}
		}

		if (toReturn.size() == 0) {			
			try {
				toReturn = webDriver.findElements(By.xpath("//input[@" + attributeName + "=\"" + elementIdentifier + "\"]"));
			} catch (Exception e)  {
				// ignore
			}
		}

		if (toReturn.size() == 0) {			
			try {
				toReturn = webDriver.findElements(By.xpath("//span[@" + attributeName + "=\"" + elementIdentifier + "\"]"));
			} catch (Exception e)  {
				// ignore
			}
		}
		
		return toReturn;
	}
	
	private List<WebElement> findMatchingElements(final String elementIdentifier)
	{
        List<WebElement> toReturn = new ArrayList<>();
        
		try {
			toReturn = webDriver.findElements(By.id(elementIdentifier));
        } catch (Exception e)  {
            // ignore
        }

		if (toReturn.size() == 0)
		{
			try {
				toReturn = webDriver.findElements(By.name(elementIdentifier));
			} catch (Exception e) {
				// ignore
			}
		}

		if (toReturn.size() == 0)
		{			
			try {
				toReturn = webDriver.findElements(By.xpath(elementIdentifier));
			} catch (Exception e)  {
				// ignore
			}			
		}

		if (toReturn.size() == 0) {
			try {
				toReturn = webDriver.findElements(By.tagName(elementIdentifier));
			} catch (Exception e)  {
				// ignore
			}
		}
		
		if (toReturn.size() == 0) {
			try {
				toReturn = webDriver.findElements(By.className(elementIdentifier));
			} catch (Exception e)  {
				// ignore
			}
		}

		if (toReturn.size() == 0) {
			try {
				toReturn = webDriver.findElements(By.linkText(elementIdentifier));
			} catch (Exception e)  {
				// ignore
			}
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

	/**
	 * Checks availability by asserting that it is displayed and enabled.
	 * @param element
	 * @return
	 */
	public boolean isElementReady(WebElement element)
	{
		return  element != null
				&& element.isDisplayed()
				&& element.isEnabled();
	}

	public boolean isElementReadyToUse(WebElement element)
	{
		return  element != null
				&& element.isDisplayed()
				&& element.isEnabled()
				&& isClickable(element);
	}

	private boolean isClickable(WebElement element)
	{
		try {
			element.click();
			return true;
		} catch (Exception e) {
			return false;
		}
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

	private String generateXPATH(WebElement element) {
		return generateXPATH(element, "");
	}

	private String generateXPATH(WebElement childElement, String current)
	{
		String childTag = childElement.getTagName();
		if(childTag.equals("html")) {
			return "/html[1]"+current;
		}
		WebElement parentElement = childElement.findElement(By.xpath(".."));
		List<WebElement> childrenElements = parentElement.findElements(By.xpath("*"));
		int count = 0;
		for(int i=0;i<childrenElements.size(); i++) {
			WebElement childrenElement = childrenElements.get(i);
			String childrenElementTag = childrenElement.getTagName();
			if(childTag.equals(childrenElementTag)) {
				count++;
			}
			if(childElement.equals(childrenElement)) {
				return generateXPATH(parentElement, "/" + childTag + "[" + count + "]"+current);
			}
		}
		return null;
	}

	@Override
	public void windowToFront()
	{
		// TODO 
	}

	@Override
	public void enterDateInDateField(String value, String elementIndentifier)
	{
		// TODO 
	}

	@Override
	public boolean isDialogAvailable(String dialogTitle)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCheckBoxTicked(int checkBoxIndex)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean assureTickInCheckBox(int checkBoxIndex)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean assureNoTickInCheckBox(int checkBoxIndex)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void selectAllRowsInTable(String tableId)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectAllRowsInTable(int index)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTextInDialogTextField(String dialogName, String elementIdentifier, String text)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clickDialogButton(String dialogTitle, String guiTextOrTechnicalId)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTextInDialogTextField(String dialogTitle, int index, String text)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterTextInTextField(int index, String value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doubleClickTableCell(int index, int rowNo, int columnNo)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clickRadioButton(String elementIndentifier)
	{
		List<WebElement> result = findMatchingElements(elementIndentifier);
		if (result == null || result.size() != 1) {
	    	throw new RuntimeException("No or no unique element with identifier '" + elementIndentifier + "' found.");
		}
		
		result.get(0).click();
		
        int attempts = 1;
        while (! isSelected(elementIndentifier)) {  //das klappt im Edge noch nicht richtig!
        	if (attempts == 50) {
        		throw new SysNatException("Could not click radiobutton " + elementIndentifier);
        	}	
        	attempts++;
        	sleep(250);
        	result.get(0).click();
        }
		
	}

	@Override
	public void clickRadioButton(int index)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllText(String elementIndentifier)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void editTableCell(String tableIndentifier, int rowNumber, int columnNumber, Object value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void editTableCell(int index, int rowNumber, int columnNumber, Object value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFocusTo(String elementIdentifier)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputTextInTextArea(String value, String areaID)
	{
		List<WebElement> result = findMatchingElements(areaID);
		if (result == null || result.size() != 1) {
	    	throw new RuntimeException("No or no unique element with identifier '" + areaID + "' found.");
		}
		result.get(0).sendKeys(value);
	}

	@Override
	public List<Integer> searchRowsInTable(String tableIdentifier, String searchCriteria)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void selectRowInTable(String tableIdentifier, int row)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void selectRowInDialogTable(String dialogTitle, int row)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Integer> searchRowsInDialogTable(String dialogTitle, String searchCriteria)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clickDialogRadioButton(String dialogTitle, String elementIdentifier)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enterDateInDialogDateField(String dialogTitle, String value, String elementIdentifier)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllText(String dialogTitle, String elementIdentifier)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void expandKnotInTree(String treeName, String knotName)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean containsTreeElement(String treeName, String elementName)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Point getCoordinates(String elementIdentifier)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle getTableCellRectangle(String tableIdentifier, int rowNo, int columnNo)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clickDialogButton(String dialogTitle, int index)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clickTab(String tabPanelIdentifier, int tabIndex)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void waitUntilDialogElementIsAvailable(String dialogName, String elementIdentifier, int timeoutInSeconds)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Integer> searchRowsInTable(int index, String searchCriteria)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getTableCellContent(int index, int rowNumber, int columnNumber)
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void resetMainFrameHandle()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clickButton(int index)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clickButton(int index, String buttonName)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDialogAvailable(String dialogName, String message)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enterTextInDialogTextArea(String dialogTitle, String elementIdentifier, String text)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAllText(int index)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDialogCheckBoxTicked(String dialogName, String technicalId)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean assureTickInDialogCheckBox(String dialogName, String technicalId)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean assureTickInDialogCheckBox(String dialogName, int checkBoxIndex)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean assureNoTickInDialogCheckBox(String dialogName, String technicalId)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean assureNoTickInDialogCheckBox(String dialogName, int checkBoxIndex)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDialogCheckBoxTicked(String dialogName, int checkBoxIndex)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitForDialogToAppear(String dialogTitle, int timeoutInMillis)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitFileChooserIsVisible(int timeoutInMillis)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFilePathInFileChooser(String filePath)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectComboboxEntry(int fieldIndex, int index)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectComboboxEntry(int fieldIndex, String value)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String checkForAnyDialog(int timeoutInMillis)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void waitUntilElementIsAvailable(String elementIdentifier)
	{
		long startTimestamp = new Date().getTime();
		while ( ! isElementReady(elementIdentifier)) {
			long now = new Date().getTime();
			if ((now-startTimestamp) > 30000) {
				throw new SysNatException("GUI element was not available within 30 secs: " + elementIdentifier);
			}
		}
	}

}