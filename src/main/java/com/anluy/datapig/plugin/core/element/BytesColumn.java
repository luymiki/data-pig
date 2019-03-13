package com.anluy.datapig.plugin.core.element;

import com.anluy.datapig.plugin.core.DataPigException;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by jingxing on 14-8-24.
 */
public class BytesColumn extends Column {

	public BytesColumn() {
		this(null);
	}

	public BytesColumn(byte[] bytes) {
		super(ArrayUtils.clone(bytes), Column.Type.BYTES, null == bytes ? 0
				: bytes.length);
	}

	@Override
	public byte[] asBytes() {
		if (null == this.getRawData()) {
			return null;
		}

		return (byte[]) this.getRawData();
	}

	@Override
	public String asString() {
		if (null == this.getRawData()) {
			return null;
		}

		try {
			return ColumnCast.bytes2String(this);
		} catch (Exception e) {
			throw new DataPigException(
					String.format("Bytes[%s]不能转为String .", this.toString()));
		}
	}

	@Override
	public Long asLong() {
		throw new DataPigException( "Bytes类型不能转为Long .");
	}

	@Override
	public BigDecimal asBigDecimal() {
		throw new DataPigException( "Bytes类型不能转为BigDecimal .");
	}

	@Override
	public BigInteger asBigInteger() {
		throw new DataPigException( "Bytes类型不能转为BigInteger .");
	}

	@Override
	public Object asObject() {
		return asBytes();
	}

	@Override
	public Double asDouble() {
		throw new DataPigException( "Bytes类型不能转为Long .");
	}

	@Override
	public Date asDate() {
		throw new DataPigException( "Bytes类型不能转为Date .");
	}

	@Override
	public Boolean asBoolean() {
		throw new DataPigException( "Bytes类型不能转为Boolean .");
	}
}
