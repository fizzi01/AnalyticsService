package it.unisalento.pasproject.analyticsservice.service.Template;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.AdminListAnalyticsDTO;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;


public class OverallListTemplate extends AnalyticsTemplate<AdminListAnalyticsDTO> {

    public OverallListTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    protected MatchOperation createMatchOperation(String email, LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation matchOperation;

        if (startDate != null && endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("completedTime").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            matchOperation = Aggregation.match(Criteria.where("completedTime").gte(startDate));
        } else if (endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("completedTime").lte(endDate));
        } else {
            matchOperation = new MatchOperation(new Criteria());
        }

        return matchOperation;
    }

    @Override
    protected List<AggregationOperation> getAdditionalOperations() {
        return List.of(
                Aggregation.lookup(mongoTemplate.getCollectionName(AssignedResource.class), "taskId", "taskId", "resources"),
                Aggregation.unwind("resources", true));
    }

    @Override
    protected ProjectionOperation createProjectionOperation() {
        return Aggregation.project()
                .andInclude("taskId","emailUtente","isComplete", "assignedTime", "completedTime")
                .andExpression("resources.assignedTime").as("assignedTime")
                .andExpression("resources.completedTime").as("completedTime")
                .andExpression("resources.assignedEnergyConsumptionPerHour").as("assignedEnergyConsumptionPerHour")
                .andExpression("emailUtente").as("emailUtente")
                .andExpression("resources.memberEmail").as("memberEmail")
                .andExpression("isComplete").as("hasTaskCompleted")
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("resources.hasCompleted").equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf("resources.completedTime").subtract("resources.assignedTime")).otherwise(0)).as("workDuration")
                .and(ArithmeticOperators.Add.valueOf("resources.assignedSingleScore")
                        .add("resources.assignedMultiScore")
                        .add("resources.assignedOpenclScore")
                        .add("resources.assignedVulkanScore")
                        .add("resources.assignedCudaScore")).as("computingPower")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf("assignedTime").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%d"))).as("day")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf("assignedTime").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%m"))).as("month")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf("assignedTime").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%Y"))).as("year");
    }

    @Override
    protected GroupOperation createGroupOperation(String granularity) {
        return switch (granularity) {
            case "day" -> Aggregation.group("year", "month", "day")
                    .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                    .sum("computingPower").as("computingPowerUsed")
                    .addToSet("memberEmail").as("uniqueMembers")
                    .addToSet("emailUtente").as("uniqueUsers")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasTaskCompleted").equalToValue(true))
                            .then(1).otherwise(0)).as("tasksCompleted")
                    .addToSet("taskId").as("uniqueTaskIds")
                    .sum("workDuration").as("totalWorkDuration");
            case "month" -> Aggregation.group("year", "month")
                    .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                    .sum("computingPower").as("computingPowerUsed")
                    .addToSet("memberEmail").as("uniqueMembers")
                    .addToSet("emailUtente").as("uniqueUsers")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasTaskCompleted").equalToValue(true))
                            .then(1).otherwise(0)).as("tasksCompleted")
                    .addToSet("taskId").as("uniqueTaskIds")
                    .sum("workDuration").as("totalWorkDuration");
            case "year" -> Aggregation.group("year")
                    .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                    .sum("computingPower").as("computingPowerUsed")
                    .addToSet("memberEmail").as("uniqueMembers")
                    .addToSet("emailUtente").as("uniqueUsers")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasTaskCompleted").equalToValue(true))
                            .then(1).otherwise(0)).as("tasksCompleted")
                    .addToSet("taskId").as("uniqueTaskIds")
                    .sum("workDuration").as("totalWorkDuration");
            default -> null;
        };
    }

    @Override
    protected ProjectionOperation createFinalProjection(String granularity) {
        ProjectionOperation projectionOperation =
                Aggregation.project("energyConsumed", "computingPowerUsed", "tasksCompleted")
                .and(ArrayOperators.Size.lengthOfArray("uniqueTaskIds")).as("tasksSubmitted")
                .andExpression("totalWorkDuration / 60000").as("workMinutes") // Convert milliseconds to minutes
                .and(ArrayOperators.Size.lengthOfArray("uniqueMembers")).as("activeMemberCount")
                .and(ArrayOperators.Size.lengthOfArray("uniqueUsers")).as("activeUserCount");

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
    protected Class<AdminListAnalyticsDTO> getDTOClass() {
        return AdminListAnalyticsDTO.class;
    }
}