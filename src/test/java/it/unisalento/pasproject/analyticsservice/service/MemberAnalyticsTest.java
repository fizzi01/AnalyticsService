package it.unisalento.pasproject.analyticsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.AnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.MemberAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.MemberListAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.UserAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.repositories.AssignedResourceRepository;
import it.unisalento.pasproject.analyticsservice.repositories.AssignmentAnalyticsRepository;
import it.unisalento.pasproject.analyticsservice.service.Template.MemberListTemplate;
import it.unisalento.pasproject.analyticsservice.service.Template.MemberTemplate;
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
@Import({MemberTemplate.class, MemberListTemplate.class})
class MemberAnalyticsTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AssignedResourceRepository assignedResourceRepository;

    @Autowired
    private AssignmentAnalyticsRepository assignmentAnalyticsRepository;

    @Autowired
    private MemberTemplate memberTemplate;

    @Autowired
    private MemberListTemplate memberListTemplate;


    private static final String UTENTE_1 = "user1@example.com";
    private static final String MEMBRO_1 = "member1@example.com";

    private final Logger LOGGER = LoggerFactory.getLogger(CalculateAnalyticsService.class);

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
    void getMemberAnalyticsShouldReturnDataForValidEmail() {
        List<AssignedResource> resources = assignedResourceRepository.findAll();
        assertFalse(resources.isEmpty());
        Optional<MemberAnalyticsDTO> memberAnalyticsDTO = memberTemplate.getAnalytics(MEMBRO_1, null, null);

        assertTrue(memberAnalyticsDTO.isPresent());

        assertEquals(108300.0, memberAnalyticsDTO.get().getComputingPower());
        assertEquals(535, memberAnalyticsDTO.get().getEnergyConsumed());
        assertEquals(2940, Math.floor(memberAnalyticsDTO.get().getWorkMinutes()));
        assertEquals(4, memberAnalyticsDTO.get().getTasksCompleted());
        assertEquals(MEMBRO_1, memberAnalyticsDTO.get().getMemberEmail());
    }

    @Test
    void getMemberAnalyticsShouldReturnEmptyForNonExistentEmail() {
        String nonExistentEmail = "nonexistent@example.com";
        Optional<MemberAnalyticsDTO> result =  memberTemplate.getAnalytics(nonExistentEmail, null, null);
        assertFalse(result.isPresent());
    }

    @Test
    void getMemberMonthlyAnalyticsShouldReturnDataForValidEmailAndGranularity() throws JsonProcessingException {
        LocalDateTime startDate = LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.now();
        List<MemberListAnalyticsDTO> result = memberListTemplate.getAnalyticsList(MEMBRO_1, startDate, endDate, "month");
        LOGGER.info("Result Monthly: {}", new ObjectMapper().writeValueAsString(result));
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    void getMemberMonthlyAnalyticsShouldThrowException() {
        LocalDateTime startDate = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.now();

        assertThrows(NullPointerException.class, () -> {
            memberListTemplate.getAnalyticsList(MEMBRO_1, startDate, endDate, "invalid");
        });
    }

    @Test
    void getMemberDailyAnalyticsShouldReturnDataForValidEmailAndGranularity() throws JsonProcessingException {
        LocalDateTime startDate = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.now();
        List<MemberListAnalyticsDTO> result = memberListTemplate.getAnalyticsList(MEMBRO_1, startDate, endDate, "day");
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    void getMemberMonthlyAnalyticsShouldReturnEmptyListForInvalidEmail() {
        LocalDateTime startDate = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.now();
        List<MemberListAnalyticsDTO> result = memberListTemplate.getAnalyticsList("invalid@example.com",startDate, endDate, "month");
        assertTrue(result.isEmpty());
    }




}
