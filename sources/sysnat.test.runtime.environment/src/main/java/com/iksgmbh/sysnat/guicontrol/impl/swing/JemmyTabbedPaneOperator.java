package com.iksgmbh.sysnat.guicontrol.impl.swing;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;

public class JemmyTabbedPaneOperator extends AbstractJemmyOperator
{
	private final JTabbedPaneOperator operator;

	public JemmyTabbedPaneOperator(final ContainerOperator cont, final ComponentChooser chos, final long timeout)
	{
		super(cont, timeout);
		operator = new JTabbedPaneOperator(cont, chos);
	}
	
	public JemmyTabbedPaneOperator(final ContainerOperator cont, final String name, final long timeout)
	{
		super(cont, timeout);
		operator = new JTabbedPaneOperator(cont, name);
	}

	public JemmyTabbedPaneOperator(final ContainerOperator cont, final String name)
	{
		this(cont, name, TIMEOUT);
	}
	
	public JemmyTabbedPaneOperator(final ContainerOperator cont, final ComponentChooser chos)
	{
		this(cont, chos, TIMEOUT);
	}
	
	public JemmyTabbedPaneOperator(ContainerOperator cont, int item)
	{
		super(cont, TIMEOUT);
		operator = new JTabbedPaneOperator(cont, item);
	}
	
	public JemmyTabbedPaneOperator(ContainerOperator cont, long timeout, int item)
	{
		super(cont, timeout);
		operator = new JTabbedPaneOperator(cont, item);
	}

	public void gotoTab(String tabName) {
		operator.selectPage(tabName);
	}

	public void gotoTab(int index) {
		operator.selectPage(index);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JTabbedPaneOperator getOperator() {
		return operator;
	}

}
