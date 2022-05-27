package com.iksgmbh.sysnat.guicontrol.impl.swing;

import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.ContainerOperator;

public abstract class AbstractJemmyOperator
{
	public static final long TIMEOUT = 5000;
	
	public AbstractJemmyOperator(ContainerOperator cont, long timeout) {
		setTimeout(timeout, cont, "ComponentOperator.WaitComponentTimeout");
	}

	protected static Timeouts setTimeout(long timeout, 
			                             ContainerOperator cont, 
			                             String timeoutName)
	{
		final Timeouts oldTimes = cont.getTimeouts().cloneThis();
		final Timeouts newTimes = cont.getTimeouts().cloneThis();
		newTimes.setTimeout(timeoutName, timeout);
		cont.setTimeouts(newTimes);
		return oldTimes;
	}
	
	abstract public <T extends ContainerOperator> T getOperator();

}
