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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;

/**
 * Store of datasets holding test data of domain objects.
 * Each datasets is represented by an Property object consisting of fieldname/value pairs.
 * 
 * @author Reik Oberrath
 */
public class SysNatTestData 
{
	public static final String DEFAULT_TEST_DATA = "DefaultTestData";
	
	private HashMap<String, SysNatDataset> datasets = new HashMap<>();
	private HashMap<Integer, String> order = new HashMap<>();  // contains order information for each dataset
	private SysNatDataset markedDataset;

	/**
	 * @param type
	 * @return list of concrete existing known unique test data objects
	 */
	public List<SysNatDataset> findDataSets(final String type) 
	{
		final List<SysNatDataset> datasets = getAllDatasets();
		final List<SysNatDataset> toReturn = new ArrayList<>();
		
		for (SysNatDataset dataset : datasets) 
		{
			if (dataset.getName().startsWith(type)) {
				toReturn.add(dataset);
			}
		}
		
		return toReturn;
	}

	public SysNatDataset findMatchingDataSet(final String nameToSearchFor) 
	{
		final List<Object> keys = Arrays.asList( datasets.keySet().toArray() );
		
		for (Object name : keys) 
		{
			if (name.toString().equals(nameToSearchFor)) {
				return datasets.get(name);
			}
		}
		
		for (Object name : keys) 
		{
			if (name.toString().startsWith(nameToSearchFor)) {
				return datasets.get(name);
			}
		}
		
		// there is no exact match - for a single key only the refix must match
		if (keys.size() == 1) 
		{
			String firstDatasetName = keys.get(0).toString();
			if (firstDatasetName.startsWith(nameToSearchFor)) {
				return datasets.get(firstDatasetName);
			}
		}
		
		return null;
	}

	public List<SysNatDataset> getAllDatasets() 
	{
		final List<SysNatDataset> toReturn = new ArrayList<>();
		final Set<Integer> keySet = order.keySet();
		
		for (Integer key : keySet) {
			toReturn.add(datasets.get(order.get(key)));
		}
		return toReturn;
	}
	
	public void addEmptyDataset(String datasetName) {
		datasets.put(datasetName, new SysNatDataset(datasetName));
		order.put(order.size()+1, datasetName);
	}

	
	public boolean isKnown(String aObjectName) {
		return datasets.containsKey(aObjectName);
	}

	/**
	 * @param name or a prefix of a name of a dataset
	 * @return name of a concrete existing known unique test data object
	 */
	protected String checkDatasetName(final String datasetName) 
	{
		// following is needed for repeated test execution many datasets of the same type
		final List<SysNatDataset> matchingCandidates = findDataSets(datasetName);
		if (matchingCandidates.size() == 1) {
			return datasetName; 
		}
		
		final String singleKnownObjectName = matchingCandidates.get(0).getName();
		int pos = matchingCandidates.get(0).getName().indexOf("_");
		if (pos < 1) {
			return datasetName;
		}
		
		final String postfix = singleKnownObjectName.substring(pos); 
		return datasetName + postfix;
	}

	public void addDataset(String datasetName, Properties data)  
	{
		final SysNatDataset dataset = new SysNatDataset(datasetName, data);
		addDataset(datasetName, dataset);
	}
	
	public void addDataset(String datasetName, SysNatDataset dataset)  {
		datasets.put(datasetName, dataset);
		order.put(order.size()+1, datasetName);
	}

	public void addValue(String datasetName, String fieldName, String value) {
		datasets.get(datasetName).setProperty(fieldName, value);
	}

	/**
	 * Uses DEFAULT_TEST_DATA as dataset 
	 * @param fieldName
	 * @param value
	 */
	public void addValue(String fieldName, String value) 
	{
		if ( size() == 0 ) {
			addEmptyDataset(DEFAULT_TEST_DATA);
		}
		
		if (size() == 1) {
			String datasetName = datasets.keySet().iterator().next();
			datasets.get(datasetName).setProperty(fieldName, value);
		} else {			
			throw new SysNatTestDataException("Es gibt " + size() + " Testdatensätze. Zu welchem soll der Wert für <b>" + fieldName + "</b> zugefügt werden?");
		}
	}

	public SysNatDataset getDataSetForOrderNumber(int i) {
		if (i<1) throw new IllegalArgumentException("Invalid Order Number. Lowest valid Order Number is 1.");
		return datasets.get( order.get(i) );
	}
	
