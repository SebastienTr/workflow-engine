package com.sebastientr.workflow.domain.repository;

import com.sebastientr.workflow.domain.entity.core.FlowEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

@SpringBootTest//(classes = {FlowRepository.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FlowRepositoryTest {
    @SpyBean
    private FlowRepository flowRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private FlowTaskRepository flowTaskRepository;

    @BeforeEach
    void beforeEach() {
        // idk
    }

    @Test
    void testNaturalOrder() {
        Optional<FlowEntity> flow = flowRepository.findByNameOrderByTaskOrder("test-flow");

        validateFlowOrderIntegrity(flow);
    }

    @Test
    void testMixedOrder() {
//        var task1 = taskRepository.findById(1L);
//        var task2 = taskRepository.findById(2L);
//        var task3 = taskRepository.findById(3L);
//
//        Assertions.assertTrue(task1.isPresent());
//        Assertions.assertTrue(task2.isPresent());
//        Assertions.assertTrue(task3.isPresent());
//
//        task1.get().set
//
//        taskRepository.save(task1.get());
//        taskRepository.save(task2.get());
//        taskRepository.save(task3.get());
//
//        flowTaskRepository.find


        Optional<FlowEntity> flow = validateFlowOrderIntegrity(flowRepository.findByNameOrderByTaskOrder("test-flow"));

        flow.get().getFlowTask().get(0).setTaskOrder(2);
        flow.get().getFlowTask().get(1).setTaskOrder(3);
        flow.get().getFlowTask().get(2).setTaskOrder(1);

        flowRepository.save(flow.get());
        flowTaskRepository.save(flow.get().getFlowTask().get(0));
        flowTaskRepository.save(flow.get().getFlowTask().get(1));
        flowTaskRepository.save(flow.get().getFlowTask().get(2));

        validateFlowOrderIntegrity(flowRepository.findByNameOrderByTaskOrder("test-flow"));
    }

    private static Optional<FlowEntity> validateFlowOrderIntegrity(Optional<FlowEntity> flow) {
        Assertions.assertTrue(flow.isPresent());
        Assertions.assertEquals(3, flow.get().getFlowTask().size());
        Assertions.assertEquals(1, flow.get().getFlowTask().get(0).getTaskOrder());
        Assertions.assertEquals(2, flow.get().getFlowTask().get(1).getTaskOrder());
        Assertions.assertEquals(3, flow.get().getFlowTask().get(2).getTaskOrder());

        return flow;
    }
}
