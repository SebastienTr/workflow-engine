package com.sebastientr.workflow.process;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class TransactionExecutorTest {
    @Autowired
    TransactionExecutor transactionExecutor;

    @Test
    void withoutReturn_shouldRunRunnableInTransaction() {
        Runnable mockRunnable = mock(Runnable.class);

        transactionExecutor.withoutReturn(mockRunnable);

        verify(mockRunnable, times(1)).run();
    }

    @Test
    void withReturn_shouldReturnResultFromSupplierInTransaction() {
        Supplier<String> mockSupplier = Mockito.mock(Supplier.class);
        String expectedResult = "Mocked Result";

        // Stubbing the supplier
        when(mockSupplier.get()).thenReturn(expectedResult);

        String result = transactionExecutor.withReturn(mockSupplier);

        assertEquals(expectedResult, result);
        verify(mockSupplier, times(1)).get();
    }
}
