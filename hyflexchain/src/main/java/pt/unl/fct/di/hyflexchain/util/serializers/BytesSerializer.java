package pt.unl.fct.di.hyflexchain.util.serializers;

import java.nio.ByteBuffer;

public interface BytesSerializer<T>
{
	/**
	 * The type of the element in this serializer.
	 * @return type of the element in this serializer.
	 */
	Class<T> getType();

	/**
	 * Calculate the size of the serialized object in bytes.
	 * @param obj
	 * @return the size of the serialized object in bytes.
	 */
	int serializedSize(T obj);

	/**
	 * Convert this object to bytes.
	 * @param obj The object to serialize
	 * @param buff The buffer to place the resulting bytes.
	 * @return The buffer
	 */
	ByteBuffer serialize(T obj, ByteBuffer buff);

	/**
	 * Convert bytes to an object
	 * @param buff The buffer to place the resulting bytes.
	 * @return The object
	 */
	T deserialize(ByteBuffer buff);

	/**
	 * Create a primitive array serializer.
	 * @param <T> The primitive array type (ex: byte[].class, int[].class, etc.)
	 * @param primitiveArrayClass The primitive array class
	 * @return The primitive array serializer.
	 */
	static <T> BytesSerializer<T> primitiveArraySerializer(Class<T> primitiveArrayClass)
	{
		return ArraysBytesSerializer.primitiveSerializer(primitiveArrayClass);
	}

	/**
	 * Create an array serializer based on this serializer
	 * @return An array serializer of the type of this serializer.
	 */
	default BytesSerializer<T[]> toArraySerializer()
	{
		return ArraysBytesSerializer.genericSerializer(getType(), this);
	}
}
