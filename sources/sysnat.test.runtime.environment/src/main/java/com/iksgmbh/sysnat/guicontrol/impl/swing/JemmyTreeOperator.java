package com.iksgmbh.sysnat.guicontrol.impl.swing;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

public class JemmyTreeOperator extends AbstractJemmyOperator
{
	private final JTreeOperator operator;

	public JemmyTreeOperator(final ContainerOperator cont, final ComponentChooser chooser, final long timeout)
	{
		super(cont, timeout);
		operator = new JTreeOperator(cont, chooser);
	}

	public JemmyTreeOperator(final ContainerOperator cont, final ComponentChooser chos)
	{
		this(cont, chos, TIMEOUT);
	}

	public JemmyTreeOperator(final ContainerOperator cont, final int item)
	{
		super(cont, TIMEOUT);
		operator = new JTreeOperator(cont, item);
	}

	public void selectRow(final int i)
	{
		try
		{
			operator.setSelectionRow(i);
//			operator.selectRow(i);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error handling tree: " +  operator.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JTreeOperator getOperator() {
		return operator;
	}
	
}
