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
package com.iksgmbh.sysnat.helper.docval.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains meta information about a part of a larger document.
 * It represents a group of pages that follow it other.
 * 
 * @author Reik Oberrath
 *
 */
public class DocumentPart 
{
	private String id;
	private String partName;
	private int numberOfPages;
	private List<String> firstPageIdentifiers = new ArrayList<>();
	private List<String> sequelPageIdentifiers = new ArrayList<>();
	
	public DocumentPart(String aId, int numberOfPages)
	{
		this.id = aId;
		this.numberOfPages = numberOfPages;
	}
	
	public void addFirstPageIdentifier(String identifier) {
		firstPageIdentifiers.add(identifier);
	}

	public void addSequelPageIdentifier(String identifier) {
		sequelPageIdentifiers.add (identifier);
	}

	public void setPartName(String aPartName) {
		partName = aPartName;
	}

	public String getPartName() {
		return partName;
	}

	public String getId() {
		return id;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public List<String> getFirstPageIdentifiers() {
		return firstPageIdentifiers;
	}

	public List<String> getSequelPageIdentifiers() {
		return sequelPageIdentifiers;
	}

}
