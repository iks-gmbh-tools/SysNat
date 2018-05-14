package com.iksgmbh.sysnat.exception;

public class SkipTestCaseException extends SysNatException 
{
	public enum SkipReason {APPLICATION_TO_TEST, CATEGORY_FILTER, ACTIVATION_STATE};
	private static final long serialVersionUID = 1L;

	private SkipReason reason;
	
	public SkipTestCaseException(SkipReason aReason)  {
		super("");
		this.reason = aReason;
	}

	public SkipReason getSkipReason() {
		return reason;
	}
}
