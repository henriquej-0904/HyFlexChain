package pt.unl.fct.di.hyflexchain.util.serializer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.ArraySerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.arrays.ArraysSerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.collections.CollectionSerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.collections.MapSerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.common.CommonSerializers;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.enums.EnumSerializer;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.primitive.PrimitiveSerializers;
import pt.unl.fct.di.hyflexchain.util.serializer.serializers.records.RecordSerializerBuilder;


/**
 * Usage:
 * {@code Autor}
 */
public class AutoSerializer {
    
    private final Map<Class<?>, ISerializer<?>> customSerializers,
        runtimeSerializers;

    protected AutoSerializer(Map<Class<?>, ISerializer<?>> customSerializers)
    {
        this.customSerializers = customSerializers;
        this.runtimeSerializers = Collections.synchronizedMap(HashMap.newHashMap(100));
    }


    public <T> ISerializer<T> getSerializer(Class<T> type)
    {
        Object res = customSerializers.get(type);
        if (res != null)
            return (ISerializer<T>) res;

        res = PrimitiveSerializers.PRIMITIVE_SERIALIZERS.get(type);
        if (res != null)
            return (ISerializer<T>) res;

        res = CommonSerializers.COMMON_SERIALIZERS.get(type);
        if (res != null)
            return (ISerializer<T>) res;

        if (type.isArray())
        {
            var componentType = type.getComponentType();

            if (componentType.isPrimitive())
                return (ISerializer<T>) ArraysSerializer.ARRAY_PRIMITIVE_SERIALIZERS.get(componentType);
            
            var componentSerializer = getSerializer(componentType);
            return (ISerializer<T>) new ArraySerializer(componentType, componentSerializer);
        }

        res = this.runtimeSerializers.get(type);
        if (res != null)
            return (ISerializer<T>) res;

        // Not found :(
        // Create serializer, add to runtimeSerializers and return
        var createdSerializer = createSerializer(type);
        this.runtimeSerializers.put(type, createdSerializer);
        return createdSerializer;
    }

    protected <T> ISerializer<T> createSerializer(Class<T> type)
    {
        if (type.isRecord())
        {
            var recordType = type.asSubclass(Record.class);
            return (ISerializer<T>) RecordSerializerBuilder.createRecordSerializer(recordType, this);
        }

        if (type.isEnum())
        {
            var enumType = type.asSubclass(Enum.class);
            return (ISerializer<T>) new EnumSerializer(enumType);
        }
        
        throw new UnsupportedOperationException("createSerializer not implemented");
    }

    //#region Primitive Arrays

    public ISerializer<boolean[]> getArraySerializerBoolean()
    {
        return (ISerializer<boolean[]>) ArraysSerializer.ARRAY_PRIMITIVE_SERIALIZERS.get(boolean.class);
    }

    public ISerializer<byte[]> getArraySerializerByte()
    {
        return (ISerializer<byte[]>) ArraysSerializer.ARRAY_PRIMITIVE_SERIALIZERS.get(byte.class);
    }

    public ISerializer<char[]> getArraySerializerChar()
    {
        return (ISerializer<char[]>) ArraysSerializer.ARRAY_PRIMITIVE_SERIALIZERS.get(char.class);
    }

    public ISerializer<short[]> getArraySerializerShort()
    {
        return (ISerializer<short[]>) ArraysSerializer.ARRAY_PRIMITIVE_SERIALIZERS.get(short.class);
    }

    public ISerializer<int[]> getArraySerializerInt()
    {
        return (ISerializer<int[]>) ArraysSerializer.ARRAY_PRIMITIVE_SERIALIZERS.get(int.class);
    }

    public ISerializer<long[]> getArraySerializerLong()
    {
        return (ISerializer<long[]>) ArraysSerializer.ARRAY_PRIMITIVE_SERIALIZERS.get(long.class);
    }

    public ISerializer<float[]> getArraySerializerFloat()
    {
        return (ISerializer<float[]>) ArraysSerializer.ARRAY_PRIMITIVE_SERIALIZERS.get(float.class);
    }

    public ISerializer<double[]> getArraySerializerDouble()
    {
        return (ISerializer<double[]>) ArraysSerializer.ARRAY_PRIMITIVE_SERIALIZERS.get(double.class);
    }

    //#endregion

    public <T> ISerializer<T[]> getArraySerializerGenericFromComponentType(Class<T> componentType)
    {
        var componentSerializer = getSerializer(componentType);
        return new ArraySerializer<T>(componentType, componentSerializer);
    }

    public <T> ISerializer<T[]> getArraySerializer(Class<T> componentType, ISerializer<T> componentSerializer)
    {
        return new ArraySerializer<T>(componentType, componentSerializer);
    }

    public <E, T extends Collection<E>> ISerializer<T> getCollectionSerializer(
        ISerializer<E> componentSerializer, IntFunction<T> createCollection)
    {
        return new CollectionSerializer<>(componentSerializer, createCollection);
    }

    public <K, V, T extends Map<K, V>> ISerializer<T> getMapSerializer(
        ISerializer<K> kSerializer, ISerializer<V> vSerializer, IntFunction<T> createMap)
    {
        return new MapSerializer<>(kSerializer, vSerializer, createMap);
    }

    public <T extends Record> ISerializer<T> getRecordSerializer(Class<T> recordType,
        ISerializer<?>... componentSerializers)
    {
        return RecordSerializerBuilder.createRecordSerializer(recordType, componentSerializers);
    }

    protected ISerializer<?> getArraySerializer(Class<?> type)
    {
        var componentType = type.getComponentType();

        if (componentType.isPrimitive())
            return ArraysSerializer.ARRAY_PRIMITIVE_SERIALIZERS.get(componentType);

        var componentSerializer = getSerializer(componentType);
        return new ArraySerializer(componentType, componentSerializer);
    }



    /**
     * A builder for the AutoSerializer class
     * where a user can configure to its needs.
     */
    public static class Builder {

        private final Map<Class<?>, ISerializer<?>> customSerializers;

        /**
         * Create a new default Builder
         * @param customSerializers
         */
        public Builder() {
            // this.registeredClasses = new ArrayList<>(100);
            this.customSerializers = new HashMap<>();
        }

        /**
         * Add a custom serializer for a specific class.
         * This method can be used for users that need a more efficient
         * serializer for a specific case.
         * The serializer for this class will be used for classes that
         * need to serialize objects of this type.
         * @param <T> The generic type of the class
         * @param type The class object
         * @param serializer The serializer for this type
         * @return The updated builder
         */
        public <T> Builder addCustomSerializer(Class<T> type, ISerializer<T> serializer) {
            this.customSerializers.put(type, serializer);
            return this;
        }

        /**
         * Register a class for serialization.
         * This is an optional operation that modifies how
         * a serializer is obtained for a particular class. <p>
         * 
         * When {@link #build()} is called, all required serializers for
         * all registered classes through this method are implemented.
         * This means that all serializers for that classes are simply
         * returned when calling {@code AutoSerializer.getSerializer(type)}. <p>
         * 
         * If a class is not registered, you can still get a serializer for it
         * but every time the {@code AutoSerializer.getSerializer(type)} is called
         * a new one will be created.
         * 
         * @param types The classes to register for serialization
         * @return The updated builder
         */
        /* public Builder registerClasses(Class<?>... types)
        {
            for (var type : types) {
                this.registeredClasses.add(type);
            }

            return this;
        } */

        public AutoSerializer build() {
            return new AutoSerializer(this.customSerializers);
        }
    }

}
