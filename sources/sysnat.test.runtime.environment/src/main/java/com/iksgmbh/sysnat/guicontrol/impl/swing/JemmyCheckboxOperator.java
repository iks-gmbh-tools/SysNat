package com.iksgmbh.sysnat.guicontrol.impl.swing;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;

public class JemmyCheckboxOperator extends AbstractJemmyOperator
{
	private final JCheckBoxOperator checkboxOperator;

	public JemmyCheckboxOperator(final ContainerOperator cont, final String name, final long timeout)
	{
		super(cont, timeout);
		checkboxOperator = new JCheckBoxOperator(cont, name);
	}

	public JemmyCheckboxOperator(final ContainerOperator cont, final ComponentChooser chos, final long timeout)
	{
		super(cont, timeout);
		checkboxOperator = new JCheckBoxOperator(cont, chos);
	}

	public JemmyCheckboxOperator(final ContainerOperator cont, final String name)
	{
		this(cont, name, TIMEOUT);
	}

	public JemmyCheckboxOperator(final ContainerOperator cont, final ComponentChooser chos)
	{
		this(cont, chos, TIMEOUT);
	}

	public JemmyCheckboxOperator(final ContainerOperator cont, final int item, final ComponentChooser chos)
	{
		super(cont, TIMEOUT);
		checkboxOperator = new JCheckBoxOperator(cont, chos, item);
	}

	public JemmyCheckboxOperator(final ContainerOperator cont, final int item)
	{
		super(cont, TIMEOUT);
		checkboxOperator = new JCheckBoxOperator(cont, item);
	}

	public void push()
	{
		try
		{
			if (!checkboxOperator.isEnabled())
			{
				throw new JemmyException("Button kann nicht gedrückt werden, da er disabled ist.");
			}

			checkboxOperator.getFocus();
			checkboxOperator.push();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error cklicking button.", e);
		}

	}

	public boolean isEnabled()
	{
		return checkboxOperator.isEnabled();
	}

	public boolean isVisible()
	{
		return checkboxOperator.isVisible();
	}

	public void click()
	{
		try
		{
			checkboxOperator.clickMouse();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error opening button popup.", e);
		}
	}

	public void waitEnabled(final long timeout)
	{
		int i = 0;
		while (!checkboxOperator.isEnabled() && i++ < timeout / 100)
		{
			try
			{
				Thread.sleep(100);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}

		if (!checkboxOperator.isEnabled())
		{
			throw new TimeoutExpiredException("Checkbox " + checkboxOperator.getName() + " nach " + timeout + " ms nicht enabled!");
		}
	}
	
	public boolean isSelected()
	{
		return checkboxOperator.isSelected();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JCheckBoxOperator getOperator() {
		return checkboxOperator;
	}

	
}
