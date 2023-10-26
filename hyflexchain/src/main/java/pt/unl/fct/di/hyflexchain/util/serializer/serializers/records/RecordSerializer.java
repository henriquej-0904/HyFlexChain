package pt.unl.fct.di.hyflexchain.util.serializer.serializers.records;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;

import io.netty.buffer.ByteBuf;
import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;

public class RecordSerializer<T extends Record> implements ISerializer<T> {

    private final RecordComponent[] components;
    private final Constructor<T> constructor;
    private final ISerializer<?>[] serializers;

    /**
     * @param components
     * @param constructor
     * @param serializers
     */
    public RecordSerializer(RecordComponent[] components, Constructor<T> constructor,
            ISerializer<?>[] serializers) {
        this.components = components;
        this.constructor = constructor;
        this.serializers = serializers;
    }

    @Override
    public void serialize(T t, ByteBuf out) throws IOException {

        RecordComponent component;
        ISerializer serializer;
        Object result;

        for (int i = 0; i < components.length; i++)
        {
            component = components[i];
            serializer = serializers[i];
            try {
                result = component.getAccessor().invoke(t, (Object[]) null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new IOException(e);
            }
            serializer.serialize(result, out);
        }
    }

    @Override
    public T deserialize(ByteBuf in) throws IOException {
        Object[] params = new Object[components.length];

        for (int i = 0; i < params.length; i++)
            params[i] = serializers[i].deserialize(in);

        try {
            return constructor.newInstance(params);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }
    
}
