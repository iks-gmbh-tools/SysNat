package com.iksgmbh.sysnat.guicontrol.impl.swing;

import java.awt.Component;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;

public class JemmyTableOperator extends AbstractJemmyOperator
{
	private final JTableOperator tableOperator;

	public JemmyTableOperator(final ContainerOperator cont, final ComponentChooser chooser, final long timeout)
	{
		super(cont, timeout);
		tableOperator = new JTableOperator(cont, chooser);
	}

	public JemmyTableOperator(final ContainerOperator cont, final String name, final long timeout)
	{
		super(cont, timeout);
		tableOperator = new JTableOperator(cont, name);
	}

	public JemmyTableOperator(final ContainerOperator cont, final ComponentChooser chooser)
	{
		this(cont, chooser, TIMEOUT);
	}

	public JemmyTableOperator(final ContainerOperator cont, final String text)
	{
		this(cont, text, TIMEOUT);
	}

	public JemmyTableOperator(final ContainerOperator cont, final int item)
	{
		super(cont, TIMEOUT);
		tableOperator = new JTableOperator(cont, item);
	}

	public void click()
	{
		try
		{
			tableOperator.clickMouse();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error clicking table.", e);
		}
	}

	public void clickForPopup()
	{
		tableOperator.clickForPopup();
	}

	public void selectAll()
	{
		tableOperator.selectAll();
	}

	public int getSelectedRow()
	{
		int rowIndex = -1;
		try
		{
			rowIndex = tableOperator.getSelectedRow();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error secting row.", e);
		}
		return rowIndex;
	}

	public void clickRow( int i, final long timeout)
	{
		if ( !waitForData(timeout) )
		{
			throw new TimeoutExpiredException("warten auf Zeile " + i + " nach " + timeout + " ms abgebrochen!");
		}
		if ( i+1 > tableOperator.getRowCount() )
		{
			throw new AssertionError("Zeile " + i + " in Tabelle nicht gefunden!");
		}
		if ( i < 0 )
		{
			if ( i < -tableOperator.getRowCount() )
			{
				throw new AssertionError("Zeile " + i + " in Tabelle nicht gefunden!");
			}
			i = tableOperator.getRowCount() + i;
		}
		clickRow(i);
	}
	
	public void clickRow(final int i)
	{
		waitForData(10000);

		try
		{
			final int model = JemmyProperties.getCurrentDispatchingModel();
			JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
			tableOperator.clickOnCell(i, 0);
			JemmyProperties.setCurrentDispatchingModel(model);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error clicking row.", e);
		}

	}

	public void clickOnCell(final int i, final int j)
	{
		waitForData(10000);

		try
		{
			final int model = JemmyProperties.getCurrentDispatchingModel();
			JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
			tableOperator.clickOnCell(i, j);
			JemmyProperties.setCurrentDispatchingModel(model);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error clicking cell.", e);
		}

	}
	
	public void clickOnCell(final int i)
	{
		waitForData(10000);
		
		int j = 0;

		try
		{
			final int model = JemmyProperties.getCurrentDispatchingModel();
			JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
			tableOperator.clickOnCell(i, j);
			JemmyProperties.setCurrentDispatchingModel(model);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error clicking cell.", e);
		}

	}

	public void doubleClickRow(final int i)
	{
		waitForData(10000);

		try
		{
			tableOperator.clickOnCell(i, 0, 2);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error clicking cell.", e);
		}

	}

	public void doubleClickSelectedRow()
	{
		int i = getSelectedRow();
		try
		{
			tableOperator.clickOnCell(i, 0, 2);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error clicking cell.", e);
		}
	}

	public boolean isEnabled()
	{

		return tableOperator.isEnabled();

	}

	public Object getValueAt(final int row, final int col)
	{
		Object res = null;
		try
		{
			res = tableOperator.getValueAt(row, col);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error reading cell.", e);
		}
		return res;
	}

	public String getStringAt(final int row, final int col, final boolean nullValuesAllowed)
	{
		try
		{
			final JLabel c = (JLabel) tableOperator.getCellRenderer(row, col).getTableCellRendererComponent(tableOperator.getTableHeader().getTable(), tableOperator.getValueAt(row, col), false, false, row, col);

			if (c.getText() != null && !c.getText().equals("") || nullValuesAllowed)
			{
				return c.getText();
			}
			else
			{
				final Object val = tableOperator.getValueAt(row, col);
				if (val != null)
				{
					return val.toString();
				}
				else
				{
					return "";
				}
			}
		}
		catch (final ClassCastException e)
		{
			final Object val = tableOperator.getValueAt(row, col);
			if (val != null)
			{
				return val.toString();
			}
			else
			{
				return "";
			}
		}

	}

