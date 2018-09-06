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
package com.iksgmbh.sysnat.domain;

/**
 * Wrapper class for a string that represents a name of a complete java command.
 * 
 * @author Reik Oberrath
 */
public class JavaCommand 
{
	/**
	 * One line of Java code for the JUnit test case to create.
	 */
	public String value;

	/**
	 * Meta information about this Java command line.
	 */
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