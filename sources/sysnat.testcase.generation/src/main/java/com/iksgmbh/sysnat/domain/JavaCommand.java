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

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for a string that represents a name of a complete java command.
 * 
 * @author Reik Oberrath
 */
public class JavaCommand 
{
	public static enum CommandType { Constant,  // used for the behaviourId which is defined as a java class constant
		                             OneTimePrecondition, Precondition, 
		                             Standard, // identifies common test instructions
		                             Cleanup, OneTimeCleanup };
	
	/**
	 * One line of Java code for the JUnit test case to create.
	 */
	public String value;
	public List<Object> testObjectParameters;

	/**
	 * Meta information about this Java command line.
	 */
	public String returnType;
	public CommandType commandType;
	
	public JavaCommand(final String aValue) {
		this(aValue, CommandType.Standard);
	}

	public JavaCommand(final String aValue, final CommandType aCommandType) {
		this.value = aValue;
		this.commandType = aCommandType;
	}

	public JavaCommand(final String aValue, final Class<?> returnType) {
		this(aValue, returnType, CommandType.Standard, new ArrayList<>());
	}

	public JavaCommand(final String aValue,
	           final Class<?> returnType,
	           final CommandType aCommandType)
	{
		this(aValue, returnType, aCommandType, new ArrayList<>());
	}

	public JavaCommand(final String aValue,
			           final Class<?> returnType,
			           final CommandType aCommandType,
			           final List<Object> paramVariables) 
	{
		this.value = aValue;
		if (returnType != null) {
			this.returnType = returnType.getName();
		}
		this.commandType = aCommandType;
		this.testObjectParameters = paramVariables;
	}
	
	@Override
	public String toString() {
		return value;
	}
}