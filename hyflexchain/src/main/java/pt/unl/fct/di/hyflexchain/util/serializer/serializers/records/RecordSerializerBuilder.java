package pt.unl.fct.di.hyflexchain.util.serializer.serializers.records;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import pt.unl.fct.di.hyflexchain.util.serializer.ISerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.AutoSerializer;

public class RecordSerializerBuilder {
    
    public static <T extends Record> RecordSerializer<T>
        createRecordSerializer(Class<T> recordType, AutoSerializer autoSerializer)
    {
        var components = recordType.getRecordComponents();
        var componentTypes = Arrays.stream(components)
           .map(RecordComponent::getType)
           .toArray(Class<?>[]::new);
        
        var constructor = getCanonicalConstructor(recordType, componentTypes);
        var serializers = getSerializers(componentTypes, autoSerializer);

        return new RecordSerializer<>(components, constructor, serializers);
    }

    public static <T extends Record> RecordSerializer<T>
        createRecordSerializer(Class<T> recordType, ISerializer<?>[] componentSerializers)
    {
        var components = recordType.getRecordComponents();
        var componentTypes = Arrays.stream(components)
           .map(RecordComponent::getType)
           .toArray(Class<?>[]::new);
        
        var constructor = getCanonicalConstructor(recordType, componentTypes);

        return new RecordSerializer<>(components, constructor, componentSerializers);
    }

    private static <T extends Record> Constructor<T>
        getCanonicalConstructor(Class<T> recordType, Class<?>[] componentTypes)
    {
        try {
            return recordType.getDeclaredConstructor(componentTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private static ISerializer<?>[] getSerializers(Class<?>[] componentTypes,
        AutoSerializer autoSerializer)
    {
        return Arrays.stream(componentTypes)
            .map(autoSerializer::getSerializer)
            .toArray(ISerializer<?>[]::new);
    }

}
