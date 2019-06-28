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
package com.iksgmbh.sysnat.helper;

import com.iksgmbh.sysnat.ExecutableExample;

public class PopupHandler 
{
	private static ExecutableExample executableExample;

	public static void closeByOk() {
		executableExample.sleep(100);
		executableExample.clickElement("closeButtons");		
		executableExample.sleep(100);
	}

	public static void setTestCase(ExecutableExample aExecutableExample) {
		executableExample = aExecutableExample;
	}
}