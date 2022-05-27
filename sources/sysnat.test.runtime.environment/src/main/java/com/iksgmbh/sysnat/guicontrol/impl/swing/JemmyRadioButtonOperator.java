package com.iksgmbh.sysnat.guicontrol.impl.swing;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;

public class JemmyRadioButtonOperator extends AbstractJemmyOperator
{
	private final JRadioButtonOperator radioButtonOperator;

	public JemmyRadioButtonOperator(final ContainerOperator cont, final String name, final long timeout)
	{
		super(cont, timeout);
		radioButtonOperator = new JRadioButtonOperator(cont, name);
	}

	public JemmyRadioButtonOperator(final ContainerOperator cont, final ComponentChooser chos, final long timeout)
	{
		super(cont, timeout);
		radioButtonOperator = new JRadioButtonOperator(cont, chos);
	}

	public JemmyRadioButtonOperator(final ContainerOperator cont, final String name)
	{
		this(cont, name, TIMEOUT);
	}

	public JemmyRadioButtonOperator(final ContainerOperator cont, final ComponentChooser chos)
	{
		this(cont, chos, TIMEOUT);
	}

	public JemmyRadioButtonOperator(final ContainerOperator cont, final int item, final ComponentChooser chos)
	{
		super(cont, TIMEOUT);
		radioButtonOperator = new JRadioButtonOperator(cont, chos, item);
	}

	public JemmyRadioButtonOperator(final ContainerOperator cont, final int item)
	{
		super(cont, TIMEOUT);
		radioButtonOperator = new JRadioButtonOperator(cont, item);
	}

	public void push()
	{
		try
		{
			if (!radioButtonOperator.isEnabled())
			{
				throw new JemmyException("Button kann nicht gedrückt werden, da er disabled ist.");
			}

			radioButtonOperator.getFocus();
			radioButtonOperator.push();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error cklicking button.", e);
		}

	}

	public boolean isEnabled()
	{
		return radioButtonOperator.isEnabled();
	}

	public boolean isVisible()
	{
		return radioButtonOperator.isVisible();
	}

	public void click()
	{
		try
		{
			radioButtonOperator.clickMouse();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error opening button popup.", e);
		}
	}

	public void waitEnabled(final long timeout)
	{
		int i = 0;
		while (!radioButtonOperator.isEnabled() && i++ < timeout / 100)
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

		if (!radioButtonOperator.isEnabled())
		{
			throw new TimeoutExpiredException("Checkbox " + radioButtonOperator.getName() + " nach " + timeout + " ms nicht enabled!");
		}
	}
	
	public boolean isSelected()
	{
		return radioButtonOperator.isSelected();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JRadioButtonOperator getOperator() {
		return radioButtonOperator;
	}

	
}
