package it.unisalento.pasproject.analyticsservice.service;

import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.*;
import it.unisalento.pasproject.analyticsservice.repositories.AssignmentAnalyticsRepository;
import it.unisalento.pasproject.analyticsservice.service.Template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CalculateAnalyticsService {

    private final AssignmentAnalyticsRepository assignmentAnalyticsRepository;

    private final OverallAnalyticsTemplate overallAnalyticsTemplate;
    private final TaskAnalyticsTemplate taskAnalyticsTemplate;
    private final UserTemplate userTemplate;
    private final UserListTemplate userListTemplate;
    private final MemberTemplate memberTemplate;
    private final MemberListTemplate memberListTemplate;
    private final OverallListTemplate overallListTemplate;

    //LOgger factory
    private static final Logger LOGGER = LoggerFactory.getLogger(CalculateAnalyticsService.class);

    @Autowired
    public CalculateAnalyticsService(MongoTemplate mongoTemplate,
                                     AssignmentAnalyticsRepository assignmentAnalyticsRepository)
    {
        this.assignmentAnalyticsRepository = assignmentAnalyticsRepository;

        this.overallAnalyticsTemplate = new OverallAnalyticsTemplate(mongoTemplate);
        this.taskAnalyticsTemplate = new TaskAnalyticsTemplate(mongoTemplate);
        this.userTemplate = new UserTemplate(mongoTemplate);
        this.memberTemplate = new MemberTemplate(mongoTemplate);
        this.memberListTemplate = new MemberListTemplate(mongoTemplate);
        this.overallListTemplate = new OverallListTemplate(mongoTemplate);
        this.userListTemplate = new UserListTemplate(mongoTemplate);
    }

    // #### Member Analytics ####
    public Optional<MemberAnalyticsDTO> getMemberAnalytics(String memberEmail, LocalDateTime startDate, LocalDateTime endDate) {
        return memberTemplate.getAnalytics(memberEmail, startDate, endDate);
    }

    public List<MemberListAnalyticsDTO> getMemberMonthlyAnalytics(String memberEmail, LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        return memberListTemplate.getAnalyticsList(memberEmail, startDate, endDate, granularity);
    }

    // #### User Analytics ####
    public Optional<UserAnalyticsDTO> getTaskUserAnalytics(String taskId){
        return taskAnalyticsTemplate.getAnalytics(taskId, null, null);
    }

    public Optional<UserAnalyticsDTO> getUserAnalytics(String emailUtente, LocalDateTime startDate, LocalDateTime endDate) {
        return userTemplate.getAnalytics(emailUtente, startDate, endDate);
    }

    public List<UserListAnalyticsDTO> getUserListAnalytics(String emailUtente, LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        return userListTemplate.getAnalyticsList(emailUtente, startDate, endDate, granularity);
    }

    // #### Overall Analytics ####
    public Optional<AnalyticsDTO> getOverallAnalytics(LocalDateTime startDate, LocalDateTime endDate) {

        AnalyticsDTO analyticsDTO = overallAnalyticsTemplate.getAnalytics(null, startDate, endDate).orElse(null);

        analyticsDTO = getAssignedTasksInfo(analyticsDTO, startDate, endDate);

        return Optional.ofNullable(analyticsDTO);
    }

    public List<AdminListAnalyticsDTO> getOverallDailyAnalytics(LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        return overallListTemplate.getAnalyticsList(null, startDate, endDate, granularity);
    }

    private AnalyticsDTO getAssignedTasksInfo(AnalyticsDTO analyticsDTO, LocalDateTime startDate, LocalDateTime endDate) {
        if(analyticsDTO == null) {
            return null;
        }

        List<AssignmentAnalytics> allAssignments = assignmentAnalyticsRepository.findAll();

        // if startDate and endDate are provided, filter the assignments
        if(startDate != null && endDate != null) {
            allAssignments = allAssignments.stream()
                    .filter(assignment -> assignment.getAssignedTime().isAfter(startDate) && assignment.getAssignedTime().isBefore(endDate))
                    .toList();
        }

        //Find number of unique members email
        analyticsDTO.setActiveUserCount((int) allAssignments.stream().map(AssignmentAnalytics::getEmailUtente).distinct().count());

        //Find number of tasks submitted
        analyticsDTO.setTasksSubmitted(allAssignments.size());

        analyticsDTO.setTasksCompleted((int) allAssignments.stream().filter(AssignmentAnalytics::isComplete).count());

        if(startDate != null && endDate != null) {
            analyticsDTO.setStartDate(startDate);
            analyticsDTO.setEndDate(endDate);
        } else {
            try{
                analyticsDTO.setStartDate(allAssignments.stream().map(AssignmentAnalytics::getAssignedTime).min(LocalDateTime::compareTo).orElse(null));
                analyticsDTO.setEndDate(allAssignments.stream().map(AssignmentAnalytics::getAssignedTime).max(LocalDateTime::compareTo).orElse(null));
            }catch (Exception e){
                LOGGER.error("Error while calculating start and end date for analytics");
            }
        }

        return analyticsDTO;

    }

}