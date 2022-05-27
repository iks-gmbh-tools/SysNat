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
 */package com.iksgmbh.sysnat.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.iksgmbh.sysnat.common.exception.SysNatTestDataException;
import com.iksgmbh.sysnat.common.utils.SysNatConstants;

/**
 * Store of datasets provided for use to the current executable example (XX).
 * Each dataset is represented by an Property object consisting of fieldname/value pairs loaded from a data file.
 * All single key/value-pairs added to this store are collected in the dataset named SINGLE_TEST_DATA_VALUES.
 * 
 * When looking for a value to a given key (without explicitly naming or marking a specific dataset)
 * firstly the SINGLE_TEST_DATA_VALUES dataset is searched for the key.
 * Then, the other datasets in the opposite order of their addition to the store.
 * This means, the first dataset added to this store during the execution of the XX
 * is looked up last to contain the given key. Thus, when a key is contained in more
 * than one dataset, the latest added dataset will give the value returned for the key. 
 * 
 * @author Reik Oberrath
 */
public class SysNatTestData 
{
   public static final String SINGLE_TEST_DATA_VALUES = "SingleTestDataValues";

   private LinkedHashMap<String, SysNatDataset> orderedDatasets = new LinkedHashMap<>();  // order reflects priority when searching a value
   private HashMap<Integer, String> order = new HashMap<>();  // contains order information for each dataset
   private List<ValuePair> synonyms = new ArrayList<>();  // contains pairs of keys that can replace each other when searching for datasets
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
      final List<Object> keys = Arrays.asList( orderedDatasets.keySet().toArray() );

      // first look for an exact match
      for (Object name : keys)
      {
         if (name.toString().equals(nameToSearchFor)) {
            return orderedDatasets.get(name);
         }
      }

      // no exact match found - now look for an prefix match
      for (Object name : keys)
      {
         if (name.toString().startsWith(nameToSearchFor)) {
            return orderedDatasets.get(name);
         }
      }

