package com.iksgmbh.sysnat.domain;

/**
 * Wrapper class for a string that represents a name of a complete java command.
 * 
 * @author Reik Oberrath
 */
public class JavaCommand 
{
	public String value;
	public String importType;
	
	public JavaCommand(final String aValue) {
		this.value = aValue;
	}

	public JavaCommand(final String aValue,
			           final Class<?> returnType) 
	{
		this.value = aValue;
		if (returnType != null) {
			this.importType = returnType.getName();
		}
	}
	
	@Override
	public String toString() {
		return value;
	}
}
