package de.iksgmbh.sysnat.docing;

public class DocingRuntimeInfo //extends ExecutionRuntimeInfo
{
	private static DocingRuntimeInfo instance;

	public static DocingRuntimeInfo getInstance() 
	{
		if (instance == null)  {
			instance = new DocingRuntimeInfo();
		}
		return instance;
	}
	
	private DocingRuntimeInfo() {
		super();
	}

}
