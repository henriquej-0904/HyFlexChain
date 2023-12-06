package pt.unl.fct.di.hyflexchain.util.serializer;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Represents a serializer/deserializer that writes/reads objects to/from a byte buffer
 *
 * @param <T> The type of the object which this class serializes
 */
public interface ISerializer<T> {

    /**
     * Serializes the received object into the received byte buffer.
     *
     * @param t   The object to serialize
     * @param out The byte buffer to which the object will the written
     * @throws IOException if the serialization fails
     */
    void serialize(T t, ByteBuf out) throws IOException;

    /**
     * Deserializes an object from a byte buffer and returns it.
     *
     * @param in The byte buffer which contains the object to be deserialized
     * @return The deserialized object
     * @throws IOException if the deserialization fails
     */
    T deserialize(ByteBuf in) throws IOException;

    /**
     * Deserializes an object from a byte array and returns it.
     *
     * @param in The byte array which contains the object to be deserialized
     * @return The deserialized object
     * @throws IOException if the deserialization fails
     */
    default T deserialize(byte[] in) throws IOException
    {
        return deserialize(Unpooled.wrappedBuffer(in));
    }

}