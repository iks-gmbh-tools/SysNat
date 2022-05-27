package com.iksgmbh.sysnat.guicontrol.impl.swing;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComponentOperator;

public class JemmyComponentOperator extends AbstractJemmyOperator
{
	private JComponentOperator componentOperator;


	public JemmyComponentOperator(final ContainerOperator cont, final ComponentChooser chooser, final long timeout)
	{
		super(cont, timeout);
		componentOperator = new JComponentOperator(cont, chooser);
	}

	public JemmyComponentOperator(final ContainerOperator cont, final ComponentChooser chooser)
	{
		super(cont, TIMEOUT);
		componentOperator = new JComponentOperator(cont, chooser);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JComponentOperator getOperator() {
		return componentOperator;
	}
}
