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
                .andExpression("{$cond: {if: {$eq: ['$hasCompleted', true]}, then: {$subtract: ['$completedTime', '$assignedTime']}, else: 0}}").as("workDuration")
                .andInclude("assignedEnergyConsumptionPerHour")
                .andExpression("{$add: ['$assignedSingleScore', '$assignedMultiScore', '$assignedOpenclScore', '$assignedVulkanScore', '$assignedCudaScore']}").as("totalComputingPower")
                .andInclude("assignedTime");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectOperation,
                Aggregation.group("memberEmail")
                        .sum("workDuration").as("totalWorkDuration")
                        .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                        .sum("totalComputingPower").as("computingPower")
                        .sum("{$cond: {if: {$eq: ['$hasCompleted', true]}, then: 1, else: 0}}").as("tasksCompleted")
                        .sum("{$cond: {if: {$eq: ['$hasCompleted', false]}, then: 1, else: 0}}").as("tasksInProgress")
                        .count().as("tasksAssigned")
                        .min("assignedTime").as("startDate")
                        .max("assignedTime").as("endDate"),
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
                .andExpression("{$cond: {if: {$eq: ['$hasCompleted', true]}, then: {$subtract: ['$completedTime', '$assignedTime']}, else: 0}}").as("workDuration")
                .andInclude("assignedEnergyConsumptionPerHour")
                .andExpression("{$add: ['$assignedSingleScore', '$assignedMultiScore', '$assignedOpenclScore', '$assignedVulkanScore', '$assignedCudaScore']}").as("totalComputingPower")
                .andInclude("assignedTime");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectOperation,
                Aggregation.group("memberEmail")
                        .sum("workDuration").as("totalWorkDuration")
                        .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                        .sum("totalComputingPower").as("computingPower")
                        .sum("{$cond: {if: {$eq: ['$hasCompleted', true]}, then: 1, else: 0}}").as("tasksCompleted")
                        .sum("{$cond: {if: {$eq: ['$hasCompleted', false]}, then: 1, else: 0}}").as("tasksInProgress")
                        .count().as("tasksAssigned")
                        .min("assignedTime").as("startDate")
                        .max("assignedTime").as("endDate"),
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
                .andExpression("{$cond: {if: {$eq: ['$isComplete', true]}, then: {$subtract: ['$completedTime', '$assignedTime']}, else: 0}}").as("timeSpent")
                .andInclude("assignedTime", "completedTime", "isComplete")
                .andExpression("{$sum: '$assignedResources.assignedEnergyConsumptionPerHour'}").as("totalEnergySaved")
                .andExpression("{$sum: {$add: ['$assignedResources.assignedSingleScore', '$assignedResources.assignedMultiScore', '$assignedResources.assignedOpenclScore', '$assignedResources.assignedVulkanScore', '$assignedResources.assignedCudaScore']}}").as("totalComputingPower");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                lookupOperation,
                projectOperation,
                Aggregation.group("emailUtente")
                        .sum("timeSpent").as("totalTimeSpent")
                        .sum("{$cond: {if: {$eq: ['$isComplete', true]}, then: 1, else: 0}}").as("tasksCompleted")
                        .sum("{$cond: {if: {$eq: ['$isComplete', false]}, then: 1, else: 0}}").as("tasksOngoing")
                        .count().as("tasksSubmitted")
                        .min("assignedTime").as("startDate")
                        .max("assignedTime").as("endDate")
                        .sum("totalEnergySaved").as("energySaved")
                        .sum("totalComputingPower").as("computingPowerUsed"),
                Aggregation.project("emailUtente", "totalTimeSpent", "tasksCompleted", "tasksOngoing", "tasksSubmitted", "startDate", "endDate", "energySaved", "computingPowerUsed")
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
                .andExpression("{$cond: {if: {$eq: ['$isComplete', true]}, then: {$subtract: ['$completedTime', '$assignedTime']}, else: 0}}").as("timeSpent")
                .andInclude("assignedTime", "completedTime", "isComplete")
                .andExpression("{$sum: '$assignedResources.assignedEnergyConsumptionPerHour'}").as("totalEnergySaved")
                .andExpression("{$sum: {$add: ['$assignedResources.assignedSingleScore', '$assignedResources.assignedMultiScore', '$assignedResources.assignedOpenclScore', '$assignedResources.assignedVulkanScore', '$assignedResources.assignedCudaScore']}}").as("totalComputingPower");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                lookupOperation,
                projectOperation,
                Aggregation.group("emailUtente")
                        .sum("timeSpent").as("totalTimeSpent")
                        .sum("{$cond: {if: {$eq: ['$isComplete', true]}, then: 1, else: 0}}").as("tasksCompleted")
                        .sum("{$cond: {if: {$eq: ['$isComplete', false]}, then: 1, else: 0}}").as("tasksOngoing")
                        .count().as("tasksSubmitted")
                        .min("assignedTime").as("startDate")
                        .max("assignedTime").as("endDate")
                        .sum("totalEnergySaved").as("energySaved")
                        .sum("totalComputingPower").as("computingPowerUsed"),
                Aggregation.project("emailUtente", "totalTimeSpent", "tasksCompleted", "tasksOngoing", "tasksSubmitted", "startDate", "endDate", "energySaved", "computingPowerUsed")
                        .andExpression("totalTimeSpent / 3600000").as("timeSpentOnTasks") // Convert milliseconds to hours
        );

        AggregationResults<UserAnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assignment_analytics", UserAnalyticsDTO.class);

        return results.getMappedResults().stream().findFirst();
    }

    public AnalyticsDTO getOverallAnalytics() {
        LookupOperation lookupOperation = Aggregation.lookup("assignment_analytics", "taskId", "taskId", "assignments");

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude("assignedEnergyConsumptionPerHour", "assignedSingleScore", "assignedMultiScore", "assignedOpenclScore", "assignedVulkanScore", "assignedCudaScore", "memberEmail")
                .andExpression("{$sum: '$assignments'}").as("assignments")
                .andExpression("assignedTime").as("assignedTime")
                .andExpression("completedTime").as("completedTime")
                .andExpression("{$cond: {if: {$eq: ['$isComplete', true]}, then: {$subtract: ['$completedTime', '$assignedTime']}, else: 0}}").as("workDuration")
                .andExpression("{$sum: {$add: ['$assignedResources.assignedSingleScore', '$assignedResources.assignedMultiScore', '$assignedResources.assignedOpenclScore', '$assignedResources.assignedVulkanScore', '$assignedResources.assignedCudaScore']}}").as("computingPowerUsed");

        GroupOperation groupOperation = Aggregation.group()
                .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                .sum("computingPowerUsed").as("computingPowerUsed")
                .addToSet("memberEmail").as("uniqueMembers")
                .addToSet("assignments.emailUtente").as("uniqueUsers")
                .sum("{$cond: {if: {$eq: ['$assignments.isComplete', true]}, then: 1, else: 0}}").as("tasksCompleted")
                .count().as("tasksSubmitted")
                .sum("workDuration").as("totalWorkDuration")
                .min("assignedTime").as("startDate")
                .max("assignedTime").as("endDate");

        Aggregation aggregation = Aggregation.newAggregation(
                lookupOperation,
                projectOperation,
                groupOperation,
                Aggregation.project("energyConsumed", "computingPowerUsed", "activeMemberCount", "activeUserCount", "tasksSubmitted", "tasksCompleted", "startDate", "endDate")
                        .andExpression("totalWorkDuration / 3600000").as("workHours") // Convert milliseconds to hours
                        .and("uniqueMembers").size().as("activeMemberCount")
                        .and("uniqueUsers").size().as("activeUserCount")
        );

        AggregationResults<AnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assigned_resource_analytics", AnalyticsDTO.class);

        return results.getUniqueMappedResult();
    }

    public AnalyticsDTO getOverallAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        LookupOperation lookupOperation = Aggregation.lookup("assignment_analytics", "taskId", "taskId", "assignments");

        MatchOperation matchOperation = Aggregation.match(Criteria.where("assignedTime").gte(startDate).and("assignedTime").lte(endDate));

        ProjectionOperation projectOperation = Aggregation.project()
                .andInclude("assignedEnergyConsumptionPerHour", "assignedSingleScore", "assignedMultiScore", "assignedOpenclScore", "assignedVulkanScore", "assignedCudaScore", "memberEmail")
                .andExpression("{$sum: '$assignments'}").as("assignments")
                .andExpression("assignedTime").as("assignedTime")
                .andExpression("completedTime").as("completedTime")
                .andExpression("{$cond: {if: {$eq: ['$isComplete', true]}, then: {$subtract: ['$completedTime', '$assignedTime']}, else: 0}}").as("workDuration")
                .andExpression("{$sum: {$add: ['$assignedResources.assignedSingleScore', '$assignedResources.assignedMultiScore', '$assignedResources.assignedOpenclScore', '$assignedResources.assignedVulkanScore', '$assignedResources.assignedCudaScore']}}").as("computingPowerUsed");

        GroupOperation groupOperation = Aggregation.group()
                .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                .sum("computingPowerUsed").as("computingPowerUsed")
                .addToSet("memberEmail").as("uniqueMembers")
                .addToSet("assignments.emailUtente").as("uniqueUsers")
                .sum("{$cond: {if: {$eq: ['$assignments.isComplete', true]}, then: 1, else: 0}}").as("tasksCompleted")
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
                        .and("uniqueMembers").size().as("activeMemberCount")
                        .and("uniqueUsers").size().as("activeUserCount")
        );

        AggregationResults<AnalyticsDTO> results = mongoTemplate.aggregate(aggregation, "assigned_resource_analytics", AnalyticsDTO.class);

        return results.getUniqueMappedResult();
    }
}