      return null;
   }

   public List<SysNatDataset> getAllDatasets()
   {
      final List<SysNatDataset> toReturn = new ArrayList<>();
      final Set<Integer> keySet = order.keySet();

      for (Integer key : keySet) {
         toReturn.add(orderedDatasets.get(order.get(key)));
      }
      return toReturn;
   }

   public void addEmptyDataset(String datasetName) {
      orderedDatasets.put(datasetName, new SysNatDataset(datasetName));
      order.put(order.size()+1, datasetName);
   }


   public boolean isKnown(String aObjectName) {
      return orderedDatasets.containsKey(aObjectName);
   }

   /**
    * @param aDatasetName or a prefix of a name of a dataset
    * @return name of a concrete existing known unique test data object
    */
   protected String checkDatasetName(final String aDatasetName)
   {
      // following is needed for repeated test execution many orderedDatasets of the same type
      final List<SysNatDataset> matchingCandidates = findDataSets(aDatasetName);
      if (matchingCandidates.size() == 1) {
         return aDatasetName;
      }

      final String singleKnownObjectName = matchingCandidates.get(0).getName();
      int pos = matchingCandidates.get(0).getName().indexOf("_");
      if (pos < 1) {
         return aDatasetName;
      }

      final String postfix = singleKnownObjectName.substring(pos);
      return aDatasetName + postfix;
   }

   public void addDataset(String datasetName, Properties data)
   {
      final SysNatDataset dataset = new SysNatDataset(datasetName, data);
      addDataset(datasetName, dataset);
   }

   public void addDataset(String datasetName, SysNatDataset dataset)  {
      orderedDatasets.put(datasetName, dataset);
      order.put(order.size()+1, datasetName);
   }

   public void addValue(String datasetName, String fieldName, String value) {
      orderedDatasets.get(datasetName).setProperty(fieldName, value);
   }

   /**
    * Uses SINGLE_TEST_DATA_VALUES as dataset
    * @param fieldName
    * @param value
    */
   public void addValue(String fieldName, String value)
   {
      SysNatDataset defaultDataSet = orderedDatasets.get(SINGLE_TEST_DATA_VALUES);
      if (defaultDataSet == null) {
         addEmptyDataset(SINGLE_TEST_DATA_VALUES);
      }
      addValue(SINGLE_TEST_DATA_VALUES, fieldName, value);
   }

   public SysNatDataset getDataSetForOrderNumber(int i) {
      if (i<1) throw new IllegalArgumentException("Invalid Order Number. Lowest valid Order Number is 1.");
      return orderedDatasets.get( order.get(i) );
   }

   public SysNatDataset getDataSet(String aDatasetName) {
      return orderedDatasets.get( aDatasetName );
   }


   /**
    * Works only if one dataset is known !
    * @param fieldName
    * @return value
    */
   public String getValue(String fieldName)
   {
      if (size() == 0) {
         throw new SysNatTestDataException("Es stehen keine Testdaten zur Verfügung!");
      }

      // first try to get value from an individually defined field
      SysNatDataset singleTestDataSet = orderedDatasets.get(SINGLE_TEST_DATA_VALUES);
      if (singleTestDataSet != null) {
         String toReturn = singleTestDataSet.getValue(fieldName);
         if (toReturn != null) {
            return singleTestDataSet.getValue(fieldName);
         }
      }

      Set<Integer> orderKeys = order.keySet();
      List<Integer> orderKeyList = new ArrayList<>();
      orderKeyList.addAll(orderKeys);

      Collections.sort(orderKeyList, Collections.reverseOrder());
      Optional<?> datasetName = orderKeyList.stream().filter(key -> containsField(order.get(key), fieldName)).findFirst();

      if (datasetName.isPresent()) {
         return orderedDatasets.get(order.get(datasetName.get())).getValue(fieldName);
      }

      return null;
   }

   private boolean containsField(String datasetName, String fieldName) {
      try {
         return orderedDatasets.get(datasetName).getValue(fieldName) != null;
      } catch (Exception e) {
         return false;
      }

   }


   public String getValue(String aDatasetName, String fieldName)
   {
      final SysNatDataset dataset = orderedDatasets.get(aDatasetName);

      if (dataset == null) {
         throw new SysNatTestDataException("Name of dataset <b>" + aDatasetName + "</b> is unknown.");
      }

      String value = dataset.getProperty(fieldName);
      if (value == null) {
         throw new SysNatTestDataException("Das Feld <b>" + fieldName + "</b> ist im Datensatz <b>" + aDatasetName + "</b> nicht bekannt.");
      }

      return value;
   }


   public int size() {
      return orderedDatasets.size();
   }

   public void clear()
   {
      orderedDatasets.clear();
      order.clear();
      markedDataset = null;
   }

   @Override
   public String toString() {
      return "SysNatTestData [" + getAllDatasets() + "]";
   }


   public String findDatasetNameForDatasetReference(final String dataSetReference)
   {
      Set<String> keySet = orderedDatasets.keySet();
      List<String> datasetMatches = keySet.stream().filter(key -> key.contains(dataSetReference))
                                                 .collect(Collectors.toList());
      if (datasetMatches.size() == 1) {
         return datasetMatches.get(0);
      }

      if (size() == 0) {
         throw new SysNatTestDataException("Es sind keine Testdaten gesetzt.");
      }

      throw new SysNatTestDataException("Es gibt für <b>" + dataSetReference + "</b> <b>" + datasetMatches.size() + "</b> matchende Testdatensätze. Es passt keiner zu Datensatz-Reference <b>" + dataSetReference + "</b>.");

   }

   /**
    * Searches in all orderedDatasets for the given valueReference
    * in order to replace it by the corresponding value
    * @param valueReference
    * @return value
    */
   public String findValueForValueReference(final String valueReference)
   {
	  if (orderedDatasets.get(SINGLE_TEST_DATA_VALUES) != null) {
		  Object result = orderedDatasets.get(SINGLE_TEST_DATA_VALUES).get(valueReference);
		  if (result != null) return result.toString();
	  }
	  
      if (markedDataset != null)
      {
         String fieldName = valueReference;
         if (valueReference.startsWith("::")) {
            fieldName = valueReference.substring(2);
         }
         return markedDataset.getProperty(fieldName);
      }

      Set<String> keySet = orderedDatasets.keySet();
      List<String> datasetMatches = keySet.stream().filter(key -> orderedDatasets.get(key).containsKey(valueReference))
                                                   .collect(Collectors.toList());
      if (datasetMatches.size() > 1) {
    	  throw new SysNatTestDataException("Die Referenz des Testdatenwerts <b>" + valueReference + "</b> "
    	  		                          + "ist nicht eindeutig, weil es " + datasetMatches.size() 
    	  		                          + " Testdatensätze gibt, die sie beinhalten!");
      }
      
      if (datasetMatches.size() == 1) return orderedDatasets.get(datasetMatches.get(0)).getProperty(valueReference);
      
      if (valueReference.contains("::"))
      {
         String fieldName;
         if (valueReference.startsWith("::")) {
            fieldName = valueReference.substring(2);
         } else {
            int pos = valueReference.indexOf("::");
            fieldName = valueReference.substring(pos+2);
         }
         return getValue(fieldName);
      } 

      for (String datasetName : datasetMatches)
      {
         List<Object> fieldMatches = orderedDatasets.get(datasetName).keySet().stream()
                                           .filter(field -> valueReference.contains(datasetName + SysNatConstants.DC + field))
                                           .collect(Collectors.toList());
         String toReturn = valueReference;
         for (Object field : fieldMatches) {
            String fieldName = field.toString();
            toReturn = toReturn.replace(datasetName + SysNatConstants.DC + fieldName, getValue(datasetName, fieldName));
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
      if (orderedDatasets.containsKey(SINGLE_TEST_DATA_VALUES)) {
         orderedDatasets.get(SINGLE_TEST_DATA_VALUES).clear();
      } else {
         addEmptyDataset(SINGLE_TEST_DATA_VALUES);
      }
   }

   /**
    * This dataset is going to be used shortly
    * @param aDatasetName
    */
   public void setMarker(SysNatDataset aDataset) {
      markedDataset = aDataset;
   }
   
   public void setMarker(String aDatasetName) {
	  markedDataset = orderedDatasets.get(aDatasetName);
   }
   
   public void resetMarker() {
	  markedDataset = null;
   }
   
   public boolean addSynonym(String value1, String value2) 
   {
	   synonyms.add(new ValuePair(value1, value2));
	   return true;
   }

	public List<String> getSynonmysFor(String fieldName)
	{
		if (fieldName.startsWith(SysNatConstants.DC)) fieldName = fieldName.substring( SysNatConstants.DC.length() );
		List<String> toReturn = new ArrayList<>();
		
		final String search = fieldName;
		synonyms.forEach(pair -> { if (pair.value1.equals(search)) toReturn.add(pair.value2); });
		synonyms.forEach(pair -> { if (pair.value2.equals(search)) toReturn.add(pair.value1); });
		
		return toReturn;
	}
	
	private class ValuePair 
	{
		String value1;
		String value2;
		public ValuePair(String value1, String value2)
		{
			this.value1 = value1;
			this.value2 = value2;
		}
	}
}

