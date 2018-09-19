package com.iksgmbh.sysnat.test;

import java.util.ArrayList;
import java.util.List;

public class Calculator 
{
	private List<Integer> enteredNumbers = new ArrayList<>();
	private Integer result = null;
	
	public void enter(int number) {
		enteredNumbers.add(number);
	}
	
	public void addAllEnteredNumbers() {
		result = enteredNumbers.stream().mapToInt(Integer::intValue).sum();
	}

	public Integer getResult() {
		return result;
	}
	
}
