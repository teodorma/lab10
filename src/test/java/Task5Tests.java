import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class Task5Tests extends TaskTestsBase {
    @Test
    void testCreate() {
        TaskTestsBase.SimplePOJO expected = new TaskTestsBase.SimplePOJO(1, "asd");

        customContainer.addInstance(expected.getNumber());
        customContainer.addInstance(expected.getString());

        customContainer.addFactoryMethod(TaskTestsBase.SimplePOJO.class, (container) -> {
            int number = container.getInstance(Integer.class);
            String string = container.getInstance(String.class);
            return new TaskTestsBase.SimplePOJO(number, string);
        });

        TaskTestsBase.SimplePOJO actual = customContainer.create(TaskTestsBase.SimplePOJO.class);
        assertEquals(expected, actual);
    }

    @Test
    void testCreateWithParameters() {
        TaskTestsBase.SimplePOJO expected = new TaskTestsBase.SimplePOJO(1, "asd");

        customContainer.addFactoryMethod(TaskTestsBase.SimplePOJO.class, (container) -> {
            int number = container.getInstance(Integer.class);
            String string = container.getInstance(String.class);
            return new TaskTestsBase.SimplePOJO(number, string);
        });

        TaskTestsBase.SimplePOJO actual = customContainer.create(TaskTestsBase.SimplePOJO.class,
                Map.of(Integer.class.getName(), expected.getNumber(), String.class.getName(), expected.getString()));
        assertEquals(expected, actual);
    }

    @Test
    void testCreateWithParametersOverride() {
        TaskTestsBase.SimplePOJO expected = new TaskTestsBase.SimplePOJO(1, "asd");

        customContainer.addInstance(5);
        customContainer.addInstance("sda");


        customContainer.addFactoryMethod(TaskTestsBase.SimplePOJO.class, (container) -> {
            int number = container.getInstance(Integer.class);
            String string = container.getInstance(String.class);
            return new TaskTestsBase.SimplePOJO(number, string);
        });

        TaskTestsBase.SimplePOJO actual = customContainer.create(TaskTestsBase.SimplePOJO.class,
                Map.of(Integer.class.getName(), expected.getNumber(), String.class.getName(), expected.getString()));
        assertEquals(expected, actual);
    }

    @Test
    void testCreateDifferentInstances() {
        Object storedInstance = new Object();
        customContainer.addInstance(storedInstance);
        customContainer.addFactoryMethod(Object.class, (container) -> new Object());
        Object actual = customContainer.create(Object.class);
        assertNotEquals(storedInstance, actual);
    }

    @Test
    void testCreateWithParametersDifferentInstances() {
        TaskTestsBase.SimplePOJO other = new TaskTestsBase.SimplePOJO(2, "sda");

        customContainer.addInstance(other);
        customContainer.addFactoryMethod(TaskTestsBase.SimplePOJO.class, (container) -> {
            int number = container.getInstance(Integer.class);
            String string = container.getInstance(String.class);
            return new TaskTestsBase.SimplePOJO(number, string);
        });

        TaskTestsBase.SimplePOJO actual = customContainer.create(TaskTestsBase.SimplePOJO.class,
                Map.of(Integer.class.getName(), 1, String.class.getName(), "asd"));
        assertNotEquals(other, actual);
    }
}
