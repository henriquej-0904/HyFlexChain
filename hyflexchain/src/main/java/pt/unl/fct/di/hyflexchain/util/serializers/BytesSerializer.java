package pt.unl.fct.di.hyflexchain.util.serializers;

import java.nio.ByteBuffer;

import pt.unl.fct.di.hyflexchain.util.result.Result;

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

	default ByteBuffer serialize(T obj)
	{
		return serialize(obj,
			ByteBuffer.allocate(serializedSize(obj))
		).position(0);
	}

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

	/**
	 * Get a serializer for a Result. Due to type erasure
	 * it is not possible to return a serializer for the specified
	 * generic types. Although, it is guaranteed that as long as
	 * the caller uses the serializer with the same types used to
	 * construct it, then there is no problem.
	 * @param <Ok> The type of a Ok result
	 * @param <Failed> The type of a Failed result
	 * @param okSerializer The serializer fot the Ok result
	 * @param failedSerializer The serializer for the Failed result
	 * @return A result Serializer
	 */
	static <Ok, Failed> BytesSerializer<Result> resultSerializer(
		BytesSerializer<Ok> okSerializer, BytesSerializer<Failed> failedSerializer
	)
	{
		return new ResultSerializer<>(okSerializer, failedSerializer);
	}
}
