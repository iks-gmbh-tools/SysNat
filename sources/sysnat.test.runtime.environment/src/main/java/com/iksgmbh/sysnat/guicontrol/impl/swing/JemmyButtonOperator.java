package com.iksgmbh.sysnat.guicontrol.impl.swing;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

public class JemmyButtonOperator extends AbstractJemmyOperator
{
	private final JButtonOperator buttonOperator;

	public JemmyButtonOperator(final ContainerOperator cont, final String name, final long timeout)
	{
		super(cont, timeout);
		buttonOperator = new JButtonOperator(cont, name);
	}

	public JemmyButtonOperator(final ContainerOperator cont, final ComponentChooser chos, final long timeout)
	{
		super(cont, timeout);
		buttonOperator = new JButtonOperator(cont, chos);
	}

	public JemmyButtonOperator(final ContainerOperator cont, final String name)
	{
		this(cont, name, TIMEOUT);
	}

	public JemmyButtonOperator(final ContainerOperator cont, final ComponentChooser chos)
	{
		this(cont, chos, TIMEOUT);
	}

	public JemmyButtonOperator(final ContainerOperator cont, final int item, final ComponentChooser chos)
	{
		super(cont, TIMEOUT);
		buttonOperator = new JButtonOperator(cont, chos, item);
	}

	public JemmyButtonOperator(final ContainerOperator cont, final int item)
	{
		super(cont, TIMEOUT);
		buttonOperator = new JButtonOperator(cont, item);
	}

	public void push()
	{
		try
		{
			if (!buttonOperator.isEnabled())
			{
				throw new JemmyException("Button kann nicht gedrückt werden, da er disabled ist.");
			}

			buttonOperator.getFocus();
			buttonOperator.push();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error cklicking button.", e);
		}

	}

	public boolean isEnabled()
	{
		return buttonOperator.isEnabled();
	}

	public boolean isVisible()
	{
		return buttonOperator.isVisible();
	}

	public JPopupMenuOperator clickForPopup()
	{
		try
		{
			buttonOperator.clickForPopup();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error opening button popup.", e);
		}
		return new JPopupMenuOperator();
	}

	public void waitEnabled(final long timeout)
	{
		int i = 0;
		while (!buttonOperator.isEnabled() && i++ < timeout / 100)
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

		if (!buttonOperator.isEnabled())
		{
			throw new TimeoutExpiredException("Button " + buttonOperator.getName() + " nach " + timeout + " ms nicht enabled!");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JButtonOperator getOperator() {
		return buttonOperator;
	}
	
}
