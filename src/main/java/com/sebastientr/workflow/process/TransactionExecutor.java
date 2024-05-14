package com.sebastientr.workflow.process;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class TransactionExecutor {
    /**
     * Execute some code that does not return a result, in a "REQUIRED" transaction.
     */
    @Transactional
    public void withoutReturn(Runnable runnable) {
        runnable.run();
    }

    /**
     * Execute some code that returns a result, in a "REQUIRED" transaction.
     */
    @Transactional
    public <T> T withReturn(Supplier<T> supplier) {
        return supplier.get();
    }
}
