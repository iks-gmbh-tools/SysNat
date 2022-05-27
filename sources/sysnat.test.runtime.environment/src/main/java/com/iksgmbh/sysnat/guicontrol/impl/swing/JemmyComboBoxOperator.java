package com.iksgmbh.sysnat.guicontrol.impl.swing;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

@SuppressWarnings("rawtypes")
public class JemmyComboBoxOperator extends AbstractJemmyOperator
{

	protected JComboBoxOperator	comboBoxOperator;

	public JemmyComboBoxOperator(final ContainerOperator cont, final ComponentChooser chooser, final long timeout)
	{
		super(cont, timeout);
		comboBoxOperator = new JComboBoxOperator(cont, chooser);
	}

	public JemmyComboBoxOperator(final ContainerOperator cont, final String name, final long timeout)
	{
		super(cont, timeout);
		comboBoxOperator = new JComboBoxOperator(cont, name);
	}

	public JemmyComboBoxOperator(final ContainerOperator cont, final ComponentChooser chooser)
	{
		this(cont, chooser, TIMEOUT);
	}

	public JemmyComboBoxOperator(final ContainerOperator cont, final String name)
	{
		this(cont, name, TIMEOUT);
	}

	public JemmyComboBoxOperator(final ContainerOperator cont, final int item)
	{
		super(cont, TIMEOUT);
		comboBoxOperator = new JComboBoxOperator(cont, item);
	}

	public JemmyComboBoxOperator(final JComboBox comboBox, final ContainerOperator cont)
	{
		super(cont, TIMEOUT);
		this.comboBoxOperator = new JComboBoxOperator(comboBox);
	}

	public boolean isEnabled()
	{
		return comboBoxOperator.isEnabled();
	}

	public boolean isEditable()
	{
		return comboBoxOperator.isEditable();
	}

	@SuppressWarnings("unchecked")
	public void selectItem(final String item)
	{
		try
		{
			comboBoxOperator.getFocus();;
			for (int i = 0; i < comboBoxOperator.getItemCount(); i++)
			{
				final Component c = comboBoxOperator.getRenderer().getListCellRendererComponent(new JList(), comboBoxOperator.getItemAt(i), i, false, false);
				System.err.println(item + " - " + ((JLabel) c).getText());
				if (item.equals(((JLabel) c).getText()))
				{
					comboBoxOperator.selectItem(i);
					return;
				}
			}
			comboBoxOperator.selectItem(item);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error selecting item.", e);
		}
	}


	public void selectItem(final int item)
	{
		try
		{
			comboBoxOperator.getFocus();
			comboBoxOperator.selectItem(item);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error selecting item.", e);
		}
	}

	public void typeText(final String text)
	{
		try
		{
			comboBoxOperator.getFocus();
			comboBoxOperator.typeText(text);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error selecting item.", e);
		}
	}

	public int getItemCount()
	{
		return comboBoxOperator.getItemCount();
	}

	public boolean isVisible()
	{
		return comboBoxOperator.isVisible();
	}

	@SuppressWarnings("unchecked")
	public String getSelectedItem()
	{
		try
		{
			final int index = comboBoxOperator.getSelectedIndex();
			final Component c = comboBoxOperator.getRenderer().getListCellRendererComponent(new JList(), comboBoxOperator.getItemAt(index), index, false, false);
			return ((JLabel) c).getText();
		}
		catch (final Exception e)
		{
			try
			{
				return (String) comboBoxOperator.getSelectedItem();
			}
			catch (final ClassCastException e1)
			{
				return String.valueOf(comboBoxOperator.getSelectedItem());
			}
		}
	}
	
	public Object getItemAt (int index)
	{
		return comboBoxOperator.getItemAt(index);
	}
	
	public String getName()
	{
		return comboBoxOperator.getName();
	}
	
	public Object getSelectedItemObject()
	{
		return comboBoxOperator.getSelectedItem();
	}
	
	public Component[] getComponents()
	{
		return comboBoxOperator.getComponents();
	}

	public String getComboBoxLabelText() {
		Component[] components = getComponents();
		Container container = (Container) components[1];
		Component component = container.getComponents()[0];
		return new JLabelOperator((JLabel) component).getText();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JComboBoxOperator getOperator() {
		return comboBoxOperator;
	}

	
}
