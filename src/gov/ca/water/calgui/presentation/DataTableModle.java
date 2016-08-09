package gov.ca.water.calgui.presentation;

import javax.swing.table.AbstractTableModel;

import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;

public class DataTableModle extends AbstractTableModel {
	private IAuditSvc auditSvc = AuditSvcImpl.getAuditSvcImplInstance();
	private String tableName = "";
	private String[] columnNames;
	private Object[][] data;
	boolean isCellEditable;

	public DataTableModle(String tableName, String[] columnName, Object[][] data, boolean isCellEditable) {
		this.tableName = tableName;
		this.columnNames = columnName;
		this.data = data;
		this.isCellEditable = isCellEditable;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		String tableName = this.tableName;
		String[] colNames = new String[this.columnNames.length];
		for (int i = 0; i < colNames.length; i++) {
			colNames[i] = this.columnNames[i];
		}
		Object[][] data1 = new Object[this.data.length][this.data[0].length];
		for (int i = 0; i < data1.length; i++) {
			for (int j = 0; j < data1[0].length; j++) {
				data1[i][j] = this.data[i][j];
			}
		}
		return new DataTableModle(tableName, colNames, data1, this.isCellEditable);
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public Object[][] getData() {
		return data;
	}

	public void setData(Object[][] data) {
		this.data = data;
	}

	public boolean isCellEditable() {
		return isCellEditable;
	}

	public void setCellEditable(boolean isCellEditable) {
		this.isCellEditable = isCellEditable;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return data.length;
	}

	@Override
	public Object getValueAt(int r, int c) {
		return data[r][c];
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return this.isCellEditable;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		auditSvc.addAudit(tableName + Constant.DASH + row + Constant.DASH + col, String.valueOf(getValueAt(row, col)),
		        String.valueOf(value));
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

	@Override
	public String getColumnName(int columnIndex) {
		String colName = "";
		if (columnIndex <= getColumnCount())
			colName = columnNames[columnIndex];
		return colName;
	}

	@Override
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	// TODO remove the following method after development is completed.
	public void print() {
		for (int i = 0; i < columnNames.length; i++) {
			System.out.print(columnNames[i] + " ");
		}
		System.out.println();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[0].length; j++) {
				System.out.print(data[i][j] + " ");
			}
			System.out.println();
		}
	}
}
