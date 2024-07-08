package it.unisalento.pasproject.analyticsservice.service.Template;

import it.unisalento.pasproject.analyticsservice.dto.AnalyticsDTO;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OverallAnalyticsTemplate extends AnalyticsTemplate<AnalyticsDTO>{

    public OverallAnalyticsTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    public Optional<AnalyticsDTO> getAnalytics(String email, LocalDateTime startDate, LocalDateTime endDate) {
        return super.getAnalytics(email, startDate, endDate);
    }

    @Override
    protected MatchOperation createMatchOperation(String email, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return Aggregation.match(Criteria.where("assignedTime").gte(startDate).lte(endDate));
        } else {
            return Aggregation.match(new Criteria()); // No date filter
        }
    }


    @Override
    protected List<AggregationOperation> getAdditionalOperations() {
        return List.of(Aggregation.lookup("assignment_analytics", "taskId", "taskId", "assignments"));
    }

    @Override
    protected ProjectionOperation createProjectionOperation() {
        return Aggregation.project()
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
    }

    @Override
    protected GroupOperation createGroupOperation(String granularity) {
        return Aggregation.group()
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
    }

    @Override
    protected ProjectionOperation createFinalProjection(String granularity) {
        return Aggregation.project("energyConsumed", "computingPowerUsed", "tasksSubmitted", "tasksCompleted", "startDate", "endDate")
                .andExpression("totalWorkDuration / 60000").as("workMinutes") // Convert milliseconds to hours
                .and(ArrayOperators.Size.lengthOfArray("uniqueMembers")).as("activeMemberCount")
                .and(ArrayOperators.Size.lengthOfArray("uniqueUsers")).as("activeUserCount");
    }

    @Override
    protected SortOperation createSortOperation(String granularity) {
        return null;
    }

    @Override
    protected String getCollectionName() {
        return "assigned_resource_analytics";
    }

    @Override
    protected Class<AnalyticsDTO> getDTOClass() {
        return AnalyticsDTO.class;
    }
}