	public String getStringAtNew(final int row, final int col) {
		String value = "";
			TableCellRenderer cellRenderer = tableOperator.getCellRenderer(row, col);
			Component component = cellRenderer.getTableCellRendererComponent(
					tableOperator.getTableHeader().getTable(), tableOperator.getValueAt(row, col),
					false, false, row, col);
		try {
			JLabel jLabel = (JLabel) component;
			value = jLabel.getText();
		}
		catch (final ClassCastException classCastException) {
			try {
				Method fixtureMethode = component.getClass().getMethod("getText");
				value = (String) fixtureMethode.invoke(component, (Object[]) null);
			} catch (Exception e) {
				throw new RuntimeException("Error reading cell.", e);
			}
		}
		return value;
	}

	public int getRowCount()
	{
		int res = -1;
		try
		{
			res = tableOperator.getRowCount();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error counting rows.", e);
		}
		return res;
	}

	public int getColumnCount()
	{
		int res = -1;
		try
		{
			res = tableOperator.getColumnCount();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error counting columns.", e);	
		}
		return res;
	}

	public String[] getTableHeaderData()
	{
		int cols = tableOperator.getColumnCount();
		String[] columnNames = new String[cols];
		
		for (int i = 0; i < columnNames.length; i++)
		{
			columnNames[i] = tableOperator.getColumnName(i);
		}
		return columnNames;
	}

	public int[] getTableHeaderColumnNumbers(final String[] columns)
	{
		int cols = tableOperator.getColumnCount();
		int[] columnNumbers = new int[columns.length];
		
		for (int c = 0; c < columns.length; c++)
		{
			for (int i = 0; i < cols; i++)
			{
				if (columns[c].equals( tableOperator.getColumnName(i) ))
				{
					columnNumbers[c] = i;
					
				}
			}
			
		}
		return columnNumbers;
	}

	public String getColumnHeader(String suche)
	{
		int cols = tableOperator.getColumnCount();
		
		for (int i = 0; i < cols; i++)
		{
			if (tableOperator.getColumnName(i).matches( suche ))
			{
				return tableOperator.getColumnName(i);
			}
		}
		throw new AssertionError("Header '" + suche + "' in Tabelle nicht gefunden!");
	}

	public int getColumnHeaderNumber(String suche)
	{
		int cols = tableOperator.getColumnCount();
		
		for (int i = 0; i < cols; i++)
		{
			if (tableOperator.getColumnName(i).matches( suche ))
			{
				return i;
			}
		}
		throw new AssertionError("Header '" + suche + "' in Tabelle nicht gefunden!");
	}

	public String[][] getTableData( final int[] sortedColumns, final Map<String,String> replaces )
	{
		List<List<String>> sortedTable = new ArrayList<List<String>>();

		String[][] tab = getTableData(false);

		for ( String[] row : tab )
		{
			List<String> columns = new ArrayList<String>();
			for ( String col : row )
			{
				if ( replaces != null )
				{
					for ( final String key : replaces.keySet() )
					{
						col = col.replaceAll( key, replaces.get(key) );
					}
				}
				columns.add( col );
			}
			sortedTable.add( columns );
		}

		if ( sortedColumns.length > 0 )
		{
			Collections.sort( sortedTable, new Comparator<List<String>> ()
				{
					@Override
					public int compare( List<String> left, List<String> right )
					{
						String l = "";
						String r = "";
						for ( int col : sortedColumns )
						{
							l += left.get( col );
							r += right.get( col );
						}
						return l.compareTo( r );
					}
				}
			);
		}

		String[][] sortedArray = new String[ sortedTable.size() ][ sortedTable.get(0).size() ];
		for ( int row = 0; row < sortedTable.size(); row++ )
		{
			sortedArray[ row ] = sortedTable.get( row ).toArray( new String[ sortedTable.get( row ).size() ] );
		}
		return sortedArray;
	}

	public String[][] getTableData()
	{
		return getTableData(false);
	}

