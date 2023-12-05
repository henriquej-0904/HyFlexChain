package pt.unl.fct.di.hyflexchain.util.serializer.serializers.enums;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.common.StringSerializer;

public class EnumSerializer<T extends Enum<T>> implements ISerializer<T> {

    private final static ISerializer<String> STRING_SERIALIZER =
        StringSerializer.INSTANCE;

    private final String enumName;
    private final Map<String, T> enumConstants;

    /**
     * @param enumType
     */
    public EnumSerializer(Class<T> enumType) {
        this.enumName = enumType.getName();
        this.enumConstants = Arrays.stream(enumType.getEnumConstants())
            .collect(
                Collectors.toUnmodifiableMap(
                    T::name,
                    UnaryOperator.identity(), 
                    (x, y) -> x
                )
        );
    }

    @Override
    public void serialize(T t, ByteBuf out) throws IOException {
        STRING_SERIALIZER.serialize(t.name(), out);
    }

    @Override
    public T deserialize(ByteBuf in) throws IOException {
        String name = STRING_SERIALIZER.deserialize(in);
        T res = this.enumConstants.get(name);

        if (res == null)
            throw new IOException(
                String.format("Undefined Enum %s constant: %s", enumName, name));

        return res;
    }
    
}
