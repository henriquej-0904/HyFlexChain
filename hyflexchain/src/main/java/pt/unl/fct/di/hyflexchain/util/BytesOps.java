package pt.unl.fct.di.hyflexchain.util;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;

import io.netty.buffer.Unpooled;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public interface BytesOps {
	
	/**
	 * Get the size in bytes required for the serialization of this object. 
	 * @return
	 */
	int serializedSize();

	static int serializedSize(String s)
	{
		return Integer.BYTES + s.getBytes().length;
	}

	static int serializedSize(byte[] array)
	{
		return Integer.BYTES + array.length;
	}

	static int serializedSize(Bytes bytes)
	{
		return Integer.BYTES + bytes.size();
	}

	static <T extends BytesOps> int serializedSize(T[] array)
	{
		return Integer.BYTES + Stream.of(array)
			.mapToInt(T::serializedSize)
			.sum();
	}

	static <T extends BytesOps> int serializedSize(Collection<T> coll)
	{
		return Integer.BYTES + coll.stream()
			.mapToInt(T::serializedSize)
			.sum();
	}

	static <T extends BytesOps> byte[] serialize(T obj, ISerializer<T> serializer) throws IOException
	{
		byte[] data = new byte[obj.serializedSize()];
		serializer.serialize(obj, Unpooled.wrappedBuffer(data).setIndex(0, 0));
		return data;
	}

}
 