	public String[][] getTableData(final boolean nullValuesAllowed)
	{
		final int rows = tableOperator.getRowCount();
		final int cols = tableOperator.getColumnCount();

		final String[][] res = new String[rows][cols];

		for (int i = 0; i < rows; i++)
		{
			for (int j = 0; j < cols; j++)
			{
				try
				{
					res[i][j] = getStringAt(i, j, nullValuesAllowed);
				}
				catch (final JemmyException e)
				{
					throw new RuntimeException("Error reading table data.", e);
				}
			}
		}

		return res;
	}

	public String[][] getTableDataNew()
	{
		final int rows = tableOperator.getRowCount();
		final int cols = tableOperator.getColumnCount();

		final String[][] res = new String[rows][cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
					res[i][j] = getStringAtNew(i, j);
			}
		}
		return res;
	}

	public void editCellValue(final Object value, final int row, final int col)
	{
		try
		{
			tableOperator.changeCellObject(row, col, value);
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error editing cell.", e);
		}
	}


	public void pushPopupMenuItem(final String menuItem)
	{
		clickTableHeaderForPopup();

		final JPopupMenuOperator pop = new JPopupMenuOperator();
		pop.pushMenuNoBlock(menuItem);
	}

	public boolean isVisible()
	{
		boolean res = false;
		try
		{
			res = tableOperator.isVisible();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error checking visibilty.", e);
		}
		return res;
	}

	public boolean waitForData(final long timeout)
	{
		try
		{
			int i = 0;
			while (tableOperator.getRowCount() == 0 && i++ < timeout / 100)
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
			if (tableOperator.getRowCount() > 0)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (final JemmyException e)
		{
			return false;
		}
	}

	public void clickTableHeaderForPopup()
	{
		try
		{
			tableOperator.createDefaultColumnsFromModel();
			tableOperator.getHeaderOperator().clickForPopup();
		}
		catch (final JemmyException e)
		{
			throw new RuntimeException("Error opening popup.", e);
		}
	}

	// TODO needed ?
//	public JComboBox getComboBoxAtCell(int row, int col) 
//	{
//		tableOperator.selectCell(row, col);
//		TableCellEditor tce = tableOperator.getCellEditor(row, col);
//		Component c = tce.getTableCellEditorComponent(new JTable(), tableOperator.getValueAt(row, col), false, row, col);
//		if (c instanceof JComboBox) {
//			return (JComboBox)c;
//		}
//		return null;
//	}
//	
	public JCheckBox getCheckBoxAtCell(int row, int col) 
	{
		TableCellEditor tce = tableOperator.getCellEditor(row, col);
		Component c = tce.getTableCellEditorComponent(new JTable(), tableOperator.getValueAt(row, col), false, row, col);
		if (c instanceof JCheckBox) {
			return (JCheckBox)c;
		}
		return null;
	}
	
	public void selectCell(int row, int col) 
	{
		tableOperator.selectCell(row, col);
	}

	public void selectRow(int rowNumber) 
	{
		int rowIndex = rowNumber-1;
		tableOperator.clickOnCell(rowIndex, 0);
	}

	public int selectRow(String searchCriteria) 
	{
		int row = search( searchCriteria ).get(0);
		if ( row != -1 )
		{
			clickRow( row );
		}
		else
		{
			throw new AssertionError("Zeile '" + searchCriteria + "' in Tabelle nicht gefunden!");
		}
		return row;
	}

	public List<String> getSelectedRow(int row) 
	{
		return Arrays.asList(getTableData()[row]);
	}

	/**
	 * 
	 * @param searchCriteria list of search conditions. e.g. "Name=Miller#Age=5"
	 * @return matching rows
	 */
	public List<Integer> search(String searchCriteria)
	{
		String[] spiltResult;
		String[] headers = getTableHeaderData();
		String[][] tableCellData = getTableData();		
		String[] criteria = searchCriteria.split("#");
		String[] columns = new String[criteria.length];
		String[] searchText = new String[criteria.length];
		boolean[] exactSearch = new boolean[criteria.length];
		List<Integer> toReturn = new ArrayList<>();

		for (int i = 0; i < criteria.length; i++) 
		{
			if ( criteria[i].contains( "==" ) )
			{
				spiltResult = criteria[i].split("=="); 
				exactSearch[i] = true;
			}
			else
			{
				spiltResult = criteria[i].split("="); 
				exactSearch[i] = false;
			}
			columns[i] = spiltResult[0];
			if (spiltResult.length == 1) 
				searchText[i] = "";
			else
				searchText[i] = spiltResult[1];
		}
		
		for (int i = 0; i < tableCellData.length; i++) 
		{
			int anzahlTreffer = 0;
			for (int j = 0; j < tableCellData[i].length; j++) 
			{
				for (int k = 0; k < criteria.length; k++) 
				{
					String colName = headers[j];
					String content = tableCellData[i][j];
					if (columns[k].equals(colName) 
						&& (exactSearch[k] && searchText[k].equals( content ) || (!exactSearch[k] && content.matches( searchText[k] ) ))) 
					{
						anzahlTreffer++;
						break;
					}
				}
			}
			if (anzahlTreffer == criteria.length)
			{
				toReturn.add(i);
			}
		}
		
		return toReturn;
	}

	public int count(String suche)
	{
		String[] suchausdruck;

		String[] tabellenueberschriften = getTableHeaderData();
		String[][] tabelleninhalt = getTableData();

		int zeilen = 0;
		
		String[] suchwerte = suche.split("#");
		String[] suchspalten = new String[suchwerte.length];
		String[] suchinhalt = new String[suchwerte.length];
		boolean[] sucheExakt = new boolean[suchwerte.length];
		for (int i = 0; i < suchwerte.length; i++) 
		{
			if ( suchwerte[i].contains( "==" ) )
			{
				suchausdruck = suchwerte[i].split("=="); 
				sucheExakt[i] = true;
			}
			else
			{
				suchausdruck = suchwerte[i].split("="); 
				sucheExakt[i] = false;
			}
			suchspalten[i] = suchausdruck[0];
			if (suchausdruck.length == 1) 
				suchinhalt[i] = "";
			else
				suchinhalt[i] = suchausdruck[1];
		}
		for (int i = 0; i < tabelleninhalt.length; i++) 
		{
			int anzahlTreffer = 0;
			for (int j = 0; j < tabelleninhalt[i].length; j++) 
			{
				for (int k = 0; k < suchwerte.length; k++) 
				{
					String spalte = tabellenueberschriften[j];
					String inhalt = tabelleninhalt[i][j];
					if (suchspalten[k].equals(spalte) && (sucheExakt[k] && suchinhalt[k].equals( inhalt ) || (!sucheExakt[k] && inhalt.matches( suchinhalt[k] ) ))) 
					{
						anzahlTreffer++;
						break;
					}
				}
			}
			if (anzahlTreffer == suchwerte.length)
			{
				zeilen++;
			}
		}
		return zeilen;
	}
	
	/**
	 * Es werden keine Spaltenname sondern nur Werte übergeben.
	 * Eine Spaltenzelle kann mehrere Unterspalten haben.
	 * Man kann eine komplette Zelle oder nur ein Teil davon suchen lassen.
	 * Z. B. man kann eine Zelle komplett und für eine andere Zeile nur ein Teil der Zelleninhalt
	 * beim Suchen übergeben.
	 *  
	 * @param werte
	 * @return
	 */
	
	public int sucheZeileDerTabelle_ZelleKannMehrereSpaltenHaben(String...werte)
	{
		int anzahlZeilen = this.getRowCount();
		
		String eineZeile[];
		int anzahlGefunden = 0;
		int zeile = -1;
		
		int anzahlSpalten  = this.getColumnCount();
		if (anzahlSpalten < werte.length)
		{   
			return -1;
		}
		
		String zeilen[][] = this.getTableData();
		
		for (int zaehler=0; zaehler < anzahlZeilen; zaehler++)
		{
			eineZeile = zeilen[zaehler];
			
			//LogUtil.log("Anzahl Spalten : " + eineZeile.length);
			anzahlGefunden = 0;
			
			for(int zaehlerSpalte=0; zaehlerSpalte < eineZeile.length; zaehlerSpalte++)
			{
				for(int zaehlerWerte=0; zaehlerWerte < werte.length; zaehlerWerte++)
				{
				  
				  if(eineZeile[zaehlerSpalte].indexOf(werte[zaehlerWerte])> -1)	
			      {
			    	anzahlGefunden ++;
			    	zeile = zaehler + 1;
			      }
			    }
			}
	
		   if(anzahlGefunden == werte.length)
		   {
			   return zeile;
		   }
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JTableOperator getOperator() {
		return tableOperator;
	}
	
}
