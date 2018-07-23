'	static 
'	{

	'		// fill data pool

	#foreach($attributeDescriptor in $classDescriptor.attributeDescriptorList)
	
		#set( $AttributeName = $TemplateStringUtility.firstToUpperCase($attributeDescriptor.name) )
		'		fill${AttributeName}DataPool1();
		
	#end


	'
	'		// set maxLength values 

	#foreach($attributeDescriptor in $classDescriptor.attributeDescriptorList)

		#set( $AttributeName = $TemplateStringUtility.firstToUpperCase($attributeDescriptor.name) )
	    #set( $MaxLength = $attributeDescriptor.getMetaInfoValueFor("MaxLength") )
		 
		#if ( $classDescriptor.isValueAvailable($MaxLength) )
		
			'		maxLengths.put("$AttributeName", new Integer( $MaxLength ));
		
		#else
		
		    #set( $javaType = $attributeDescriptor.getMetaInfoValueFor("JavaType") ) 
			
			#if ($javaType == "String")

				'		maxLengths.put("$AttributeName", new Integer( DEFAULT_MAX_LENGTH_STRING_VALUE ));
			
			#elseif ($javaType == "long" || $javaType == "Long"
			         || $javaType == "int" || $javaType == "Integer"
			         || $javaType == "byte" || $javaType == "Byte")
			         
				'		maxLengths.put("$AttributeName", new Integer( DEFAULT_MAX_LENGTH_NUMBER_VALUE ));
			
			#end
		
		#end
	
	#end




    '
	'		// set minLength values

	#foreach($attributeDescriptor in $classDescriptor.attributeDescriptorList)

		#set( $AttributeName = $TemplateStringUtility.firstToUpperCase($attributeDescriptor.name) )
	    #set( $MinLength = $attributeDescriptor.getMetaInfoValueFor("MinLength") ) 
		 
		#if ( $classDescriptor.isValueAvailable($MinLength) )
		
			'		minLengths.put("$AttributeName", new Integer( $MinLength ));
		
		#else
		
		    #set( $javaType = $attributeDescriptor.getMetaInfoValueFor("JavaType") ) 
			
			#if ($javaType == "String")

				'		minLengths.put("$AttributeName", new Integer( DEFAULT_MIN_LENGTH_STRING_VALUE ));
			
			#elseif ($javaType == "long" || $javaType == "Long"
			         || $javaType == "int" || $javaType == "Integer"
			         || $javaType == "byte" || $javaType == "Byte")
			         
				'		minLengths.put("$AttributeName", new Integer( DEFAULT_MIN_LENGTH_NUMBER_VALUE ));
			
			#end
		
		#end
	
	#end
	
'	}

#foreach($attributeDescriptor in $classDescriptor.attributeDescriptorList)
'
	#set( $AttributeName = $TemplateStringUtility.firstToUpperCase($attributeDescriptor.name) ) 
	#set( $attributeName = $TemplateStringUtility.firstToLowerCase($attributeDescriptor.name) ) 
	#set( $MetaInfoList = $attributeDescriptor.getMetaInfosWithNameStartingWith("${classDescriptor.simpleName}"))
	#set( $counter = 0 )
	
	'	public static void fill${AttributeName}DataPool1()
	'	{
	'		final List<String> ${attributeName}List = new ArrayList<String>();
	
	#foreach($metainfo in $MetaInfoList)
	
		#set( $counter = $counter + 1 )
	
		#if( $counter < 5750)

			'		${attributeName}List.add("$metainfo.value");
			
		#end	
	
	#end
	
	'
	'        ${attributeName}List.addAll(fill${AttributeName}DataPool2());
	'		dataPool.put("$AttributeName", ${attributeName}List);
'	}
'
'	public static List<String> fill${AttributeName}DataPool2() {
'		final List<String> toReturn = new ArrayList<String>();

	#set( $counter = 0 )
	
	#foreach($metainfo in $MetaInfoList)
	
		#set( $counter = $counter + 1 )
	
		#if( $counter >= 5750)

			'		toReturn.add("$metainfo.value");
			
		#end	
	#end
	
	'		return toReturn; 
'	}
	
#end

'
'	public static void fillObjectIdDataPool1()
'	{
'		final List<String> objectIdList = new ArrayList<String>();
        
        #set( $counter = 0 )

		#foreach($metainfo in $MetaInfoList)
		
			#set( $counter = $counter + 1 )
		
			#if( $counter < 5750)

	   			'		objectIdList.add( "$metainfo.name" );
			
			#end	
		
		#end
		
'        objectIdList.addAll(fillObjectIdDataPool2());
'		dataPool.put(OBJECT_ID, objectIdList);
'	}
'
'
'	public static List<String> fillObjectIdDataPool2()
'	{
'		final List<String> objectIdList = new ArrayList<String>();
        
        #set( $counter = 0 )

		#foreach($metainfo in $MetaInfoList)
		
			#set( $counter = $counter + 1 )
		
			#if( $counter >= 5750)

	   			'		objectIdList.add( "$metainfo.name" );
			
			#end	
		
		#end
		
'		return objectIdList;
'	}