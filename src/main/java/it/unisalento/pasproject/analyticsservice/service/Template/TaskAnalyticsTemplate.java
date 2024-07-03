package it.unisalento.pasproject.analyticsservice.service.Template;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.dto.UserAnalyticsDTO;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants.*;

public class TaskAnalyticsTemplate extends AnalyticsTemplate<UserAnalyticsDTO>{

    public TaskAnalyticsTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    public Optional<UserAnalyticsDTO> getAnalytics(String id, LocalDateTime startDate, LocalDateTime endDate) {
        return super.getAnalytics(id, startDate, endDate);
    }

    @Override
    protected MatchOperation createMatchOperation(String id, LocalDateTime startDate, LocalDateTime endDate) {
        return Aggregation.match(Criteria.where(ASSIGNMENT_TASK_ID_FIELD).is(id));
    }

    @Override
    protected List<AggregationOperation> getAdditionalOperations() {
        return List.of(
                Aggregation.lookup(this.mongoTemplate.getCollectionName(AssignedResource.class), RES_TASK_ID_FIELD, ASSIGNMENT_TASK_ID_FIELD, "assignedResources"),
                Aggregation.unwind("assignedResources", true)
        );
    }

    @Override
    protected ProjectionOperation createProjectionOperation() {
        return Aggregation.project()
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
    }

    @Override
    protected GroupOperation createGroupOperation() {
        return Aggregation.group(ASSIGNMENT_TASK_ID_FIELD)
                .first(ASSIGNMENT_TASK_ID_FIELD).as(ASSIGNMENT_TASK_ID_FIELD)
                .sum("timeSpent").as("totalTimeSpent")
                .sum("assignedEnergyConsumptionPerHour").as("energySaved")
                .sum("totalComputingPower").as("computingPowerUsed");
    }

    @Override
    protected ProjectionOperation createFinalProjection() {
        return Aggregation.project(ASSIGNMENT_TASK_ID_FIELD, "totalTimeSpent", "energySaved", "computingPowerUsed")
                .andExpression("totalTimeSpent / 60000").as("timeSpentOnTasks");// Convert milliseconds to minutes
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
