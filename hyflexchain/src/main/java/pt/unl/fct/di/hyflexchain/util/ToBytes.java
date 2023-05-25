package pt.unl.fct.di.hyflexchain.util;

import java.nio.ByteBuffer;

public interface ToBytes {
	/**
	 * Convert this object to bytes.
	 * @param buff The buffer to place the resulting bytes.
	 * @return The buffer
	 */
	public ByteBuffer toBytes(ByteBuffer buff);
}
