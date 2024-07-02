package it.unisalento.pasproject.analyticsservice.service;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.AnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.MemberAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.UserAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.repositories.AssignmentAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import static it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CalculateAnalyticsService {

    private final MongoTemplate mongoTemplate;
    private final AssignmentAnalyticsRepository assignmentAnalyticsRepository;

    //LOgger factory
    private static final Logger LOGGER = LoggerFactory.getLogger(CalculateAnalyticsService.class);

    @Autowired
    public CalculateAnalyticsService(MongoTemplate mongoTemplate,
                                        AssignmentAnalyticsRepository assignmentAnalyticsRepository
    ) {
        this.mongoTemplate = mongoTemplate;
        this.assignmentAnalyticsRepository = assignmentAnalyticsRepository;
    }

    public Optional<MemberAnalyticsDTO> getMemberAnalytics(String memberEmail) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(EMAIL_MEMBER_FIELD).is(memberEmail));

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude(EMAIL_MEMBER_FIELD)
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)).otherwise(0)).as(WORK_DURATION_FIELD)
                .andInclude(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD,HAS_COMPLETED_FIELD)
                .and(ArithmeticOperators.Add.valueOf(ASSIGNED_SINGLE_SCORE_FIELD)
                        .add(ASSIGNED_MULTI_SCORE_FIELD)
                        .add(ASSIGNED_OPENCL_SCORE_FIELD)
                        .add(ASSIGNED_VULKAN_SCORE_FIELD)
                        .add(ASSIGNED_CUDA_SCORE_FIELD)).as(TOTAL_COMPUTING_POWER_FIELD)
                .andInclude(ASSIGNED_TIME_FIELD);

        GroupOperation groupOperation = Aggregation.group(EMAIL_MEMBER_FIELD)
                .first(EMAIL_MEMBER_FIELD).as(EMAIL_MEMBER_FIELD)
                .sum(WORK_DURATION_FIELD).as(TOTAL_WORK_DURATION_FIELD)
                .sum(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD).as(ENERGY_CONSUMED_FIELD)
                .sum(TOTAL_COMPUTING_POWER_FIELD).as(COMPUTING_POWER_FIELD)
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(true)).then(1).otherwise(0)).as(TASKS_COMPLETED_FIELD)
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(false)).then(1).otherwise(0)).as(TASKS_IN_PROGRESS_FIELD)
                .count().as(TASKS_ASSIGNED_FIELD)
                .min(ASSIGNED_TIME_FIELD).as(START_DATE_FIELD)
                .max(ASSIGNED_TIME_FIELD).as(END_DATE_FIELD);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectOperation,
                groupOperation,
                Aggregation.project(EMAIL_MEMBER_FIELD, TOTAL_WORK_DURATION_FIELD, ENERGY_CONSUMED_FIELD, COMPUTING_POWER_FIELD, TASKS_COMPLETED_FIELD, TASKS_IN_PROGRESS_FIELD, TASKS_ASSIGNED_FIELD, START_DATE_FIELD, END_DATE_FIELD)
                        .andExpression(TOTAL_WORK_DURATION_FIELD + " / 3600000").as(WORK_HOURS_FIELD) // Convert milliseconds to hours
        );

        AggregationResults<MemberAnalyticsDTO> results = mongoTemplate.aggregate(aggregation, mongoTemplate.getCollectionName(AssignedResource.class), MemberAnalyticsDTO.class);

        return results.getMappedResults().stream().findFirst();
    }

    public Optional<MemberAnalyticsDTO> getMemberAnalytics(String memberEmail, LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("memberEmail").is(memberEmail));

        if (startDate != null && endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(memberEmail)
                    .and("assignedTime").gte(startDate)
                    .and("assignedTime").lte(endDate));
        } else if (startDate != null) {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(memberEmail)
                    .and("assignedTime").gte(startDate));
        } else if (endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(memberEmail)
                    .and("assignedTime").lte(endDate));
        }

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude(EMAIL_MEMBER_FIELD)
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)).otherwise(0)).as(WORK_DURATION_FIELD)
                .andInclude(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD,HAS_COMPLETED_FIELD)
                .and(ArithmeticOperators.Add.valueOf(ASSIGNED_SINGLE_SCORE_FIELD)
                        .add(ASSIGNED_MULTI_SCORE_FIELD)
                        .add(ASSIGNED_OPENCL_SCORE_FIELD)
                        .add(ASSIGNED_VULKAN_SCORE_FIELD)
                        .add(ASSIGNED_CUDA_SCORE_FIELD)).as(TOTAL_COMPUTING_POWER_FIELD)
                .andInclude(ASSIGNED_TIME_FIELD);

        GroupOperation groupOperation = Aggregation.group(EMAIL_MEMBER_FIELD)
                .first(EMAIL_MEMBER_FIELD).as(EMAIL_MEMBER_FIELD)
                .sum(WORK_DURATION_FIELD).as(TOTAL_WORK_DURATION_FIELD)
                .sum(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD).as(ENERGY_CONSUMED_FIELD)
                .sum(TOTAL_COMPUTING_POWER_FIELD).as(COMPUTING_POWER_FIELD)
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(true)).then(1).otherwise(0)).as(TASKS_COMPLETED_FIELD)
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(false)).then(1).otherwise(0)).as(TASKS_IN_PROGRESS_FIELD)
                .count().as(TASKS_ASSIGNED_FIELD)
                .min(ASSIGNED_TIME_FIELD).as(START_DATE_FIELD)
                .max(ASSIGNED_TIME_FIELD).as(END_DATE_FIELD);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectOperation,
                groupOperation,
                Aggregation.project(EMAIL_MEMBER_FIELD, TOTAL_WORK_DURATION_FIELD, ENERGY_CONSUMED_FIELD, COMPUTING_POWER_FIELD, TASKS_COMPLETED_FIELD, TASKS_IN_PROGRESS_FIELD, TASKS_ASSIGNED_FIELD, START_DATE_FIELD, END_DATE_FIELD)
                        .andExpression(TOTAL_WORK_DURATION_FIELD + " / 3600000").as(WORK_HOURS_FIELD) // Convert milliseconds to hours
        );

        AggregationResults<MemberAnalyticsDTO> results = mongoTemplate.aggregate(aggregation, mongoTemplate.getCollectionName(AssignedResource.class), MemberAnalyticsDTO.class);

        return results.getMappedResults().stream().findFirst();
    }

    public Optional<UserAnalyticsDTO> getTaskUserAnalytics(String taskId){
MatchOperation matchOperation = Aggregation.match(Criteria.where(ASSIGNMENT_TASK_ID_FIELD).is(taskId));

        LookupOperation lookupOperation = Aggregation.lookup(mongoTemplate.getCollectionName(AssignedResource.class), RES_TASK_ID_FIELD, ASSIGNMENT_TASK_ID_FIELD, "assignedResources");

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude(ASSIGNMENT_TASK_ID_FIELD)
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(IS_COMPLETE_FIELD).equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)).otherwise(0)).as("timeSpent")
                .andInclude(ASSIGNED_TIME_FIELD, COMPLETED_TIME_FIELD, IS_COMPLETE_FIELD)
                .and("assignedResources.assignedEnergyConsumptionPerHour").as(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD)
                .and(ArithmeticOperators.Add.valueOf("assignedResources.assignedSingleScore")
                        .add("assignedResources.assignedMultiScore")
                        .add("assignedResources.assignedOpenclScore")
                        .add("assignedResources.assignedVulkanScore")
                        .add("assignedResources.assignedCudaScore")).as("totalComputingPower");

        GroupOperation groupOperation = Aggregation.group(ASSIGNMENT_TASK_ID_FIELD)
                .first(ASSIGNMENT_TASK_ID_FIELD).as(ASSIGNMENT_TASK_ID_FIELD)
                .sum("timeSpent").as("totalTimeSpent")
                .sum("assignedEnergyConsumptionPerHour").as("energySaved")
                .sum("totalComputingPower").as("computingPowerUsed");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                lookupOperation,
                Aggregation.unwind("assignedResources", true),
                projectOperation,
                groupOperation,
                Aggregation.project(ASSIGNMENT_TASK_ID_FIELD, "totalTimeSpent", "energySaved", "computingPowerUsed")
                        .andExpression("totalTimeSpent / 3600000").as("timeSpentOnTasks"));// Convert milliseconds to hours

        LOGGER.info("Aggregation: {} " , aggregation);

        AggregationResults<UserAnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assignment_analytics", UserAnalyticsDTO.class);

        return results.getMappedResults().stream().findFirst();
    }

    public Optional<UserAnalyticsDTO> getUserAnalytics(String emailUtente) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where(USER_EMAIL_FIELD).is(emailUtente));

        LookupOperation lookupOperation = Aggregation.lookup(mongoTemplate.getCollectionName(AssignedResource.class), RES_TASK_ID_FIELD, ASSIGNMENT_TASK_ID_FIELD, "assignedResources");

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude(USER_EMAIL_FIELD)
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(IS_COMPLETE_FIELD).equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)).otherwise(0)).as("timeSpent")
                .andInclude(ASSIGNED_TIME_FIELD, COMPLETED_TIME_FIELD, IS_COMPLETE_FIELD)
                .and("assignedResources.assignedEnergyConsumptionPerHour").as(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD)
                .and(ArithmeticOperators.Add.valueOf("assignedResources.assignedSingleScore")
                        .add("assignedResources.assignedMultiScore")
                        .add("assignedResources.assignedOpenclScore")
                        .add("assignedResources.assignedVulkanScore")
                        .add("assignedResources.assignedCudaScore")).as("totalComputingPower");

        GroupOperation groupOperation = Aggregation.group("emailUtente")
                .first("emailUtente").as("userEmail")
                .sum("timeSpent").as("totalTimeSpent")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(false)).then(1).otherwise(0)).as("tasksOngoing")
                .count().as("tasksSubmitted")
                .min("assignedTime").as("startDate")
                .max("assignedTime").as("endDate")
                .sum("assignedEnergyConsumptionPerHour").as("energySaved")
                .sum("totalComputingPower").as("computingPowerUsed");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                lookupOperation,
                Aggregation.unwind("assignedResources", true),
                projectOperation,
                groupOperation,
                Aggregation.project("userEmail", "totalTimeSpent", "tasksCompleted", "tasksOngoing", "tasksSubmitted", "startDate", "endDate", "energySaved", "computingPowerUsed")
                        .andExpression("totalTimeSpent / 3600000").as("timeSpentOnTasks") // Convert milliseconds to hours
        );

        AggregationResults<UserAnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assignment_analytics", UserAnalyticsDTO.class);

        return results.getMappedResults().stream().findFirst();
    }

    public Optional<UserAnalyticsDTO> getUserAnalytics(String emailUtente, LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("emailUtente").is(emailUtente));

        if (startDate != null && endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(emailUtente)
                    .andOperator(Criteria.where("assignedTime").gte(startDate),
                            Criteria.where("assignedTime").lte(endDate)));
        } else if (startDate != null) {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(emailUtente)
                    .and("assignedTime").gte(startDate));
        } else if (endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(emailUtente)
                    .and("assignedTime").lte(endDate));
        }


        LookupOperation lookupOperation = Aggregation.lookup("assigned_resource_analytics", "taskId", "taskId", "assignedResources");

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude("emailUtente")
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf("completedTime").subtract("assignedTime")).otherwise(0)).as("timeSpent")
                .andInclude("assignedTime", "completedTime", "isComplete")
                .and("assignedResources.assignedEnergyConsumptionPerHour").as("assignedEnergyConsumptionPerHour")
                .and(ArithmeticOperators.Add.valueOf("assignedResources.assignedSingleScore")
                        .add("assignedResources.assignedMultiScore")
                        .add("assignedResources.assignedOpenclScore")
                        .add("assignedResources.assignedVulkanScore")
                        .add("assignedResources.assignedCudaScore")).as("totalComputingPower");

        GroupOperation groupOperation = Aggregation.group("emailUtente")
                .first("emailUtente").as("userEmail")
                .sum("timeSpent").as("totalTimeSpent")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(false)).then(1).otherwise(0)).as("tasksOngoing")
                .count().as("tasksSubmitted")
                .min("assignedTime").as("startDate")
                .max("assignedTime").as("endDate")
                .sum("assignedEnergyConsumptionPerHour").as("energySaved")
                .sum("totalComputingPower").as("computingPowerUsed");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                lookupOperation,
                Aggregation.unwind("assignedResources", true),
                projectOperation,
                groupOperation,
                Aggregation.project("userEmail", "totalTimeSpent", "tasksCompleted", "tasksOngoing", "tasksSubmitted", "startDate", "endDate", "energySaved", "computingPowerUsed")
                        .andExpression("totalTimeSpent / 3600000").as("timeSpentOnTasks") // Convert milliseconds to hours
        );

        AggregationResults<UserAnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assignment_analytics", UserAnalyticsDTO.class);

        return results.getMappedResults().stream().findFirst();
    }

    public AnalyticsDTO getOverallAnalytics() {
        LookupOperation lookupOperation = Aggregation.lookup("assignment_analytics", "taskId", "taskId", "assignments");

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude("taskId","assignedTime","completedTime","assignedEnergyConsumptionPerHour", "assignedSingleScore", "assignedMultiScore", "assignedOpenclScore", "assignedVulkanScore", "assignedCudaScore", "memberEmail", "emailUtente", "hasCompleted")
                .andExpression("{$sum: '$assignments'}").as("tasksSubmitted")
                .andExpression("assignedTime").as("assignedTime")
                .andExpression("completedTime").as("completedTime")
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasCompleted").equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf("completedTime").subtract("assignedTime")).otherwise(0)).as("workDuration")
                .and(ArithmeticOperators.Add.valueOf("assignedSingleScore")
                        .add("assignedMultiScore")
                        .add("assignedOpenclScore")
                        .add("assignedVulkanScore")
                        .add("assignedCudaScore")).as("computingPower");

        GroupOperation groupOperation = Aggregation.group()
                .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                .sum("computingPower").as("computingPowerUsed")
                .addToSet("memberEmail").as("uniqueMembers")
                .addToSet("emailUtente").as("uniqueUsers")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasCompleted").equalToValue(true))
                        .then(1).otherwise(0)).as("tasksCompleted")
                .count().as("tasksSubmitted")
                .sum("workDuration").as("totalWorkDuration")
                .min("assignedTime").as("startDate")
                .max("assignedTime").as("endDate");

        Aggregation aggregation = Aggregation.newAggregation(
                lookupOperation,
                projectOperation,
                groupOperation,
                Aggregation.project("energyConsumed", "computingPowerUsed", "tasksSubmitted", "tasksCompleted", "startDate", "endDate")
                        .andExpression("totalWorkDuration / 360000").as("workHours") // Convert milliseconds to hours
                        .and(ArrayOperators.Size.lengthOfArray("uniqueMembers")).as("activeMemberCount")
                        .and(ArrayOperators.Size.lengthOfArray("uniqueUsers")).as("activeUserCount")
        );

        AggregationResults<AnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assigned_resource_analytics", AnalyticsDTO.class);

        AnalyticsDTO analyticsDTO = results.getUniqueMappedResult();

        analyticsDTO = getAssignedTasksInfo(analyticsDTO, null, null);

        return analyticsDTO;
    }

    private AnalyticsDTO getAssignedTasksInfo(AnalyticsDTO analyticsDTO, LocalDateTime startDate, LocalDateTime endDate) {
        if(analyticsDTO == null) {
            return null;
        }

        List<AssignmentAnalytics> allAssignments = assignmentAnalyticsRepository.findAll();

        //Find number of unique members email
        analyticsDTO.setActiveUserCount((int) allAssignments.stream().map(AssignmentAnalytics::getEmailUtente).distinct().count());

        //Find number of tasks submitted
        analyticsDTO.setTasksSubmitted(assignmentAnalyticsRepository.findAll().size());

        analyticsDTO.setTasksCompleted((int) allAssignments.stream().filter(AssignmentAnalytics::isComplete).count());

        if(startDate != null && endDate != null) {
            analyticsDTO.setStartDate(startDate);
            analyticsDTO.setEndDate(endDate);
        } else {
            try{
                analyticsDTO.setStartDate(allAssignments.stream().map(AssignmentAnalytics::getAssignedTime).min(LocalDateTime::compareTo).orElse(null));
                analyticsDTO.setEndDate(allAssignments.stream().map(AssignmentAnalytics::getAssignedTime).max(LocalDateTime::compareTo).orElse(null));
            }catch (Exception e){
                LOGGER.error("Error while calculating start and end date for analytics");
            }
        }

        return analyticsDTO;

    }

    public AnalyticsDTO getOverallAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        LookupOperation lookupOperation = Aggregation.lookup("assignment_analytics", "taskId", "taskId", "assignments");

        MatchOperation matchOperation = Aggregation.match(Criteria.where("assignedTime").gte(startDate).and("assignedTime").lte(endDate));

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude("taskId","assignedEnergyConsumptionPerHour", "assignedSingleScore", "assignedMultiScore", "assignedOpenclScore", "assignedVulkanScore", "assignedCudaScore", "memberEmail", "assignedTime", "completedTime", "isComplete")
                .andExpression("{$sum: '$assignments'}").as("assignments")
                .andExpression("assignedTime").as("assignedTime")
                .andExpression("completedTime").as("completedTime")
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf("completedTime").subtract("assignedTime")).otherwise(0)).as("workDuration")
                .and(ArithmeticOperators.Add.valueOf("assignedSingleScore")
                        .add("assignedMultiScore")
                        .add("assignedOpenclScore")
                        .add("assignedVulkanScore")
                        .add("assignedCudaScore")).as("computingPower");

        GroupOperation groupOperation = Aggregation.group()
                .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                .sum("computingPower").as("computingPowerUsed")
                .addToSet("memberEmail").as("uniqueMembers")
                .addToSet("assignments.emailUtente").as("uniqueUsers")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("assignments.isComplete").equalToValue(true))
                        .then(1).otherwise(0)).as("tasksCompleted")
                .count().as("tasksSubmitted")
                .sum("workDuration").as("totalWorkDuration")
                .min("assignedTime").as("startDate")
                .max("assignedTime").as("endDate");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                lookupOperation,
                projectOperation,
                groupOperation,
                Aggregation.project("energyConsumed", "computingPowerUsed", "activeMemberCount", "activeUserCount", "tasksSubmitted", "tasksCompleted", "startDate", "endDate")
                        .andExpression("totalWorkDuration / 3600000").as("workHours") // Convert milliseconds to hours
                        .and(ArrayOperators.Size.lengthOfArray("uniqueMembers")).as("activeMemberCount")
                        .and(ArrayOperators.Size.lengthOfArray("uniqueUsers")).as("activeUserCount")
        );

        AggregationResults<AnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assigned_resource_analytics", AnalyticsDTO.class);

        AnalyticsDTO analyticsDTO = results.getUniqueMappedResult();

        analyticsDTO = getAssignedTasksInfo(analyticsDTO, startDate, endDate);

        return analyticsDTO;
    }
}