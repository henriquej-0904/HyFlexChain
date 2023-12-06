package pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays;

import java.util.Map;

import pt.unl.fct.di.hyflexchain.util.serializer.AutoSerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive.BooleanArraySerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive.ByteArraySerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive.CharArraySerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive.DoubleArraySerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive.FloatArraySerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive.IntArraySerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive.LongArraySerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.primitive.ShortArraySerializer;

public class ArraysSerializer {
    
    public static final Map<Class<?>, ISerializer<?>> ARRAY_PRIMITIVE_SERIALIZERS =
        Map.of(
            boolean.class, BooleanArraySerializer.INSTANCE,
            byte.class, ByteArraySerializer.INSTANCE,
            char.class, CharArraySerializer.INSTANCE,
            short.class, ShortArraySerializer.INSTANCE,
            int.class, IntArraySerializer.INSTANCE,
            long.class, LongArraySerializer.INSTANCE,
            float.class, FloatArraySerializer.INSTANCE,
            double.class, DoubleArraySerializer.INSTANCE
        );

    private final AutoSerializer autoSerializer;

    public ArraysSerializer(AutoSerializer autoSerializer)
    {
        this.autoSerializer = autoSerializer;
    }

    

    public <T> ISerializer<T[]> getArraySerializer(Class<T> componentType)
    {
        var componentSerializer = this.autoSerializer.getSerializer(componentType);
        return new ArraySerializer<T>(componentType, componentSerializer);
    }

    public static <T> ISerializer<T[]> getArraySerializer(Class<? extends T> componentType,
        ISerializer<T> componentSerializer)
    {
        return new ArraySerializer<T>(componentType, componentSerializer);
    }

}
