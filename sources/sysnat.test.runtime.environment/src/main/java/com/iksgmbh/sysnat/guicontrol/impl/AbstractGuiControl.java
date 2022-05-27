package com.iksgmbh.sysnat.guicontrol.impl;

import java.util.Date;

import com.iksgmbh.sysnat.ExecutionRuntimeInfo;
import com.iksgmbh.sysnat.common.utils.SysNatDateUtil;
import com.iksgmbh.sysnat.guicontrol.GuiControl;

public abstract class AbstractGuiControl implements GuiControl
{
	protected Integer nextExecutionSpeedWaitState = null; 
	
	public void setNextExecutionSpeedWaitState(int millis) {
		nextExecutionSpeedWaitState = millis;
	}
	
	protected void checkExecutionSpeed(Date startTime)
	{
		Date endTime = new Date();
		int duration = SysNatDateUtil.getDiffInMillis(startTime, endTime);
		int minimum = ExecutionRuntimeInfo.getInstance().getMillisToWaitForAvailabilityCheck();
		if (nextExecutionSpeedWaitState != null) {
			minimum = nextExecutionSpeedWaitState.intValue();
			nextExecutionSpeedWaitState = null;
		}
		if (duration < minimum) {
			wait(minimum-duration);
		}
	}	
		
}
