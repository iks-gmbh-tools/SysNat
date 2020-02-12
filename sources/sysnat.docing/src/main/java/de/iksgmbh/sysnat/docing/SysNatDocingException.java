package de.iksgmbh.sysnat.docing;

import com.iksgmbh.sysnat.common.exception.SysNatException;

public class SysNatDocingException extends SysNatException
{
	public SysNatDocingException(String message)
	{
		super(message);
	}
	
	public SysNatDocingException(String message, Exception e)
	{
		super(message, e);
	}

	private static final long serialVersionUID = 1L;

}
