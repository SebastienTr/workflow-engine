package com.sebastientr.workflow.process;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Map;

@SpringBootTest
class LoggerContextTest {
    @Test
    void testPrivateConstructor() throws NoSuchMethodException {
        Constructor<LoggerContext> constructor = LoggerContext.class.getDeclaredConstructor();
        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    void testSimpleSetGet() {
        LoggerContext.set("key", "value");
        Assertions.assertEquals("value", LoggerContext.get("key"));
    }

    @Test
    void testSetGetMap() {
        Map<String, Object> map = Map.of("key", "value");
        LoggerContext.addAll(map);
        Assertions.assertEquals(map, LoggerContext.getAll());
    }


    @Test
    void testClear() {
        Map<String, Object> map = Map.of("key", "value");
        LoggerContext.addAll(map);
        LoggerContext.clear();
        Assertions.assertEquals(Collections.emptyMap(), LoggerContext.getAll());
    }

    @Test
    void testRemove() {
        Map<String, Object> map = Map.of("key", "value");
        LoggerContext.addAll(map);
        LoggerContext.remove("key");
        Assertions.assertEquals(Collections.emptyMap(), LoggerContext.getAll());
    }

    @Test
    void testUnload() {
        Map<String, Object> map = Map.of("key", "value");
        LoggerContext.addAll(map);
        LoggerContext.unload();
        Assertions.assertNull(LoggerContext.getAll());
    }
}