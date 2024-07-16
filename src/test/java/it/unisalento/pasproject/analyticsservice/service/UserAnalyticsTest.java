package it.unisalento.pasproject.analyticsservice.service;


import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.UserAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.UserListAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.repositories.AssignedResourceRepository;
import it.unisalento.pasproject.analyticsservice.repositories.AssignmentAnalyticsRepository;
import it.unisalento.pasproject.analyticsservice.service.Template.TaskAnalyticsTemplate;
import it.unisalento.pasproject.analyticsservice.service.Template.UserListTemplate;
import it.unisalento.pasproject.analyticsservice.service.Template.UserTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest()
@ExtendWith({MockitoExtension.class, SpringExtension.class})
@ActiveProfiles("test")
@Import({UserTemplate.class, UserListTemplate.class, TaskAnalyticsTemplate.class})
class UserAnalyticsTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AssignedResourceRepository assignedResourceRepository;

    @Autowired
    private AssignmentAnalyticsRepository assignmentAnalyticsRepository;

    @Autowired
    private UserTemplate userTemplate;

    @Autowired
    private UserListTemplate userListTemplate;

    private static final String UTENTE_1 = "user1@example.com";
    private static final String MEMBRO_1 = "member1@example.com";

    private final Logger LOGGER = LoggerFactory.getLogger(OverallAnalyticsTest.class);

    @Autowired
    private TaskAnalyticsTemplate taskAnalyticsTemplate;

    @BeforeEach
    void setUpDb() {
        AssignmentAnalytics assignment = new AssignmentAnalytics();
        assignment.setAssignedTime(LocalDateTime.now().minusDays(1));
        assignment.setEmailUtente(UTENTE_1);
        assignment.setTaskId("1");
        assignment.setAssignedTime(LocalDateTime.now().minusMinutes(30));
        assignment.setCompletedTime(LocalDateTime.now());
        assignment.setComplete(true);
        assignment.setLastUpdate(LocalDateTime.now());

        assignment = assignmentAnalyticsRepository.save(assignment);

        AssignedResource resource1 = new AssignedResource();
        resource1.setHardwareId("gpu1");
        resource1.setTaskId(assignment.getTaskId());
        resource1.setHardwareName("NVIDIA RTX 3080");
        resource1.setAssignedEnergyConsumptionPerHour(320.0);
        resource1.setMemberEmail(MEMBRO_1);
        resource1.setAssignedSingleScore(0);
        resource1.setAssignedMultiScore(0);
        resource1.setAssignedOpenclScore(9000);
        resource1.setAssignedVulkanScore(9500);
        resource1.setAssignedCudaScore(10000);
        resource1.setAssignedTime(LocalDateTime.now().minusMinutes(20));
        resource1.setCompletedTime(LocalDateTime.now());
        resource1.setHasCompleted(true);

        AssignedResource resource2 = new AssignedResource();
        resource2.setTaskId(assignment.getTaskId());
        resource2.setHardwareId("soc1");
        resource2.setHardwareName("Apple M1");
        resource2.setAssignedEnergyConsumptionPerHour(15.0);
        resource2.setMemberEmail(MEMBRO_1);
        resource2.setAssignedSingleScore(1700);
        resource2.setAssignedMultiScore(7500);
        resource2.setAssignedOpenclScore(8000);
        resource2.setAssignedVulkanScore(8200);
        resource2.setAssignedCudaScore(0);
        resource2.setAssignedTime(LocalDateTime.now().minusMinutes(40));
        resource2.setCompletedTime(LocalDateTime.now());
        resource2.setHasCompleted(true);

        AssignedResource resource3 = new AssignedResource();
        resource3.setTaskId(assignment.getTaskId());
        resource3.setHardwareId("cpu1");
        resource3.setHardwareName("Intel i9 10900k");
        resource3.setAssignedEnergyConsumptionPerHour(100.0);
        resource3.setMemberEmail(MEMBRO_1);
        resource3.setAssignedSingleScore(2000);
        resource3.setAssignedMultiScore(8000);
        resource3.setAssignedOpenclScore(8500);
        resource3.setAssignedVulkanScore(8700);
        resource3.setAssignedCudaScore(0);
        resource3.setAssignedTime(LocalDateTime.now().minusDays(2));
        resource3.setCompletedTime(LocalDateTime.now().minusDays(1));
        resource3.setHasCompleted(true);

        AssignedResource resource4 = new AssignedResource();
        resource4.setTaskId(assignment.getTaskId());
        resource4.setHardwareId("cpu2");
        resource4.setHardwareName("Intel i9 10900k");
        resource4.setAssignedEnergyConsumptionPerHour(100.0);
        resource4.setMemberEmail(MEMBRO_1);
        resource4.setAssignedSingleScore(2000);
        resource4.setAssignedMultiScore(8000);
        resource4.setAssignedOpenclScore(8500);
        resource4.setAssignedVulkanScore(8700);
        resource4.setAssignedCudaScore(0);
        resource4.setAssignedTime(LocalDateTime.now().minusMonths(1).minusDays(1));
        resource4.setCompletedTime(LocalDateTime.now().minusMonths(1));
        resource4.setHasCompleted(true);


        assignedResourceRepository.save(resource1);
        assignedResourceRepository.save(resource2);
        assignedResourceRepository.save(resource3);
        assignedResourceRepository.save(resource4);
    }

    @AfterEach
    void cleanUp() {
        assignmentAnalyticsRepository.deleteAll();
        assignedResourceRepository.deleteAll();
    }

    @Test
    void taskAnalyticsReturnsDataForValidTaskId() {
        String validTaskId = "1";
        Optional<UserAnalyticsDTO> result = taskAnalyticsTemplate.getAnalytics(validTaskId, null, null);
        assertTrue(result.isPresent());
        UserAnalyticsDTO analytics = result.get();
        assertEquals(535, analytics.getEnergySaved());
    }

    @Test
    void taskAnalyticsReturnsEmptyForInvalidTaskId() {
        String invalidTaskId = "invalidTaskId";
        Optional<UserAnalyticsDTO> result = taskAnalyticsTemplate.getAnalytics(invalidTaskId, null, null);
        assertFalse(result.isPresent());
    }

    @Test
    void taskAnalyticsThrowsExceptionForNullTaskId() {
        Optional<UserAnalyticsDTO> result = taskAnalyticsTemplate.getAnalytics(null, null, null);
        assertFalse(result.isPresent());
    }

    @Test
    void userAnalyticsReturnsDataForValidUserAndDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        Optional<UserAnalyticsDTO> result = userTemplate.getAnalytics(UTENTE_1, startDate, endDate);
        assertTrue(result.isPresent());
    }

    @Test
    void userAnalyticsReturnsEmptyForInvalidUser() {
        String invalidUserEmail = "invalid@example.com";
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        Optional<UserAnalyticsDTO> result = userTemplate.getAnalytics(invalidUserEmail, startDate, endDate);
        assertFalse(result.isPresent());
    }

    @Test
    void userAnalyticsHandlesNullStartDate() {
        LocalDateTime endDate = LocalDateTime.now();
        Optional<UserAnalyticsDTO> result = userTemplate.getAnalytics(UTENTE_1, null, endDate);
        assertTrue(result.isPresent());
    }

    @Test
    void userAnalyticsHandlesNullEndDate() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        Optional<UserAnalyticsDTO> result = userTemplate.getAnalytics(UTENTE_1, startDate, null);
        assertTrue(result.isPresent());
    }

    @Test
    void userAnalyticsHandlesNullStartAndEndDate() {
        Optional<UserAnalyticsDTO> result = userTemplate.getAnalytics(UTENTE_1, null, null);
        assertTrue(result.isPresent());
    }

    @Test
    void userListAnalyticsReturnsDataForValidUserAndDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        String granularity = "day";
        List<UserListAnalyticsDTO> result = userListTemplate.getAnalyticsList(UTENTE_1, startDate, endDate, granularity);
        assertFalse(result.isEmpty());
    }

    void userListAnalyticsReturnsEmptyForInvalidUser() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        String granularity = "day";
        List<UserListAnalyticsDTO> result = userListTemplate.getAnalyticsList("invalid@example.com", startDate, endDate, granularity);
        assertTrue(result.isEmpty());
    }

    void userListAnalyticsHandlesNullStartDate() {
        LocalDateTime endDate = LocalDateTime.now();
        String granularity = "month";
        List<UserListAnalyticsDTO> result = userListTemplate.getAnalyticsList(UTENTE_1, null, endDate, granularity);
        assertFalse(result.isEmpty());
    }

    void userListAnalyticsHandlesNullEndDate() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        String granularity = "month";
        List<UserListAnalyticsDTO> result = userListTemplate.getAnalyticsList(UTENTE_1, startDate, null, granularity);
        assertFalse(result.isEmpty());
    }

    void userListAnalyticsHandlesNullStartAndEndDate() {
        String granularity = "year";
        List<UserListAnalyticsDTO> result = userListTemplate.getAnalyticsList(UTENTE_1, null, null, granularity);
        assertFalse(result.isEmpty());
    }

    void userListAnalyticsHandlesInvalidGranularity() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        String granularity = "invalidGranularity";
        assertThrows(IllegalArgumentException.class, () -> userListTemplate.getAnalyticsList(UTENTE_1, startDate, endDate, granularity));
    }
}
