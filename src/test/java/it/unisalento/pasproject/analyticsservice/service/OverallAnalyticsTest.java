package it.unisalento.pasproject.analyticsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.AdminListAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.AnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.MemberAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.repositories.AssignedResourceRepository;
import it.unisalento.pasproject.analyticsservice.repositories.AssignmentAnalyticsRepository;
import it.unisalento.pasproject.analyticsservice.service.Template.MemberListTemplate;
import it.unisalento.pasproject.analyticsservice.service.Template.MemberTemplate;
import it.unisalento.pasproject.analyticsservice.service.Template.OverallAnalyticsTemplate;
import it.unisalento.pasproject.analyticsservice.service.Template.OverallListTemplate;
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
@Import({OverallAnalyticsTemplate.class, OverallListTemplate.class})
class OverallAnalyticsTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AssignedResourceRepository assignedResourceRepository;

    @Autowired
    private AssignmentAnalyticsRepository assignmentAnalyticsRepository;

    @Autowired
    private OverallAnalyticsTemplate overallAnalyticsTemplate;

    @Autowired
    private OverallListTemplate overallListTemplate;

    private static final String UTENTE_1 = "user1@example.com";
    private static final String MEMBRO_1 = "member1@example.com";

    private final Logger LOGGER = LoggerFactory.getLogger(OverallAnalyticsTest.class);

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
    void analyticsForMultipleAssignmentsShouldAggregateCorrectly() {
        // Add another assignment for MEMBRO_1 with different scores and energy consumption
        AssignmentAnalytics newAssignment = new AssignmentAnalytics();
        newAssignment.setAssignedTime(LocalDateTime.now().minusDays(3));
        newAssignment.setEmailUtente(MEMBRO_1);
        newAssignment.setTaskId("2");
        newAssignment.setAssignedTime(LocalDateTime.now().minusDays(2).minusMinutes(60));
        newAssignment.setCompletedTime(LocalDateTime.now().minusDays(2));
        newAssignment.setComplete(true);
        newAssignment.setLastUpdate(LocalDateTime.now().minusDays(2));

        newAssignment = assignmentAnalyticsRepository.save(newAssignment);

        AssignedResource newResource = new AssignedResource();
        newResource.setHardwareId("gpu2");
        newResource.setTaskId(newAssignment.getTaskId());
        newResource.setHardwareName("AMD Radeon RX 6900 XT");
        newResource.setAssignedEnergyConsumptionPerHour(300.0);
        newResource.setMemberEmail(MEMBRO_1);
        newResource.setAssignedSingleScore(2500);
        newResource.setAssignedMultiScore(9500);
        newResource.setAssignedOpenclScore(12000);
        newResource.setAssignedVulkanScore(11500);
        newResource.setAssignedCudaScore(0); // Assuming AMD GPUs do not have CUDA scores
        newResource.setAssignedTime(LocalDateTime.now().minusDays(2).minusMinutes(55));
        newResource.setCompletedTime(LocalDateTime.now().minusDays(2).minusMinutes(30));
        newResource.setHasCompleted(true);

        assignedResourceRepository.save(newResource);

        Optional<AnalyticsDTO> updatedAnalyticsDTO = overallAnalyticsTemplate.getAnalytics(MEMBRO_1, null, null);

        assertTrue(updatedAnalyticsDTO.isPresent());
        assertEquals(143800.0, updatedAnalyticsDTO.get().getComputingPowerUsed());
        assertEquals(835.0, updatedAnalyticsDTO.get().getEnergyConsumed());
        assertEquals(2965, Math.floor(updatedAnalyticsDTO.get().getWorkMinutes()));
    }

    @Test
    void analyticsShouldHandleIncompleteAssignmentsCorrectly() {
        // Add an incomplete assignment for MEMBRO_1
        AssignmentAnalytics incompleteAssignment = new AssignmentAnalytics();
        incompleteAssignment.setAssignedTime(LocalDateTime.now().minusDays(1));
        incompleteAssignment.setEmailUtente(MEMBRO_1);
        incompleteAssignment.setTaskId("3");
        incompleteAssignment.setAssignedTime(LocalDateTime.now().minusMinutes(45));
        incompleteAssignment.setCompletedTime(null); // Incomplete
        incompleteAssignment.setComplete(false);
        incompleteAssignment.setLastUpdate(LocalDateTime.now().minusMinutes(45));

        incompleteAssignment = assignmentAnalyticsRepository.save(incompleteAssignment);

        AssignedResource incompleteResource = new AssignedResource();
        incompleteResource.setHardwareId("gpu3");
        incompleteResource.setTaskId(incompleteAssignment.getTaskId());
        incompleteResource.setHardwareName("NVIDIA GTX 1060");
        incompleteResource.setAssignedEnergyConsumptionPerHour(120.0);
        incompleteResource.setMemberEmail(MEMBRO_1);
        incompleteResource.setAssignedSingleScore(1000);
        incompleteResource.setAssignedMultiScore(4000);
        incompleteResource.setAssignedOpenclScore(5000);
        incompleteResource.setAssignedVulkanScore(4800);
        incompleteResource.setAssignedCudaScore(5200);
        incompleteResource.setAssignedTime(LocalDateTime.now().minusMinutes(45));
        incompleteResource.setCompletedTime(null); // Incomplete
        incompleteResource.setHasCompleted(false);

        assignedResourceRepository.save(incompleteResource);

        Optional<AnalyticsDTO> analyticsWithIncomplete = overallAnalyticsTemplate.getAnalytics(MEMBRO_1, null, null);

        assertTrue(analyticsWithIncomplete.isPresent());
        // Ensure the incomplete assignment does not affect the completed tasks count and energy consumed
        assertEquals(2940.0, Math.floor(analyticsWithIncomplete.get().getWorkMinutes()));
        assertEquals(655, analyticsWithIncomplete.get().getEnergyConsumed());
    }

    @Test
    void analyticsShouldReturnZeroForAllMetricsWhenNoAssignmentsExist() {
        cleanUp();
        Optional<AnalyticsDTO> emptyAnalyticsDTO = overallAnalyticsTemplate.getAnalytics(MEMBRO_1, null, null);

        assertFalse(emptyAnalyticsDTO.isPresent());
    }

    @Test
    void overallAnalyticsReturnsDataForValidDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        Optional<AnalyticsDTO> result = overallAnalyticsTemplate.getAnalytics(null, startDate, endDate);
        assertTrue(result.isPresent());
        assertTrue(result.get().getComputingPowerUsed() > 0);
        assertTrue(result.get().getEnergyConsumed() > 0);
        assertTrue(result.get().getWorkMinutes() > 0);
    }

    @Test
    void overallAnalyticsReturnsEmptyForInvalidDateRange() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);
        Optional<AnalyticsDTO> result = overallAnalyticsTemplate.getAnalytics(null, startDate, endDate);
        assertFalse(result.isPresent());
    }

    @Test
    void overallAnalyticsHandlesNullStartDate() {
        LocalDateTime endDate = LocalDateTime.now();
        Optional<AnalyticsDTO> result = overallAnalyticsTemplate.getAnalytics(null, null, endDate);
        assertTrue(result.isPresent());
    }

    @Test
    void overallAnalyticsHandlesNullEndDate() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        Optional<AnalyticsDTO> result = overallAnalyticsTemplate.getAnalytics(null, startDate, null);
        assertTrue(result.isPresent());
    }

    @Test
    void overallAnalyticsHandlesNullStartAndEndDate() {
        Optional<AnalyticsDTO> result = overallAnalyticsTemplate.getAnalytics(null, null, null);
        assertTrue(result.isPresent());
    }

    @Test
    void overallListAnalyticsReturnsDataForValidDateRangeAndGranularity() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        String granularity = "day";
        List<AdminListAnalyticsDTO> result = overallListTemplate.getAnalyticsList(null, startDate, endDate, granularity);
        assertFalse(result.isEmpty());
    }

    @Test
    void overallListAnalyticsReturnsEmptyListForFutureDateRange() {
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusMonths(1);
        String granularity = "day";
        List<AdminListAnalyticsDTO> result = overallListTemplate.getAnalyticsList(null, startDate, endDate, granularity);
        assertTrue(result.isEmpty());
    }

    @Test
    void overallListAnalyticsHandlesNullStartDate() {
        LocalDateTime endDate = LocalDateTime.now();
        String granularity = "month";
        List<AdminListAnalyticsDTO> result = overallListTemplate.getAnalyticsList(null, null, endDate, granularity);
        assertFalse(result.isEmpty());
    }

    @Test
    void overallListAnalyticsHandlesNullEndDate() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        String granularity = "month";
        List<AdminListAnalyticsDTO> result = overallListTemplate.getAnalyticsList(null, startDate, null, granularity);
        assertFalse(result.isEmpty());
    }

    @Test
    void overallListAnalyticsHandlesInvalidGranularity() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        String granularity = "invalidGranularity";
        assertThrows(NullPointerException.class, () -> overallListTemplate.getAnalyticsList(null, startDate, endDate, granularity));
    }

    @Test
    void overallListAnalyticsHandlesNullStartAndEndDateWithValidGranularity() {
        String granularity = "day";
        List<AdminListAnalyticsDTO> result = overallListTemplate.getAnalyticsList(null, null, null, granularity);
        assertFalse(result.isEmpty());
    }


}
