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
package com.iksgmbh.sysnat.common.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileList 
{
	private List<File> list;
	private String fileListName;
	
	public FileList() {
		list = new ArrayList<>();
	}
	
	public void add(File file)  {
		list.add(file);
	}
	
	public int size()  {
		return list.size();
	}

	public String getName()  {
		return fileListName;
	}

	public List<File> getFiles()  {
		return list;
	}
	
	public void setName(String aFileListName) {
		this.fileListName = aFileListName;
	}

}