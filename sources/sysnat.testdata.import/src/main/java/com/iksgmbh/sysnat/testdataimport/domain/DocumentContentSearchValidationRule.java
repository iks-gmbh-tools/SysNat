package com.iksgmbh.sysnat.testdataimport.domain;

/**
 * The content of a document may be validated by one of more DocumentValidationRules.
 * To validate a document its content is searched for a specific text that is expected
 * 
 * a) either anywhere in the document
 * b) on a specific page
 * c) in a specific line on a specific page
 * 
 * As a variant of b), the page is not defined as PageNumber but by a PageIdentifier text.
 * For this relative text search, the first page is taken where the PageIdentifier is found
 * and on this page the actual search text has to be found to validate the document.
 * 
 * @author Reik Oberrath
 */
public class DocumentContentSearchValidationRule implements DocumentValidationRule
{
	public enum ContentRuleType { Contains,               // search in whole document
		                          ContainsOnPage,         // absolute search on page with page number
		                          ContainsInLine,         // absolute search on page with page and line number
		                          ContainsOnRelativePage, // relative page search 
		                          ContainsInRelativeLine  // relative page search with line number
		                        };
	
	private ContentRuleType type;

	/**
	 * For absolute text searches
	 */
	private int lineNumber = -1;
	private int pageNumber = -1;
	
	/**
	 * For relative text searches
	 */
	private String pageIdentifier;

	/**
	 * Text to be found to validate a document successfuly.
	 */
	private String expectedContent;
	
	
	// ###############  Constructors #################
	
	public DocumentContentSearchValidationRule(String aContent, int aPageNumber, int aLineNumber)
	{
		type = ContentRuleType.ContainsInLine;
		this.expectedContent = aContent;
		this.pageNumber = aPageNumber;
		this.lineNumber = aLineNumber;
	}
	
	public DocumentContentSearchValidationRule(String aContent, int aPageNumber)
	{
		type = ContentRuleType.ContainsOnPage;
		this.expectedContent = aContent;
		this.pageNumber = aPageNumber;
	}
	
	public DocumentContentSearchValidationRule(String aContent)
	{
		type = ContentRuleType.Contains;
		this.expectedContent = aContent;
	}

	public DocumentContentSearchValidationRule(String aContent, String aPageIdentifier)
	{
		type = ContentRuleType.ContainsOnRelativePage;
		this.expectedContent = aContent;
		this.pageIdentifier = aPageIdentifier;
	}

	public DocumentContentSearchValidationRule(String aContent, String aPageIdentifier, int aLineNumber)
	{
		type = ContentRuleType.ContainsInRelativeLine;
		this.expectedContent = aContent;
		this.pageIdentifier = aPageIdentifier;
		this.lineNumber = aLineNumber;
	}

	// ###############  Getters #################	
	
	public ContentRuleType getType()
	{
		return type;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}

	public int getPageNumber()
	{
		return pageNumber;
	}

	public String getPageIdentifier()
	{
		return pageIdentifier;
	}

	public String getExpectedContent()
	{
		return expectedContent;
	}
	
	@Override
	public String toString()
	{
		return "DocumentContentSearchValidationRule [type=" + type + ", lineNumber=" + lineNumber + ", pageNumber=" + pageNumber
		        + ", pageIdentifier=" + pageIdentifier + ", expectedContent=" + expectedContent + "]";
	}
		
}