	public SysNatDataset getDataSet(String aDatasetName) {
		return datasets.get( aDatasetName );
	}
	
	
	/**
	 * Works only if one dataset is known !
	 * @param fieldName
	 * @return value
	 */
	public String getValue(String fieldName) 
	{
		if (size() == 1) {
			String datasetName = datasets.keySet().iterator().next();
			return getValue(datasetName, fieldName);
		}
		
		if (size() == 0) {
			throw new SysNatTestDataException("Es wurden keine Testdatensätze geladen!");
		}
		
		throw new SysNatTestDataException("Es gibt " + size() + " Testdatensätze. Von welchem wird der Wert für <b>" + fieldName + "</b> benötigt?");
	}

	public String getValue(String aDatasetName, String fieldName) 
	{
		final SysNatDataset dataset = datasets.get(aDatasetName);
		
		if (dataset == null) {
			throw new SysNatTestDataException("Name of dataset <b>" + aDatasetName + "</b> is unknown.");
		}
		
		return dataset.getProperty(fieldName);
	}
		

	public int size() {
		return datasets.size();
	}
	
	public void clear() 
	{
		datasets.clear();
		order.clear();
		markedDataset = null;
	}

	@Override
	public String toString() {
		return "SysNatTestData [" + getAllDatasets() + "]";
	}

	/**
	 * Searches in all datasets for the given valueReference 
	 * in order to replace it by the corresponding value
	 * @param valueReference
	 * @return value
	 */
	public String findValueForValueReference(final String valueReference) 
	{
		if (markedDataset != null) 
		{
			String fieldName = valueReference;
			if (valueReference.startsWith(":")) {
				fieldName = valueReference.substring(1);
			}
			return markedDataset.getProperty(fieldName);
		}
		
		Set<String> keySet = datasets.keySet();
		List<String> datasetMatches = keySet.stream().filter(key -> valueReference.contains(key))
				                                     .collect(Collectors.toList());
		if (   datasetMatches.size() == 0 
			&& keySet.size() == 1 
			&& valueReference.contains(":")) 
		{
			String fieldName;
			if (valueReference.startsWith(":")) {
				fieldName = valueReference.substring(1);
			} else {
				int pos = valueReference.indexOf(":");
				fieldName = valueReference.substring(pos+1);
			}
			return getValue(fieldName);
		}
		
		for (String datasetName : datasetMatches) 
		{
			List<Object> fieldMatches = datasets.get(datasetName).keySet().stream()
					                            .filter(field -> valueReference.contains(datasetName + ":" + field))
					                            .collect(Collectors.toList());
			String toReturn = valueReference;
			for (Object field : fieldMatches) {
				String fieldName = field.toString();
				toReturn = toReturn.replace(datasetName + ":" + fieldName, getValue(datasetName, fieldName));
			}
			
			return toReturn;
		}
		
		return getValue(valueReference);
	}
	
	
	/**
	 * List of key-value-pairs that represent a data set for a domain object. 
	 */
	public static class SysNatDataset extends Properties 
	{
		private static final long serialVersionUID = 1L;

		private String name;

		public SysNatDataset(String name) {
			this.name = name;
		}
		
		public SysNatDataset(String name, Properties data) 
		{
			this.name = name;
			Iterator<String> iterator = data.stringPropertyNames().iterator();
			while (iterator.hasNext()) {
				String fieldName = (String) iterator.next();
				setProperty(fieldName, data.getProperty(fieldName));
			}
		}
		
		public String getValue(String fieldName) {
			return getProperty(fieldName);
		}
		
		public String getValueWithReplacedUnderscores(String fieldName) 
		{
			String toReturn = getValue(fieldName);
			if (toReturn != null) {				
				toReturn = getValue(fieldName).replaceAll("_", " ");
			}
			return toReturn;
		}

		public String getName() {
			return name;
		}
		
		public SysNatDataset dublicate(String newName) 
		{
			Enumeration<Object> keys = this.keys();
			SysNatDataset toReturn = new SysNatDataset(newName);
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				String value = this.getProperty(key);
				toReturn.put(key, value);
			}
			return toReturn;
		}
	}


	public void initDefaultDataset() 
	{
		if (datasets.containsKey(DEFAULT_TEST_DATA)) {
			datasets.get(DEFAULT_TEST_DATA).clear();
		} else {
			addEmptyDataset(DEFAULT_TEST_DATA);
		}
	}

	/**
	 * This dataset is going to be used shortly
	 * @param aDatasetName
	 */
	public void setMarker(SysNatDataset dataset) {
		markedDataset = dataset;
	}

}