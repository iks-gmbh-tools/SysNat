package com.iksgmbh.sysnat.domain;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.iksgmbh.sysnat.exception.TestDataException;

/**
 * Map of properties-objects that represent test data for domain objects.
 * 
 * @author Reik Oberrath
 */
public class SysNatTestData 
{
	private HashMap<String, ObjectData> dataSets = new HashMap<>();
	private HashMap<Integer, String> order = new HashMap<>();  // contains order information for each dataset
	
	public boolean isKnown(String aObjectName)
	{
		aObjectName = checkObjectName(aObjectName);
		return dataSets.containsKey(aObjectName);
	}

	private String checkObjectName(String aObjectName) 
	{
		// following is needed for repeated test execution many datasets of the same type
		List<ObjectData> matchingCandidates = findDataSetsWithSuffix(aObjectName);
		if (matchingCandidates.size() != 1) return aObjectName;
		String singleKnownObjectName = matchingCandidates.get(0).getName();
		int pos = matchingCandidates.get(0).getName().indexOf("_");
		if (pos < 1) return aObjectName;
		String postfix = singleKnownObjectName.substring(pos); 
		return aObjectName + postfix;
	}

	private List<ObjectData> findDataSetsWithSuffix(String aObjectName) 
	{
		List<ObjectData> allObjectData = getAllObjectData();
		List<ObjectData> toReturn = new ArrayList<>();
		for (ObjectData objectData : allObjectData) {
			if (objectData.getName().startsWith(aObjectName)) {
				toReturn.add(objectData);
			}
		}
		return toReturn;
	}

	public List<ObjectData> getAllObjectData() 
	{
		List<ObjectData> toReturn = new ArrayList<>();
		Set<Integer> keySet = order.keySet();
		for (Integer key : keySet) {
			toReturn.add(dataSets.get(order.get(key)));
		}
		return toReturn;
	}
	
	public void addEmptyObjectData(String aObjectName) {
		dataSets.put(aObjectName, new ObjectData(aObjectName));
		order.put(order.size()+1, aObjectName);
	}
	
	public void addObjectData(String aObjectName, ObjectData someObjectData)  {
		dataSets.put(aObjectName, someObjectData);
		order.put(order.size()+1, aObjectName);
	}

	public void add(String objectName, String fieldName, String value) {
		dataSets.get(objectName).setProperty(fieldName, value);
	}
	
	public ObjectData getObjectData(String aObjectName) {
		aObjectName = checkObjectName(aObjectName);
		return dataSets.get(aObjectName);
	}

	public ObjectData getObjectData(int i) {
		return dataSets.get( order.get(i) );
	}
	
	public String getValue(String aObjectName, String fieldName) {
		aObjectName = checkObjectName(aObjectName);
		return dataSets.get(aObjectName).getProperty(fieldName);
	}

	public void add(String fieldName, String value) 
	{
		if (size() == 1) {
			String objectName = dataSets.keySet().iterator().next();
			dataSets.get(objectName).setProperty(fieldName, value);
		} else if (size() == 0) {
			addEmptyObjectData("DefaultTestData");
			add(fieldName, value);
		} else {
			throw new TestDataException("Es gibt " + size() + " Testdaten-Objekte. Zu welchem soll der Wert für <b>" + fieldName + "</b> hinzugefügt werden?");
		}
	}
	
	public String getValue(String fieldName) 
	{
		if (size() == 1) {
			String objectName = dataSets.keySet().iterator().next();
			return getValue(objectName, fieldName);
		}
		
		throw new TestDataException("Es gibt " + size() + " Testdaten-Objekte. Von welchem wird der Wert für <b>" + fieldName + "</b> benötigt?");
	}
	

	public int size() {
		return dataSets.size();
	}
	
	public void clear() {
		dataSets.clear();
		order.clear();
	}
	
	

	@Override
	public String toString() {
		return "SysNatTestData [" + getAllObjectData() + "]";
	}



	/**
	 * List of key-value-pairs that represent a data set for a domain object. 
	 */
	public static class ObjectData extends Properties 
	{
		private static final long serialVersionUID = 1L;

		private String name;

		public ObjectData(String name) {
			this.name = name;
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
		
		public ObjectData dublicate(String newName) 
		{
			Enumeration<Object> keys = this.keys();
			ObjectData toReturn = new ObjectData(newName);
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				String value = this.getProperty(key);
				toReturn.put(key, value);
			}
			return toReturn;
		}
	}
}
