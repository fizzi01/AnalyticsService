package it.unisalento.pasproject.analyticsservice.service.Template;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.dto.MemberListAnalyticsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;

import static it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants.*;
import static it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants.ASSIGNED_TIME_FIELD;

public class MemberListTemplate extends AnalyticsTemplate<MemberListAnalyticsDTO> {
    //LOgger factory
    private static final Logger logger = LoggerFactory.getLogger(MemberListTemplate.class);

    public MemberListTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    public List<MemberListAnalyticsDTO> getAnalyticsList(String email, LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        return super.getAnalyticsList(email, startDate, endDate, granularity);
    }

    @Override
    protected MatchOperation createMatchOperation(String email, LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation matchOperation;

        if (startDate != null && endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(email)
                    .andOperator(Criteria.where("completedTime").gte(startDate).lte(endDate)));
        } else if (startDate != null) {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(email)
                    .and("completedTime").gte(startDate));
        } else if (endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(email)
                    .and("completedTime").lte(endDate));
        } else {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(email));
        }

        return matchOperation;
    }

    @Override
    protected List<AggregationOperation> getAdditionalOperations() {
        return List.of();
    }

    @Override
    protected ProjectionOperation createProjectionOperation() {
        return Aggregation.project()
                .andInclude(
                        EMAIL_MEMBER_FIELD,
                        ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD,
                        ASSIGNED_TIME_FIELD,
                        COMPLETED_TIME_FIELD,
                        HAS_COMPLETED_FIELD
                )
                .and(ArithmeticOperators.Add.valueOf(ASSIGNED_SINGLE_SCORE_FIELD)
                        .add(ASSIGNED_MULTI_SCORE_FIELD)
                        .add(ASSIGNED_OPENCL_SCORE_FIELD)
                        .add(ASSIGNED_VULKAN_SCORE_FIELD)
                        .add(ASSIGNED_CUDA_SCORE_FIELD))
                .as("totalComputingPowerSold")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf(COMPLETED_TIME_FIELD).withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%d"))).as("day")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf(COMPLETED_TIME_FIELD).withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%m"))).as("month")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf(COMPLETED_TIME_FIELD).withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%Y"))).as("year");
    }

    @Override
    protected GroupOperation createGroupOperation(String granularity) {
        return switch (granularity) {
            case "day" -> Aggregation.group("memberEmail", "year", "month", "day")
                    .sum(ArithmeticOperators.Divide.valueOf(
                            ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)
                    ).divideBy(60000)).as("totalWorkMinutes")
                    .sum(ArithmeticOperators.Multiply.valueOf(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD)
                            .multiplyBy(ArithmeticOperators.Divide.valueOf(
                                    ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)
                            ).divideBy(3600000))).as("totalEnergySold")
                    .sum("totalComputingPowerSold").as("totalComputingPowerSold")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(false)).then(1).otherwise(0)).as("tasksInProgress")
                    .count().as("tasksAssigned");
            case "month" -> Aggregation.group("memberEmail", "year", "month")
                    .sum(ArithmeticOperators.Divide.valueOf(
                            ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)
                    ).divideBy(60000)).as("totalWorkMinutes")
                    .sum(ArithmeticOperators.Multiply.valueOf(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD)
                            .multiplyBy(ArithmeticOperators.Divide.valueOf(
                                    ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)
                            ).divideBy(3600000))).as("totalEnergySold")
                    .sum("totalComputingPowerSold").as("totalComputingPowerSold")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(false)).then(1).otherwise(0)).as("tasksInProgress")
                    .count().as("tasksAssigned");
            case "year" -> Aggregation.group("memberEmail", "year")
                    .sum(ArithmeticOperators.Divide.valueOf(
                            ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)
                    ).divideBy(60000)).as("totalWorkMinutes")
                    .sum(ArithmeticOperators.Multiply.valueOf(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD)
                            .multiplyBy(ArithmeticOperators.Divide.valueOf(
                                    ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)
                            ).divideBy(3600000))).as("totalEnergySold")
                    .sum("totalComputingPowerSold").as("totalComputingPowerSold")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                    .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(false)).then(1).otherwise(0)).as("tasksInProgress")
                    .count().as("tasksAssigned");
            default -> null;
        };
    }

    @Override
    protected ProjectionOperation createFinalProjection(String granularity) {
        ProjectionOperation finalProjection = Aggregation.project()
                .andExpression("memberEmail").as("memberEmail")
                .andExpression("totalWorkMinutes").as("totalWorkMinutes")
                .andExpression("totalEnergySold").as("totalEnergySold")
                .andExpression("totalComputingPowerSold").as("totalComputingPowerSold")
                .andExpression("tasksCompleted").as("tasksCompleted")
                .andExpression("tasksInProgress").as("tasksInProgress")
                .andExpression("tasksAssigned").as("tasksAssigned");

        finalProjection = switch (granularity) {
            case "day" -> finalProjection
                    .andExpression("day").as("day")
                    .andExpression("month").as("month")
                    .andExpression("year").as("year");
            case "month" -> finalProjection
                    .andExpression("month").as("month")
                    .andExpression("year").as("year");
            case "year" -> finalProjection
                    .andExpression("year").as("year");
            default -> finalProjection;
        };

        return finalProjection;
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
        return this.mongoTemplate.getCollectionName(AssignedResource.class);
    }

    @Override
    protected Class<MemberListAnalyticsDTO> getDTOClass() {
        return MemberListAnalyticsDTO.class;
    }
}
