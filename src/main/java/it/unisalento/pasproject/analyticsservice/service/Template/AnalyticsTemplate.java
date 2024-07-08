package it.unisalento.pasproject.analyticsservice.service.Template;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AnalyticsTemplate<T> {

    protected final MongoTemplate mongoTemplate;

    protected AnalyticsTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<T> getAnalyticsList(String email, LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        MatchOperation matchOperation = createMatchOperation(email, startDate, endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(matchOperation);
        operations.addAll(getAdditionalOperations());
        operations.add(createProjectionOperation());
        operations.add(createGroupOperation(granularity));
        operations.add(createFinalProjection(granularity));
        operations.add(createSortOperation(granularity));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<T> results = mongoTemplate.aggregate(aggregation, getCollectionName(), getDTOClass());

        return results.getMappedResults();
    }

    public Optional<T> getAnalytics(String email, LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation matchOperation = createMatchOperation(email, startDate, endDate);
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(matchOperation);
        operations.addAll(getAdditionalOperations());
        operations.add(createProjectionOperation());
        operations.add(createGroupOperation(""));
        operations.add(createFinalProjection(""));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<T> results = mongoTemplate.aggregate(aggregation, getCollectionName(), getDTOClass());

        return results.getMappedResults().stream().findFirst();
    }

    protected abstract MatchOperation createMatchOperation(String email, LocalDateTime startDate, LocalDateTime endDate);

    protected abstract List<AggregationOperation> getAdditionalOperations();

    protected abstract ProjectionOperation createProjectionOperation();

    protected abstract GroupOperation createGroupOperation(String granularity);

    protected abstract ProjectionOperation createFinalProjection(String granularity);

    protected abstract SortOperation createSortOperation(String granularity);

    protected abstract String getCollectionName();

    protected abstract Class<T> getDTOClass();
}
