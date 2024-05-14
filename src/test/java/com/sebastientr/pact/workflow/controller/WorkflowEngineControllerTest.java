package com.sebastientr.workflow.controller;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@AutoConfigureMockMvc(addFilters = false)
//@WebMvcTest(WorkflowEngineController.class)
//class WorkflowEngineControllerTest {
//    @Autowired
//    private MockMvc mvc;
//
//    @Test
//    void testGetAllFlow() {
//        try {
//            mvc.perform(MockMvcRequestBuilders.get("/api/employees")
//                            .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isOk())
//                    .andExpect(content()
//                            .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//                    .andExpect(MockMvcResultMatchers.jsonPath("$.employees[*].employeeId").exists());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//}