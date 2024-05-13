import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CustomContainerImpl implements CustomContainer {
    public Map<String, Object> instances = new HashMap<>();
    private Map<Class<?>, Function<CustomContainer, ?>> factoryMethods = new HashMap<>();

    @Override
    public <T> boolean addInstance(T instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Null is not allowed as a parameter");
        }
        String className = instance.getClass().getSimpleName();
        return addInstanceInternal(instance, className);
    }

    @Override
    public <T> boolean addInstance(T instance, String customName) {
        if (instance == null || customName == null || customName.trim().isEmpty()) {
            throw new IllegalArgumentException("Null is not allowed as a parameter");
        }
        return addInstanceInternal(instance, customName);
    }

    private <T> boolean addInstanceInternal(T instance, String name) {
        if (instances.containsKey(name)) {
            throw new IllegalStateException("Instances cannot be redeclared");
        }
        instances.put(name, instance);
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Null is not allowed as a parameter");
        }
        return getInstance(type, type.getSimpleName());
    }

    @Override
    public <T> T getInstance(Class<T> type, String customName) {
        if (type == null || customName == null) {
            throw new IllegalArgumentException("Null is not allowed as a parameter");
        }
        Object instance = instances.get(customName);
        if (instance == null) {
            Function<CustomContainer, ?> factory = factoryMethods.get(type);
            if (factory != null) {
                instance = factory.apply(this);
                instances.put(customName, instance);
            } else {
                throw new IllegalStateException("Cannot provide instance");
            }
        }
        if (!type.isInstance(instance)) {
            throw new ClassCastException("Invalid type for object");
        }
        return type.cast(instance);
    }

    @Override
    public <T> boolean addFactoryMethod(Class<T> type, Function<CustomContainer, T> factoryMethod) {
        if (type == null || factoryMethod == null) {
            throw new IllegalArgumentException("Null is not allowed as a parameter");
        }
        factoryMethods.put(type, factoryMethod);
        return true;
    }

    @Override
    public <T> T create(Class<T> type) {
        return create(type, new HashMap<>());
    }

    @Override
    public <T> T create(Class<T> type, Map<String, Object> parameters) {
        if (type == null || parameters == null) {
            throw new IllegalArgumentException("Null is not allowed as a parameter");
        }

        Function<CustomContainer, ?> factory = factoryMethods.get(type);
        if (factory == null) {
            throw new IllegalStateException("Cannot provide instance");
        }

        Map<String, Object> originalInstances = new HashMap<>(this.instances);
        parameters.forEach((name, instance) -> this.instances.put(name, instance));

        try {
            T newInstance = type.cast(factory.apply(this));
            if (newInstance == null) {
                throw new IllegalStateException("Cannot provide instance");
            }
            return newInstance;
        } finally {
            this.instances.clear();
            this.instances.putAll(originalInstances);
        }
    }

    @Override
    public void close() throws Exception {
        for (Object instance : instances.values()) {
            if (instance instanceof AutoCloseable) {
                ((AutoCloseable) instance).close();
            }
        }
    }
}
