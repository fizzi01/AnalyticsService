package it.unisalento.pasproject.analyticsservice.service;

import it.unisalento.pasproject.analyticsservice.dto.AnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.MemberAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.UserAnalyticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CalculateAnalyticsService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public CalculateAnalyticsService(MongoTemplate mongoTemplate) {

        this.mongoTemplate = mongoTemplate;
    }

    public Optional<MemberAnalyticsDTO> getMemberAnalytics(String memberEmail) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("memberEmail").is(memberEmail));

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude("memberEmail")
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasCompleted").equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf("completedTime").subtract("assignedTime")).otherwise(0)).as("workDuration")
                .andInclude("assignedEnergyConsumptionPerHour","hasCompleted")
                .and(ArithmeticOperators.Add.valueOf("assignedSingleScore")
                        .add("assignedMultiScore")
                        .add("assignedOpenclScore")
                        .add("assignedVulkanScore")
                        .add("assignedCudaScore")).as("totalComputingPower")
                .andInclude("assignedTime");

        GroupOperation groupOperation = Aggregation.group("memberEmail")
                .first("memberEmail").as("memberEmail")
                .sum("workDuration").as("totalWorkDuration")
                .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                .sum("totalComputingPower").as("computingPower")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasCompleted").equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasCompleted").equalToValue(false)).then(1).otherwise(0)).as("tasksInProgress")
                .count().as("tasksAssigned")
                .min("assignedTime").as("startDate")
                .max("assignedTime").as("endDate");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectOperation,
                groupOperation,
                Aggregation.project("memberEmail", "totalWorkDuration", "energyConsumed", "computingPower", "tasksCompleted", "tasksInProgress", "tasksAssigned", "startDate", "endDate")
                        .andExpression("totalWorkDuration / 3600000").as("workHours") // Convert milliseconds to hours
        );

        AggregationResults<MemberAnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assigned_resource_analytics", MemberAnalyticsDTO.class);

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
                .andInclude("memberEmail")
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasCompleted").equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf("completedTime").subtract("assignedTime")).otherwise(0)).as("workDuration")
                .andInclude("assignedEnergyConsumptionPerHour","hasCompleted")
                .and(ArithmeticOperators.Add.valueOf("assignedSingleScore")
                        .add("assignedMultiScore")
                        .add("assignedOpenclScore")
                        .add("assignedVulkanScore")
                        .add("assignedCudaScore")).as("totalComputingPower")
                .andInclude("assignedTime");

        GroupOperation groupOperation = Aggregation.group("memberEmail")
                .first("memberEmail").as("memberEmail")
                .sum("workDuration").as("totalWorkDuration")
                .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                .sum("totalComputingPower").as("computingPower")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasCompleted").equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasCompleted").equalToValue(false)).then(1).otherwise(0)).as("tasksInProgress")
                .count().as("tasksAssigned")
                .min("assignedTime").as("startDate")
                .max("assignedTime").as("endDate");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectOperation,
                groupOperation,
                Aggregation.project("memberEmail", "totalWorkDuration", "energyConsumed", "computingPower", "tasksCompleted", "tasksInProgress", "tasksAssigned", "startDate", "endDate")
                        .andExpression("totalWorkDuration / 3600000").as("workHours") // Convert milliseconds to hours
        );

        AggregationResults<MemberAnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assigned_resource_analytics", MemberAnalyticsDTO.class);

        return results.getMappedResults().stream().findFirst();
    }

    public Optional<UserAnalyticsDTO> getUserAnalytics(String emailUtente) {
        MatchOperation matchOperation = Aggregation.match(Criteria.where("emailUtente").is(emailUtente));

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
                .andInclude("assignedEnergyConsumptionPerHour", "assignedSingleScore", "assignedMultiScore", "assignedOpenclScore", "assignedVulkanScore", "assignedCudaScore", "memberEmail", "assignedTime", "completedTime", "isComplete")
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
                lookupOperation,
                projectOperation,
                groupOperation,
                Aggregation.project("energyConsumed", "computingPowerUsed", "tasksSubmitted", "tasksCompleted", "startDate", "endDate")
                        .andExpression("totalWorkDuration / 3600000").as("workHours") // Convert milliseconds to hours
                        .and(ArrayOperators.Size.lengthOfArray("uniqueMembers")).as("activeMemberCount")
                        .and(ArrayOperators.Size.lengthOfArray("uniqueUsers")).as("activeUserCount")
        );

        AggregationResults<AnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assigned_resource_analytics", AnalyticsDTO.class);

        return results.getUniqueMappedResult();
    }

    public AnalyticsDTO getOverallAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        LookupOperation lookupOperation = Aggregation.lookup("assignment_analytics", "taskId", "taskId", "assignments");

        MatchOperation matchOperation = Aggregation.match(Criteria.where("assignedTime").gte(startDate).and("assignedTime").lte(endDate));

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude("assignedEnergyConsumptionPerHour", "assignedSingleScore", "assignedMultiScore", "assignedOpenclScore", "assignedVulkanScore", "assignedCudaScore", "memberEmail", "assignedTime", "completedTime", "isComplete")
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

        return results.getUniqueMappedResult();
    }
}