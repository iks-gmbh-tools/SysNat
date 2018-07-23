'
'	public static boolean validateDataPool() 
'	{
'		boolean ok = true;
'		Object[] keys = dataPool.keySet().toArray();
'		int numberOfElements = dataPool.get(keys[0]).size();
'		for (Object key : keys) {
'			int num = dataPool.get(key).size();
'			if (numberOfElements != num) {
'				System.err.println("Data Pool entry '" + key.toString() + "' does not match expected number of elements.");
'				ok = false;
'			}
'		}
'
'		if (ok) {
'			System.out.println("Data pool size of " + ${classDescriptor.simpleName}Factory.class.getSimpleName() + ": " + numberOfElements);
'		}
'
'		return ok;
'	}
