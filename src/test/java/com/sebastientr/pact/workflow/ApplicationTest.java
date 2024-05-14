package com.sebastientr.workflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest
@ComponentScan(basePackages = "com.sebastientr.workflow")
class ApplicationTest {
    @Test
    void contextLoads() {
        // Context loads
        Assertions.assertTrue(true);
    }
}
