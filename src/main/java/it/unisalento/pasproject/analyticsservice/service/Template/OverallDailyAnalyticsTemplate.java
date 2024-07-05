package it.unisalento.pasproject.analyticsservice.service.Template;

import it.unisalento.pasproject.analyticsservice.dto.DailyAnalyticsDTO;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;


public class OverallDailyAnalyticsTemplate extends AnalyticsTemplate<DailyAnalyticsDTO> {

    public OverallDailyAnalyticsTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    protected MatchOperation createMatchOperation(String id, LocalDateTime startDate, LocalDateTime endDate) {
        return Aggregation.match(Criteria.where("assignedTime").gte(startDate).lte(endDate));
    }

    @Override
    protected List<AggregationOperation> getAdditionalOperations() {
        return List.of(
                Aggregation.lookup("assigned_resource_analytics", "taskId", "taskId", "resources"),
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
                .and(DateOperators.dateOf("assignedTime").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%d")).as("day")
                .and(DateOperators.dateOf("assignedTime").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%m")).as("month")
                .and(DateOperators.dateOf("assignedTime").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%Y")).as("year");
    }

    @Override
    protected GroupOperation createGroupOperation() {
        return Aggregation.group("year", "month", "day")
                .sum("assignedEnergyConsumptionPerHour").as("energyConsumed")
                .sum("computingPower").as("computingPowerUsed")
                .addToSet("memberEmail").as("uniqueMembers")
                .addToSet("emailUtente").as("uniqueUsers")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf("hasTaskCompleted").equalToValue(true))
                        .then(1).otherwise(0)).as("tasksCompleted")
                .count().as("tasksSubmitted")
                .sum("workDuration").as("totalWorkDuration");
    }

    @Override
    protected ProjectionOperation createFinalProjection() {
        return Aggregation.project("energyConsumed", "computingPowerUsed", "tasksSubmitted", "tasksCompleted")
                .andExpression("totalWorkDuration / 60000").as("workMinutes") // Convert milliseconds to minutes
                .and(ArrayOperators.Size.lengthOfArray("uniqueMembers")).as("activeMemberCount")
                .and(ArrayOperators.Size.lengthOfArray("uniqueUsers")).as("activeUserCount")
                .andExpression("toInt(year)").as("year")
                .andExpression("toInt(month)").as("month")
                .andExpression("toInt(day)").as("day");
    }

    @Override
    protected SortOperation createSortOperation() {
        return Aggregation.sort(Sort.by(Sort.Direction.ASC, "year", "month", "day"));
    }

    @Override
    protected String getCollectionName() {
        return "assignment_analytics";
    }

    @Override
    protected Class<DailyAnalyticsDTO> getDTOClass() {
        return DailyAnalyticsDTO.class;
    }
}