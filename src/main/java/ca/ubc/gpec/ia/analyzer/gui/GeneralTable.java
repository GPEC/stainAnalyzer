/**
 * GeneralTable.java
 *
 * Created on August 17, 2005 by Andy Chan
 *
 * Extends the JTable to enable ability to display any JComponent in the cells.
 *
 * Acknowledgments: The idea and some source code of this class comes from the CodeGuru Java tutorial
 * "Display any JComponent in a cell" by Zafir Anjum. Please refer to http://www.codeguru.com/java/articles/162.shtml.
 */

package ca.ubc.gpec.ia.analyzer.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.Serializable;
import java.util.EventObject;
import java.awt.event.*;

public class GeneralTable extends JTable {
    /**
     * Constructor allowing specification of the data and column names
     *
     * @param   rowData The data for the table
     * @param   columnNames Names of each column
     */     
    public GeneralTable (Object [][] rowData, Object [] columnNames) {
        super(rowData, columnNames);
        setProperties(); // Set table properties
    }
    
    /**
     * Sets the width of a particular column
     *
     * @param   col The column to be set 
     * @param   width   The width to be used
     */       
    public void setColumnWidth (int col, int width) {
        TableColumn column = getColumnModel().getColumn(col);
        column.setPreferredWidth(width);        
    }
    
    /**
     * Retrieve the value of a particular cell of the table
     *
     * @param   row The row number of the cell
     * @param   col The column number of the cell
     * @return  An object that represents the value as returned by the corresponding JComponent
     */        
    public Object getValue(int row, int col) {
        Object value = null;
        Object o = getValueAt(row, col);
        String className = o.getClass().getName();
        if (className.equals("javax.swing.JTextField")) {
            JTextField textField = (JTextField)o;
            value = textField.getText();
        }
        else if (className.equals("javax.swing.JCheckBox")) {
            JCheckBox checkBox = (JCheckBox)o;
            value = new Boolean(checkBox.isSelected());
        }
        else if (className.equals("javax.swing.JComboBox")) {
            JComboBox comboBox = (JComboBox)o;
            value = comboBox.getSelectedItem();
        }        
        
        return value;
    }
    
    /**
     * Sets the properties of the table
     */     
    private void setProperties () {
        setDefaultRenderer(JComponent.class, new JComponentCellRenderer());
        setDefaultEditor(JComponent.class, new JComponentCellEditor());
    }    
    
    // Method written by Zafir Anjum
    public TableCellRenderer getCellRenderer(int row, int column) {
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellRenderer renderer = tableColumn.getCellRenderer();
        if (renderer == null) {
            Class c = getColumnClass(column);
            if( c.equals(Object.class) ) {
                Object o = getValueAt(row,column);
                if( o != null )
                    c = getValueAt(row,column).getClass();
            }
            renderer = getDefaultRenderer(c);
        }
        return renderer;
    }
    
    // Method written by Zafir Anjum
    public TableCellEditor getCellEditor(int row, int column) {
        TableColumn tableColumn = getColumnModel().getColumn(column);
        TableCellEditor editor = tableColumn.getCellEditor();
        if (editor == null) {
            Class c = getColumnClass(column);
            if( c.equals(Object.class) ) {
                Object o = getValueAt(row,column);
                if( o != null )
                    c = getValueAt(row,column).getClass();
            }
            editor = getDefaultEditor(c);
        }
        return editor;
    } 
}

// Note: The entire following section contains source code written by Zafir Anjum
class JComponentCellRenderer implements TableCellRenderer
{
    public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {
        return (JComponent)value;
    }
}

class JComponentCellEditor implements TableCellEditor, TreeCellEditor, Serializable {
	protected EventListenerList listenerList = new EventListenerList();
	transient protected ChangeEvent changeEvent = null;
	
	protected JComponent editorComponent = null;
	protected JComponent container = null;		// Can be tree or table
	
	
	public Component getComponent() {
		return editorComponent;
	}
	
	
	public Object getCellEditorValue() {
		return editorComponent;
	}
	
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}
	
	public boolean shouldSelectCell(EventObject anEvent) {
		if( editorComponent != null && anEvent instanceof MouseEvent
			&& ((MouseEvent)anEvent).getID() == MouseEvent.MOUSE_PRESSED )
		{
            Component dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent, 3, 3 );
			MouseEvent e = (MouseEvent)anEvent;
			MouseEvent e2 = new MouseEvent( dispatchComponent, MouseEvent.MOUSE_RELEASED,
				e.getWhen() + 100000, e.getModifiers(), 3, 3, e.getClickCount(),
				e.isPopupTrigger() );
			dispatchComponent.dispatchEvent(e2); 
			e2 = new MouseEvent( dispatchComponent, MouseEvent.MOUSE_CLICKED,
				e.getWhen() + 100001, e.getModifiers(), 3, 3, 1,
				e.isPopupTrigger() );
			dispatchComponent.dispatchEvent(e2); 
		}
		return false;
	}
	
	public boolean stopCellEditing() {
		fireEditingStopped();
		return true;
	}
	
	public void cancelCellEditing() {
		fireEditingCanceled();
	}
	
	public void addCellEditorListener(CellEditorListener l) {
		listenerList.add(CellEditorListener.class, l);
	}
	
	public void removeCellEditorListener(CellEditorListener l) {
		listenerList.remove(CellEditorListener.class, l);
	}
	
	protected void fireEditingStopped() {
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==CellEditorListener.class) {
				// Lazily create the event:
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((CellEditorListener)listeners[i+1]).editingStopped(changeEvent);
			}	       
		}
	}
	
	protected void fireEditingCanceled() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==CellEditorListener.class) {
				// Lazily create the event:
				if (changeEvent == null)
					changeEvent = new ChangeEvent(this);
				((CellEditorListener)listeners[i+1]).editingCanceled(changeEvent);
			}	       
		}
	}
	
	// implements javax.swing.tree.TreeCellEditor
	public Component getTreeCellEditorComponent(JTree tree, Object value,
		boolean isSelected, boolean expanded, boolean leaf, int row) {
		String         stringValue = tree.convertValueToText(value, isSelected,
			expanded, leaf, row, false);
		
		editorComponent = (JComponent)value;
		container = tree;
		return editorComponent;
	}
	
	// implements javax.swing.table.TableCellEditor
	public Component getTableCellEditorComponent(JTable table, Object value,
		boolean isSelected, int row, int column) {
		
		editorComponent = (JComponent)value;
		container = table;
		return editorComponent;
	}
}
