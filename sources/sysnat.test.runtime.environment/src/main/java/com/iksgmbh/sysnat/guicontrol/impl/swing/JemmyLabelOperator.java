package com.iksgmbh.sysnat.guicontrol.impl.swing;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JLabelOperator;

public class JemmyLabelOperator extends AbstractJemmyOperator
{
	private final JLabelOperator labelOperator;

	public JemmyLabelOperator(final ContainerOperator cont, final String textFromLabelBefore, final long timeout)
	{
		super(cont, timeout);
		labelOperator = findLabelByLabelTextBefore(cont, textFromLabelBefore);
	}

	public JemmyLabelOperator(final ContainerOperator cont, final ComponentChooser chos, final long timeout)
	{
		super(cont, timeout);
		labelOperator = new JLabelOperator(cont, chos);
	}

	public JemmyLabelOperator(final ContainerOperator cont, final String textFromLabelBefore)
	{
		this(cont, textFromLabelBefore, TIMEOUT);
	}

	
	public JemmyLabelOperator(final ContainerOperator cont, final ComponentChooser chos)
	{
		this(cont, chos, TIMEOUT);
	}

	public JemmyLabelOperator(final ContainerOperator cont, final int item)
	{
		super(cont, TIMEOUT);
		labelOperator = new JLabelOperator(cont, item);
	}

	
	public String getText()
	{
		return labelOperator.getText();
	}

	public Icon getIcon()
	{
		return labelOperator.getIcon();
	}

	public String getToolTipText()
	{
		return labelOperator.getToolTipText();
	}
	
	public boolean isEnabled()
	{
		return labelOperator.isEnabled();
	}

	private JLabelOperator findLabelByLabelTextBefore(final ContainerOperator cont, final String text)
	{
		JLabelOperator labelOperator = new JLabelOperator(cont, text);
		Component comp = labelOperator.getSource();
		
		Component[] comps = labelOperator.getParent().getComponents();
		for (int i = 0; i < comps.length; i++)
			if (comps[i].equals(comp))
					return new JLabelOperator( (JLabel)comps[i+1] );

		throw new RuntimeException("Label zum vorangestelltem Label mit Text '" + 
				text + "' nicht gefunden.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public JLabelOperator getOperator() {
		return labelOperator;
	}
	
}
