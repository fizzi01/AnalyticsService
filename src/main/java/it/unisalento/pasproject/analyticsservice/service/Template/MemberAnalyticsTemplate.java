package it.unisalento.pasproject.analyticsservice.service.Template;

import it.unisalento.pasproject.analyticsservice.domain.AssignedResource;
import it.unisalento.pasproject.analyticsservice.dto.MemberAnalyticsDTO;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants.*;
import static it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants.ASSIGNED_TIME_FIELD;

public class MemberAnalyticsTemplate extends AnalyticsTemplate<MemberAnalyticsDTO>{
    public MemberAnalyticsTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    public Optional<MemberAnalyticsDTO> getAnalytics(String id, LocalDateTime startDate, LocalDateTime endDate) {
        return super.getAnalytics(id, startDate, endDate);
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

        return matchOperation;

    }

    @Override
    protected List<AggregationOperation> getAdditionalOperations() {
        return List.of();
    }

    @Override
    protected ProjectionOperation createProjectionOperation() {
        return Aggregation.project()
                .andInclude(EMAIL_MEMBER_FIELD)
                .and(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(true))
                        .thenValueOf(ArithmeticOperators.Subtract.valueOf(COMPLETED_TIME_FIELD).subtract(ASSIGNED_TIME_FIELD)).otherwise(0)).as(WORK_DURATION_FIELD)
                .andInclude(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD,HAS_COMPLETED_FIELD)
                .and(ArithmeticOperators.Add.valueOf(ASSIGNED_SINGLE_SCORE_FIELD)
                        .add(ASSIGNED_MULTI_SCORE_FIELD)
                        .add(ASSIGNED_OPENCL_SCORE_FIELD)
                        .add(ASSIGNED_VULKAN_SCORE_FIELD)
                        .add(ASSIGNED_CUDA_SCORE_FIELD)).as(TOTAL_COMPUTING_POWER_FIELD)
                .andInclude(ASSIGNED_TIME_FIELD);
    }

    @Override
    protected GroupOperation createGroupOperation() {
        return Aggregation.group(EMAIL_MEMBER_FIELD)
                .first(EMAIL_MEMBER_FIELD).as(EMAIL_MEMBER_FIELD)
                .sum(WORK_DURATION_FIELD).as(TOTAL_WORK_DURATION_FIELD)
                .sum(ASSIGNED_ENERGY_CONSUMPTION_PER_HOUR_FIELD).as(ENERGY_CONSUMED_FIELD)
                .sum(TOTAL_COMPUTING_POWER_FIELD).as(COMPUTING_POWER_FIELD)
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(true)).then(1).otherwise(0)).as(TASKS_COMPLETED_FIELD)
                .sum(ConditionalOperators.when(ComparisonOperators.Eq.valueOf(HAS_COMPLETED_FIELD).equalToValue(false)).then(1).otherwise(0)).as(TASKS_IN_PROGRESS_FIELD)
                .count().as(TASKS_ASSIGNED_FIELD)
                .min(ASSIGNED_TIME_FIELD).as(START_DATE_FIELD)
                .max(ASSIGNED_TIME_FIELD).as(END_DATE_FIELD);
    }

    @Override
    protected ProjectionOperation createFinalProjection() {
        return Aggregation.project(EMAIL_MEMBER_FIELD, TOTAL_WORK_DURATION_FIELD, ENERGY_CONSUMED_FIELD, COMPUTING_POWER_FIELD, TASKS_COMPLETED_FIELD, TASKS_IN_PROGRESS_FIELD, TASKS_ASSIGNED_FIELD, START_DATE_FIELD, END_DATE_FIELD)
                .andExpression(TOTAL_WORK_DURATION_FIELD + " / 60000").as(WORK_TIME_FIELD); // Convert milliseconds to minutes
    }

    @Override
    protected SortOperation createSortOperation() {
        return null;
    }

    @Override
    protected String getCollectionName() {
        return this.mongoTemplate.getCollectionName(AssignedResource.class);
    }

    @Override
    protected Class<MemberAnalyticsDTO> getDTOClass() {
        return MemberAnalyticsDTO.class;
    }
}
