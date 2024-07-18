package it.unisalento.pasproject.analyticsservice.controller;
import it.unisalento.pasproject.analyticsservice.config.TestSecurityConfig;
import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.ListTaskAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.TaskAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.UserAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.repositories.AssignmentAnalyticsRepository;
import it.unisalento.pasproject.analyticsservice.service.CalculateAnalyticsService;
import it.unisalento.pasproject.analyticsservice.service.UserCheckService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalculateAnalyticsService calculateAnalyticsService;

    @MockBean
    private UserCheckService userCheckService;

    @MockBean
    private AssignmentAnalyticsRepository assignmentAnalyticsRepository;

    private static final String EMAIL_UTENTE = "user@example.com";
    private static final String ROLE_UTENTE = "ROLE_UTENTE";
    private static final String ROLE_MEMBRO = "ROLE_MEMBRO";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    @Test
    @WithMockUser(username = EMAIL_UTENTE, roles = {"UTENTE"})
    void getUserAnalytics_whenDataExists_returnsUserAnalyticsDTO() throws Exception {
        UserAnalyticsDTO userAnalyticsDTO = new UserAnalyticsDTO();
        userAnalyticsDTO.setEnergySaved(100);

        given(userCheckService.getCurrentUserEmail()).willReturn(EMAIL_UTENTE);
        given(calculateAnalyticsService.getUserAnalytics(anyString(), any(), any())).willReturn(Optional.of(userAnalyticsDTO));

        mockMvc.perform(get("/api/analytics/user/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.energySaved").value(100));
    }

    @Test
    @WithMockUser(username = EMAIL_UTENTE, roles = {"UTENTE"})
    void getUserListAnalytics_whenInvalidMonth_throwsBadFormatRequestException() throws Exception {
        mockMvc.perform(get("/api/analytics/user/list")
                        .param("month", "13")
                        .param("year", "2020")
                        .param("granularity", "month"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertEquals("Wrong request format. Please provide a valid month and year", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    @WithMockUser(username = EMAIL_UTENTE, roles = {"UTENTE"})
    void getUserAnalyticsByTask_whenUserHasTasks_returnsNonEmptyListTaskAnalytics() throws Exception {
        List<AssignmentAnalytics> allAssignments = new ArrayList<>();
        AssignmentAnalytics assignment = new AssignmentAnalytics();
        assignment.setTaskId("1");

        AssignmentAnalytics assignment2 = new AssignmentAnalytics();
        assignment2.setTaskId("1");
        allAssignments.add(assignment2);
        allAssignments.add(assignment);

        ListTaskAnalytics listDTO = new ListTaskAnalytics();
        List<TaskAnalyticsDTO> list = new ArrayList<>();
        TaskAnalyticsDTO taskAnalyticsDTO = new TaskAnalyticsDTO();
        taskAnalyticsDTO.setTaskId("1");
        taskAnalyticsDTO.setEnergySaved(200);
        list.add(taskAnalyticsDTO);
        listDTO.setList(list);

        UserAnalyticsDTO userAnalyticsDTO = new UserAnalyticsDTO();
        userAnalyticsDTO.setEnergySaved(100);
        userAnalyticsDTO.setComputingPowerUsed(50);

        given(userCheckService.getCurrentUserEmail()).willReturn(EMAIL_UTENTE);
        given(assignmentAnalyticsRepository.findAllByEmailUtente(EMAIL_UTENTE)).willReturn(allAssignments);
        given(calculateAnalyticsService.getTaskUserAnalytics(anyString())).willReturn(Optional.of(userAnalyticsDTO));

        mockMvc.perform(get("/api/analytics/user/get/task/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].taskId").value("1"))
                .andExpect(jsonPath("$.list[0].energySaved").value(100));
    }

    @Test
    @WithMockUser(username = EMAIL_UTENTE, roles = {"UTENTE"})
    void getUserAnalyticsByTask_whenUserHasNoTasks_returnsEmptyListTaskAnalytics() throws Exception {
        given(userCheckService.getCurrentUserEmail()).willReturn(EMAIL_UTENTE);
        given(assignmentAnalyticsRepository.findAllByEmailUtente(EMAIL_UTENTE)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/analytics/user/get/task/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isEmpty());
    }

    @Test
    @WithMockUser(username = EMAIL_UTENTE, roles = {"UTENTE"})
    void getUserAnalyticsByTask_whenServiceThrowsException_returnsInternalServerError() throws Exception {
        given(userCheckService.getCurrentUserEmail()).willReturn(EMAIL_UTENTE);
        given(assignmentAnalyticsRepository.findAllByEmailUtente(EMAIL_UTENTE)).willThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/analytics/user/get/task/all"))
                .andExpect(status().isNotFound());
    }

}