package pt.unl.fct.di.hyflexchain.util.serializer.serializers.common;

import java.util.Map;

import org.apache.tuweni.bytes.Bytes;

import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class CommonSerializers {
    
    public static final Map<Class<?>, ISerializer<?>> COMMON_SERIALIZERS =
        Map.of(
            String.class, StringSerializer.INSTANCE,
            Bytes.class, BytesSerializer.INSTANCE
        );

}
