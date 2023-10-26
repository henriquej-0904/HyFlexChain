package pt.unl.fct.di.hyflexchain.util.serializer.serializers.primitive;

import java.io.IOException;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class PrimitiveSerializers {
    
    public static final Map<Class<?>, ISerializer<?>> PRIMITIVE_SERIALIZERS =
        Map.ofEntries(
            Map.entry(Boolean.class, BooleanSerializer.INSTANCE),
            Map.entry(boolean.class, BooleanSerializer.INSTANCE),

            Map.entry(Byte.class, ByteSerializer.INSTANCE),
            Map.entry(byte.class, ByteSerializer.INSTANCE),

            Map.entry(Character.class, CharSerializer.INSTANCE),
            Map.entry(char.class, CharSerializer.INSTANCE),

            Map.entry(Short.class, ShortSerializer.INSTANCE),
            Map.entry(short.class, ShortSerializer.INSTANCE),

            Map.entry(Integer.class, IntSerializer.INSTANCE),
            Map.entry(int.class, IntSerializer.INSTANCE),

            Map.entry(Long.class, LongSerializer.INSTANCE),
            Map.entry(long.class, LongSerializer.INSTANCE),

            Map.entry(Float.class, FloatSerializer.INSTANCE),
            Map.entry(float.class, FloatSerializer.INSTANCE),

            Map.entry(Double.class, DoubleSerializer.INSTANCE),
            Map.entry(double.class, DoubleSerializer.INSTANCE)
        );


    public static void serializeBoolean(boolean val, ByteBuf out) throws IOException
    {
        out.writeBoolean(val);
    }

    public static boolean deserializeBoolean(ByteBuf in) throws IOException
    {
        return in.readBoolean();
    }

    public static void serializeByte(byte val, ByteBuf out) throws IOException
    {
        out.writeByte(val);
    }

    public static byte deserializeByte(ByteBuf in) throws IOException
    {
        return in.readByte();
    }

    public static void serializeChar(char val, ByteBuf out) throws IOException
    {
        out.writeChar(val);
    }

    public static int deserializeChar(ByteBuf in) throws IOException
    {
        return in.readChar();
    }

    public static void serializeShort(short val, ByteBuf out) throws IOException
    {
        out.writeShort(val);
    }

    public static short deserializeShort(ByteBuf in) throws IOException
    {
        return in.readShort();
    }

    public static void serializeInt(int val, ByteBuf out) throws IOException
    {
        out.writeInt(val);
    }

    public static int deserializeInt(ByteBuf in) throws IOException
    {
        return in.readInt();
    }

    public static void serializeLong(long val, ByteBuf out) throws IOException
    {
        out.writeLong(val);
    }

    public static long deserializeLong(ByteBuf in) throws IOException
    {
        return in.readLong();
    }

    public static void serializeFloat(float val, ByteBuf out) throws IOException
    {
        out.writeFloat(val);
    }

    public static float deserializeFloat(ByteBuf in) throws IOException
    {
        return in.readFloat();
    }

    public static void serializeDouble(double val, ByteBuf out) throws IOException
    {
        out.writeDouble(val);
    }

    public static double deserializeDouble(ByteBuf in) throws IOException
    {
        return in.readDouble();
    }

}
