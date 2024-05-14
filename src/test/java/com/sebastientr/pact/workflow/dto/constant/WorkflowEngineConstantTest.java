package com.sebastientr.workflow.dto.constant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

@SpringBootTest
class WorkflowEngineConstantTest {
    @Test
    void testPrivateConstructor() throws NoSuchMethodException {
        Constructor<WorkflowEngineConstant> constructor = WorkflowEngineConstant.class.getDeclaredConstructor();
        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}