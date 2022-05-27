package com.iksgmbh.sysnat.guicontrol.impl.swing;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

public class JemmyMenuItemOperator extends AbstractJemmyOperator
{
	private final JMenuItemOperator	menuItemOperator;

	public JemmyMenuItemOperator(final ContainerOperator cont, final String name, final long timeout)
	{
		super(cont, timeout);
		menuItemOperator = new JMenuItemOperator(cont, name);
	}

	public JemmyMenuItemOperator(final ContainerOperator cont, final ComponentChooser chos, final long timeout)
	{
		super(cont, timeout);
		menuItemOperator = new JMenuItemOperator(cont, chos);
	}

	public JemmyMenuItemOperator(final ContainerOperator cont, final String name)
	{
		this(cont, name, TIMEOUT);
	}

	public JemmyMenuItemOperator(final ContainerOperator cont, final ComponentChooser chos)
	{
		this(cont, chos, TIMEOUT);
	}

	public JemmyMenuItemOperator(final ContainerOperator cont, final int item, final ComponentChooser chos)
	{
		super(cont, TIMEOUT);
		menuItemOperator = new JMenuItemOperator(cont, chos, item);
	}

	public JemmyMenuItemOperator(final ContainerOperator cont, final int item)
	{
		super(cont, TIMEOUT);
		menuItemOperator = new JMenuItemOperator(cont, item);
	}

	public void push()
	{
		try
		{
			if (!menuItemOperator.isEnabled())
			{
				throw new JemmyException("Button kann nicht gedrückt werden, da er disabled ist.");
			}

			menuItemOperator.getFocus();
			menuItemOperator.push();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error pushing main menue.", e);
		}

	}

	public boolean isEnabled()
	{
		return menuItemOperator.isEnabled();
	}

	public boolean isVisible()
	{
		return menuItemOperator.isVisible();
	}

	public JPopupMenuOperator clickForPopup()
	{
		try
		{
			menuItemOperator.clickForPopup();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error opening popup of menu item.", e);
		}
		return new JPopupMenuOperator();
	}

	public void waitEnabled(final long timeout)
	{
		int i = 0;
		while (!menuItemOperator.isEnabled() && i++ < timeout / 100)
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

		if (!menuItemOperator.isEnabled())
		{
			throw new TimeoutExpiredException("Button " + menuItemOperator.getName() + " nach " + timeout + " ms nicht enabled!");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public JMenuItemOperator getOperator() {
		return menuItemOperator;
	}

}
