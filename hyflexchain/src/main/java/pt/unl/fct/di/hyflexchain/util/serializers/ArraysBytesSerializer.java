package pt.unl.fct.di.hyflexchain.util.serializers;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

public class ArraysBytesSerializer
{
    
    public static final <T> BytesSerializer<T> primitiveSerializer(Class<T> primitiveArrayClass)
    {
        if (!primitiveArrayClass.isArray() || !primitiveArrayClass.getComponentType().isPrimitive())
            throw new IllegalArgumentException("Type is not array of a primitive type: " + primitiveArrayClass.getName());

        if (primitiveArrayClass.equals(byte[].class))
        {
            @SuppressWarnings("unchecked")
            BytesSerializer<T> result = (BytesSerializer<T>) BYTE_ARRAY_SERIALIZER;
            return result;
        }

        throw new UnsupportedOperationException("Array primitive serializer not implemented for: " + primitiveArrayClass.getName());
    }

    public static final <T> BytesSerializer<T[]> genericSerializer(Class<T> elemType, BytesSerializer<T> elemSerializer)
    {
        return new GenericArraySerializer<>(elemType, elemSerializer);
    }

    public static final BytesSerializer<byte[]> BYTE_ARRAY_SERIALIZER =
        new BytesSerializer<byte[]>() {

            @Override
            public int serializedSize(byte[] obj) {
                return Integer.BYTES + obj.length;
            }

            @Override
            public ByteBuffer serialize(byte[] obj, ByteBuffer buff) {
                return buff.putInt(obj.length).put(obj);
            }

            @Override
            public byte[] deserialize(ByteBuffer buff) {
                int size = buff.getInt();

                if (size < 0)
                    throw new IllegalArgumentException("Invalid array size: " + size);

                byte[] result = new byte[size];
                buff.get(result);

                return result;
            }

            @Override
            public Class<byte[]> getType() {
                return byte[].class;
            }

            @Override
            public BytesSerializer<byte[][]> toArraySerializer() {
                // TODO Auto-generated method stub
                return BytesSerializer.super.toArraySerializer();
            } 
            
        };

    public static class GenericArraySerializer<T> implements BytesSerializer<T[]>
    {
        protected final Class<T> elemType;
        protected final BytesSerializer<T> elemSerializer;

        /**
         * @param elemSerializer
         */
        public GenericArraySerializer(Class<T> elemType, BytesSerializer<T> elemSerializer) {
            this.elemType = elemType;
            this.elemSerializer = elemSerializer;
        }

        @Override
        public int serializedSize(T[] obj) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'serializedSize'");
        }

        @Override
        public ByteBuffer serialize(T[] obj, ByteBuffer buff) {
            buff.putInt(obj.length);

            for (T t : obj) {
                elemSerializer.serialize(t, buff);
            }

            return buff;
        }

        @Override
        public T[] deserialize(ByteBuffer buff) {
            int size = buff.getInt();

            if (size < 0)
                throw new IllegalArgumentException("Invalid array size: " + size);

            @SuppressWarnings("unchecked")
            T[] result = (T[]) Array.newInstance(elemType, size);
            
            for (int i = 0; i < size; i++)
                result[i] = elemSerializer.deserialize(buff);

            return result;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<T[]> getType() {
            return (Class<T[]>) this.elemType.arrayType();
        }
        
    }
}
