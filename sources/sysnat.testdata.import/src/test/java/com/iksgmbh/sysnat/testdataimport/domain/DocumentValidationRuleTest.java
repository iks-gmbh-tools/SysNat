package com.iksgmbh.sysnat.testdataimport.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class DocumentValidationRuleTest
{

	@Test
	public void createsSimpleInstanceFromStringRepresentation()
	{
		// arrange
		DocumentContentSearchValidationRule rule = new DocumentContentSearchValidationRule("test");
		
		// act
		DocumentValidationRule result = DocumentValidationRule.getSearchInstance(rule.toString());
		
		// assert
		assertTrue("result is not equal to rule", rule.toString().equals(result.toString()));
	}

	@Test
	public void createsInstanceWithPageNumberFromStringRepresentation()
	{
		// arrange
		DocumentContentSearchValidationRule rule = new DocumentContentSearchValidationRule("test", 1);
		
		// act
		DocumentValidationRule result = DocumentValidationRule.getSearchInstance(rule.toString());
		
		// assert
		assertTrue("result is not equal to rule", rule.toString().equals(result.toString()));
	}
	
	@Test
	public void createsInstanceWithLineNumberFromStringRepresentation()
	{
		// arrange
		DocumentContentSearchValidationRule rule = new DocumentContentSearchValidationRule("test", 1, 2);
		
		// act
		DocumentValidationRule result = DocumentValidationRule.getSearchInstance(rule.toString());
		
		// assert
		assertTrue("result is not equal to rule", rule.toString().equals(result.toString()));
	}
	
	@Test
	public void createsInstanceWithLineIdentifierFromStringRepresentation()
	{
		// arrange
		DocumentContentSearchValidationRule rule = new DocumentContentSearchValidationRule("test1", "test2");
		
		// act
		DocumentValidationRule result = DocumentValidationRule.getSearchInstance(rule.toString());
		
		// assert
		assertTrue("result is not equal to rule", rule.toString().equals(result.toString()));
	}

	@Test
	public void createsInstanceWithLineIdentifierAndLineNumberFromStringRepresentation()
	{
		// arrange
		DocumentContentSearchValidationRule rule = new DocumentContentSearchValidationRule("test1", "test2", 3);
		
		// act
		DocumentValidationRule result = DocumentValidationRule.getSearchInstance(rule.toString());
		
		// assert
		assertTrue("result is not equal to rule", rule.toString().equals(result.toString()));
	}
	
}
