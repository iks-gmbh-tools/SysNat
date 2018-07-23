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
	private HashMap<String, SysNatDataset> datasets = new HashMap<>();
	private HashMap<Integer, String> order = new HashMap<>();  // contains order information for each dataset

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

	public SysNatDataset getDataSetForName(final String nameToSearchFor) 
	{
		final List<Object> keys = Arrays.asList( datasets.keySet().toArray() );
		
		for (Object name : keys) 
		{
			if (name.toString().equals(nameToSearchFor)) {
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
	
	public void addEmptyObjectData(String aObjectName) {
		datasets.put(aObjectName, new SysNatDataset(aObjectName));
		order.put(order.size()+1, aObjectName);
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

	public SysNatDataset getDataSetForOrderNumber(int i) {
		if (i<1) throw new IllegalArgumentException("Invalid Order Number. Lowest valid Order Number is 1.");
		return datasets.get( order.get(i) );
	}
	
	public SysNatDataset getDataSet(String aDatadetName) {
		return datasets.get( aDatadetName );
	}
	
	public String getValue(String aDatasetName, String fieldName) 
	{
		final SysNatDataset dataset = datasets.get(aDatasetName);
		
		if (dataset == null) {
			throw new SysNatTestDataException("Name of dataset <b>" + aDatasetName + "</b> is unknown.");
		}
		
		return dataset.getProperty(fieldName);
	}

	/**
	 * Works only if one dataset is known !
	 * @param fieldName
	 * @param value
	 */
	public void addValue(String fieldName, String value) 
	{
		if (size() == 1) {
			String objectName = datasets.keySet().iterator().next();
			datasets.get(objectName).setProperty(fieldName, value);
		} else if (size() == 0) {
			addEmptyObjectData("DefaultTestData");
			addValue(fieldName, value);
		} else {
			throw new SysNatTestDataException("Es gibt " + size() + " Testdaten-Objekte. Zu welchem soll der Wert für <b>" + fieldName + "</b> hinzugefügt werden?");
		}
	}
	
	/**
	 * Works only if one dataset is known !
	 * @param fieldName
	 * @return value
	 */
	public String getValue(String fieldName) 
	{
		if (size() == 1) {
			String objectName = datasets.keySet().iterator().next();
			return getValue(objectName, fieldName);
		}
		
		if (size() == 0) {
			throw new SysNatTestDataException("Es wurden keine Testdatensätze geladen!");
		}
		
		throw new SysNatTestDataException("Es gibt " + size() + " Testdatensätze. Von welchem wird der Wert für <b>" + fieldName + "</b> benötigt?");
	}
	

	public int size() {
		return datasets.size();
	}
	
	public void clear() {
		datasets.clear();
		order.clear();
	}

	@Override
	public String toString() {
		return "SysNatTestData [" + getAllDatasets() + "]";
	}

	public String getValueFor(String valueCandidate) 
	{
		if (valueCandidate.startsWith(":")) {
			String fieldName = valueCandidate.substring(1);
			return getValue(fieldName);
		}
		
		while (valueCandidate.contains(":")) {
			valueCandidate = replaceTestDataReferences(valueCandidate);
		}
		
		return valueCandidate;
	}

	private String replaceTestDataReferences(final String valueCandidate) 
	{
		String toReturn = valueCandidate;
		Set<String> keySet = datasets.keySet();
		List<String> datasetMatches = keySet.stream().filter(key -> valueCandidate.contains(key)).collect(Collectors.toList());
		
		if (datasetMatches.size() == 0 && keySet.size() == 1 && valueCandidate.contains(":")) 
		{
			int pos = valueCandidate.indexOf(":");
			String testdataIdCandidate = valueCandidate.substring(0, pos);
			if (keySet.iterator().next().startsWith(testdataIdCandidate))
				return keySet.iterator().next() + valueCandidate.substring(pos);
		}
		
		for (String datasetName : datasetMatches) 
		{
			List<Object> fieldMatches = datasets.get(datasetName).keySet().stream()
					                            .filter(field -> valueCandidate.contains(datasetName + ":" + field)).collect(Collectors.toList());
			
			for (Object field : fieldMatches) {
				String fieldName = field.toString();
				toReturn = toReturn.replace(datasetName + ":" + fieldName, getValue(datasetName, fieldName));
			}
		}
		return toReturn;
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

}
