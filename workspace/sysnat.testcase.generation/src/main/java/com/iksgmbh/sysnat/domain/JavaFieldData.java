package com.iksgmbh.sysnat.domain;

public class JavaFieldData 
{
	public String name;
	public Class<?> type;
	
	public JavaFieldData(String aName, Class<?> aType) {
		this.name = aName;
		this.type = aType;
	}
	
	@Override
	public String toString() {
		return "JavaFieldData: " + type.getSimpleName() + " " + name;
	}
}
