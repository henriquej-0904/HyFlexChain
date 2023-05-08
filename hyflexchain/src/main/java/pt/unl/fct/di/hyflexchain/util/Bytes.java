package pt.unl.fct.di.hyflexchain.util;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * An interface for converting an object to bytes and vice versa.
 */
public interface Bytes<T> {
	
	// ByteBuffer toBytes();

	void applyToBytes(Consumer<ByteBuffer> apply);

	static <T extends Bytes<T>> void applyToBytes(T[] array, Consumer<ByteBuffer> apply)
	{
		for (T t : array)
			t.applyToBytes(apply);
	}

	

}
 