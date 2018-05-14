package com.iksgmbh.sysnat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.iksgmbh.sysnat.domain.SysNatTestData.ObjectData;
import com.iksgmbh.sysnat.helper.VirtualTestCase;

public class TestCaseTest 
{
	@Test
	public void buildsObjectData() 
	{
		// arrange
		TestCase testCase = new VirtualTestCase("Test");
		List<TestObject> list = new ArrayList<>();
		list.add(new TestObject());
		
		// act
		testCase.buildTestDataFor("TestName", list);
		
		// assert
		ObjectData objectData = testCase.getTestDataSets().getObjectData("TestName");
		assertNotNull(objectData);
		assertEquals("Value for property A", "1", objectData.get("A"));
		assertEquals("Value for property B", "2", objectData.get("B"));
	}
	
	class TestObject
	{
		@Override
		public String toString() {
			return "[A=1,B=2]";
		}
	}
}
