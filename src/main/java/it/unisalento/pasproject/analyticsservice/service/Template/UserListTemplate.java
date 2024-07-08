package it.unisalento.pasproject.analyticsservice.service.Template;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.UserListAnalyticsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;

import static it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants.COMPLETED_TIME_FIELD;

public class UserListTemplate extends AnalyticsTemplate<UserListAnalyticsDTO> {
    private static final Logger logger = LoggerFactory.getLogger(UserListTemplate.class);

    public UserListTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    public List<UserListAnalyticsDTO> getAnalyticsList(String email, LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        return super.getAnalyticsList(email, startDate, endDate, granularity);
    }

    @Override
    protected MatchOperation createMatchOperation(String email, LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation matchOperation;

        if (startDate != null && endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(email)
                    .andOperator(Criteria.where("completedTime").gte(startDate).lte(endDate)));
        } else if (startDate != null) {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(email)
                    .and("completedTime").gte(startDate));
        } else if (endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(email)
                    .and("completedTime").lte(endDate));
        } else {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(email));
        }

        return matchOperation;
    }

    @Override
    protected List<AggregationOperation> getAdditionalOperations() {
        return List.of(Aggregation.lookup(mongoTemplate.getCollectionName(AssignedResource.class), "taskId", "taskId", "assignedResources"));
    }

    @Override
    protected ProjectionOperation createProjectionOperation() {
        return Aggregation.project()
                .andInclude("emailUtente")
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf("completedTime").subtract("assignedTime")).otherwise(0)).as("timeSpent")
                .andInclude("assignedTime", "completedTime", "isComplete")
                .and("assignedResources.assignedEnergyConsumptionPerHour").as("assignedEnergyConsumptionPerHour")
                .and(ArithmeticOperators.Add.valueOf("assignedResources.assignedSingleScore")
                        .add("assignedResources.assignedMultiScore")
                        .add("assignedResources.assignedOpenclScore")
                        .add("assignedResources.assignedVulkanScore")
                        .add("assignedResources.assignedCudaScore")).as("totalComputingPower")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf(COMPLETED_TIME_FIELD).withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%d"))).as("day")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf(COMPLETED_TIME_FIELD).withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%m"))).as("month")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf(COMPLETED_TIME_FIELD).withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%Y"))).as("year");
    }

    @Override
    protected GroupOperation createGroupOperation(String granularity) {
        return switch (granularity) {
            case "day" -> Aggregation.group("emailUtente", "year", "month", "day")
                    .sum("timeSpent").as("totalTimeSpent")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(false)).then(1).otherwise(0)).as("tasksOngoing")
                    .count().as("tasksSubmitted")
                    .min("assignedTime").as("startDate")
                    .max("assignedTime").as("endDate")
                    .sum("assignedEnergyConsumptionPerHour").as("energySaved")
                    .sum("totalComputingPower").as("computingPowerUsed");
            case "month" -> Aggregation.group("emailUtente", "year", "month")
                    .sum("timeSpent").as("totalTimeSpent")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(false)).then(1).otherwise(0)).as("tasksOngoing")
                    .count().as("tasksSubmitted")
                    .min("assignedTime").as("startDate")
                    .max("assignedTime").as("endDate")
                    .sum("assignedEnergyConsumptionPerHour").as("energySaved")
                    .sum("totalComputingPower").as("computingPowerUsed");
            case "year" -> Aggregation.group("emailUtente", "year")
                    .sum("timeSpent").as("totalTimeSpent")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(false)).then(1).otherwise(0)).as("tasksOngoing")
                    .count().as("tasksSubmitted")
                    .min("assignedTime").as("startDate")
                    .max("assignedTime").as("endDate")
                    .sum("assignedEnergyConsumptionPerHour").as("energySaved")
                    .sum("totalComputingPower").as("computingPowerUsed");
            default -> null;
        };
    }

    @Override
    protected ProjectionOperation createFinalProjection(String granularity) {
        ProjectionOperation projectionOperation = Aggregation.project()
                .andExpression("emailUtente").as("userEmail")
                .andExpression("totalTimeSpent").as("totalTimeSpent")
                .andExpression("tasksCompleted").as("tasksCompleted")
                .andExpression("tasksOngoing").as("tasksOngoing")
                .andExpression("tasksSubmitted").as("tasksSubmitted")
                .andExpression("energySaved").as("energySaved")
                .andExpression("computingPowerUsed").as("computingPowerUsed")
                .andExpression("totalTimeSpent / 60000").as("timeSpentOnTasks");

        projectionOperation = switch (granularity) {
            case "day" -> projectionOperation
                    .andExpression("day").as("day")
                    .andExpression("month").as("month")
                    .andExpression("year").as("year");
            case "month" -> projectionOperation
                    .andExpression("month").as("month")
                    .andExpression("year").as("year");
            case "year" -> projectionOperation
                    .andExpression("year").as("year");
            default -> projectionOperation;
        };

        return projectionOperation;
    }

    @Override
    protected SortOperation createSortOperation(String granularity) {
        return switch (granularity) {
            case "day" -> Aggregation.sort(Sort.by(Sort.Order.asc("year"), Sort.Order.asc("month"), Sort.Order.asc("day")));
            case "month" -> Aggregation.sort(Sort.by(Sort.Order.asc("year"), Sort.Order.asc("month")));
            case "year" -> Aggregation.sort(Sort.by(Sort.Order.asc("year")));
            default -> null;
        };
    }

    @Override
    protected String getCollectionName() {
        return mongoTemplate.getCollectionName(AssignmentAnalytics.class);
    }

    @Override
    protected Class<UserListAnalyticsDTO> getDTOClass() {
        return UserListAnalyticsDTO.class;
    }
}
