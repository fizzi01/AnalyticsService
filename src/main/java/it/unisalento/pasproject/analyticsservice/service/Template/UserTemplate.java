package it.unisalento.pasproject.analyticsservice.service.Template;

import it.unisalento.pasproject.analyticsservice.dto.UserAnalyticsDTO;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserTemplate extends AnalyticsTemplate<UserAnalyticsDTO>{

    public UserTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    public Optional<UserAnalyticsDTO> getAnalytics(String email, LocalDateTime startDate, LocalDateTime endDate) {
        return super.getAnalytics(email, startDate, endDate);
    }

    @Override
    protected MatchOperation createMatchOperation(String email, LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation matchOperation;

        if (startDate != null && endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(email)
                    .andOperator(Criteria.where("assignedTime").gte(startDate).lte(endDate)));
        } else if (startDate != null) {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(email)
                    .and("assignedTime").gte(startDate));
        } else if (endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(email)
                    .and("assignedTime").lte(endDate));
        } else {
            matchOperation = Aggregation.match(Criteria.where("emailUtente").is(email));
        }

        return matchOperation;
    }

    @Override
    protected List<AggregationOperation> getAdditionalOperations() {
        return List.of(Aggregation.lookup("assigned_resource_analytics", "taskId", "taskId", "assignedResources"));
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
                        .add("assignedResources.assignedCudaScore")).as("totalComputingPower");
    }

    @Override
    protected GroupOperation createGroupOperation(String granularity) {
        return Aggregation.group("emailUtente")
                .first("emailUtente").as("userEmail")
                .sum("timeSpent").as("totalTimeSpent")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("isComplete").equalToValue(false)).then(1).otherwise(0)).as("tasksOngoing")
                .count().as("tasksSubmitted")
                .min("assignedTime").as("startDate")
                .max("assignedTime").as("endDate")
                .sum("assignedEnergyConsumptionPerHour").as("energySaved")
                .sum("totalComputingPower").as("computingPowerUsed");
    }

    @Override
    protected ProjectionOperation createFinalProjection(String granularity) {
        return Aggregation.project("userEmail", "totalTimeSpent", "tasksCompleted", "tasksOngoing", "tasksSubmitted", "startDate", "endDate", "energySaved", "computingPowerUsed")
                .andExpression("totalTimeSpent / 60000").as("timeSpentOnTasks"); // Convert milliseconds to minutes
    }

    @Override
    protected SortOperation createSortOperation(String granularity) {
        return null;
    }

    @Override
    protected String getCollectionName() {
        return "assignment_analytics";
    }

    @Override
    protected Class<UserAnalyticsDTO> getDTOClass() {
        return UserAnalyticsDTO.class;
    }
}
