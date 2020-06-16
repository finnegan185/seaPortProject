/* File: CustomTableModel.java
 * Date: 11/30/2019
 * Author: Zachary Finnegan
 * Purpose: Used to create the SeaPortPrograms custom tables. Makes the tables
 * interactive and continuously updating. Also ensures JSwing components are 
 * rendered properly.
 */

package seaPortProject;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class CustomTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	public CustomTableModel(Object[][] dataObjects, Object[] headers) {
		super(dataObjects, headers);
	}
	
	public Class<?> getColumnClass(int col){
		return getValueAt(0, col).getClass();
	}
	
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	@Override
	public void setValueAt(Object aValue, int row, int col) {
	        this.fireTableCellUpdated(row, col);
	}
}

class CustomCellRenderer implements TableCellRenderer, ActionListener{
	private Timer timer = new Timer(10, this);
	private JTable table;
	
	public CustomCellRenderer() {
		
	}
	public CustomCellRenderer(JTable table) {
		this.table = table;
		timer.start();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		TableModel model = table.getModel();
		if(model.getColumnCount() == 3) {
			for (int row = 0; row < model.getRowCount(); row++) {
	            table.getModel().setValueAt(0, row, 1);
	            table.getModel().setValueAt(0, row, 2);
			}
		} else {
			for (int row = 0; row < model.getRowCount(); row++) {
	            table.getModel().setValueAt(0, row, 3);
	            table.getModel().setValueAt(0, row, 4);
	            table.getModel().setValueAt(0, row, 5);
	            table.getModel().setValueAt(0, row, 6);
	            table.getModel().setValueAt(0, row, 7);
	            table.getModel().setValueAt(0, row, 8);
			}
        }
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int col) {
		return (Component) value;
	}
		
}

class MouseListener extends MouseAdapter {
    private JTable table;
    
    public MouseListener(JTable table) {
        this.table = table;
    } 
    
	@Override
    public void mouseClicked(MouseEvent e) {
        int col = table.getColumnModel().getColumnIndexAtX(e.getX());
        int row = e.getY() / table.getRowHeight();
        if (row < table.getRowCount() && row >= 0 && col < table.getColumnCount() && col >= 0) {
            Object value = table.getValueAt(row, col);
            if (value instanceof JButton) {
                ((JButton) value).doClick();
            }             
        }
    }
}
