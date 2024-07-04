package it.unisalento.pasproject.analyticsservice.service.Template;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.dto.MemberMonthlyAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.service.CalculateAnalyticsService;
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

public class MemberMonthlyTemplate extends AnalyticsTemplate<MemberMonthlyAnalyticsDTO> {
    //LOgger factory
    private static final Logger logger = LoggerFactory.getLogger(CalculateAnalyticsService.class);

    public MemberMonthlyTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    public List<MemberMonthlyAnalyticsDTO> getAnalyticsList(String id, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Executing getAnalytics with id: {}, startDate: {}, endDate: {}", id, startDate, endDate);
        List<MemberMonthlyAnalyticsDTO> result = super.getAnalyticsList(id, startDate, endDate);
        logger.info("Result of getAnalytics: {}", result);
        return result;
    }

    @Override
    protected MatchOperation createMatchOperation(String id, LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation matchOperation;

        if (startDate != null && endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(id)
                    .andOperator(Criteria.where("assignedTime").gte(startDate).lte(endDate)));
        } else if (startDate != null) {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(id)
                    .and("assignedTime").gte(startDate));
        } else if (endDate != null) {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(id)
                    .and("assignedTime").lte(endDate));
        } else {
            matchOperation = Aggregation.match(Criteria.where("memberEmail").is(id));
        }

        logger.info("MatchOperation: {}", matchOperation);
        return matchOperation;
    }

    @Override
    protected List<AggregationOperation> getAdditionalOperations() {
        return List.of();
    }

    @Override
    protected ProjectionOperation createProjectionOperation() {
        ProjectionOperation projectionOperation = Aggregation.project()
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
                .and(DateOperators.dateOf(COMPLETED_TIME_FIELD).withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%m")).as("month")
                .and(DateOperators.dateOf(COMPLETED_TIME_FIELD).withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%Y")).as("year");

        logger.info("ProjectionOperation: {}", projectionOperation);
        return projectionOperation;
    }

    @Override
    protected GroupOperation createGroupOperation() {
        GroupOperation groupOperation = Aggregation.group("month", "year")
                .sum(ArithmeticOperators.Divide.valueOf(
                        ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)
                ).divideBy(60000)).as("totalWorkMinutes")
                .sum(ArithmeticOperators.Multiply.valueOf(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD)
                        .multiplyBy(ArithmeticOperators.Divide.valueOf(
                                ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)
                        ).divideBy(3600000))).as("totalEnergySold")
                .sum(TOTAL_COMPUTING_POWER_FIELD).as("totalComputingPowerSold")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(true)).then(1).otherwise(0)).as("tasksCompleted")
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(false)).then(1).otherwise(0)).as("tasksInProgress")
                .count().as("tasksAssigned");

        logger.info("GroupOperation: {}", groupOperation);
        return groupOperation;
    }

    @Override
    protected ProjectionOperation createFinalProjection() {
        ProjectionOperation finalProjection = Aggregation.project(
                "memberEmail",
                "month",
                "year",
                "totalWorkMinutes",
                "totalEnergySold",
                "totalComputingPowerSold",
                "tasksCompleted",
                "tasksInProgress",
                "tasksAssigned"
        );

        logger.info("FinalProjectionOperation: {}", finalProjection);
        return finalProjection;
    }

    @Override
    protected SortOperation createSortOperation() {
        return Aggregation.sort(Sort.by(Sort.Order.asc("year"), Sort.Order.asc("month")));
    }

    @Override
    protected String getCollectionName() {
        return this.mongoTemplate.getCollectionName(AssignedResource.class);
    }

    @Override
    protected Class<MemberMonthlyAnalyticsDTO> getDTOClass() {
        return MemberMonthlyAnalyticsDTO.class;
    }
}
