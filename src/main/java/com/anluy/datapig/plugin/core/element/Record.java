package com.anluy.datapig.plugin.core.element;

/**
 * Created by jingxing on 14-8-24.
 */

public interface Record {

	public void addColumn(Column column);

	public void setColumn(int i, final Column column);

	public Column getColumn(int i);

	@Override
	public String toString();

	public int getColumnNumber();

	public int getByteSize();

	public int getMemorySize();

}
