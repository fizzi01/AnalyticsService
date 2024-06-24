package it.unisalento.pasproject.analyticsservice.controller;

import it.unisalento.pasproject.analyticsservice.domain.AssignmentAnalytics;
import it.unisalento.pasproject.analyticsservice.dto.*;
import it.unisalento.pasproject.analyticsservice.exceptions.BadFormatRequestException;
import it.unisalento.pasproject.analyticsservice.exceptions.MissingDataException;
import it.unisalento.pasproject.analyticsservice.repositories.AssignmentAnalyticsRepository;
import it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants;
import it.unisalento.pasproject.analyticsservice.service.CalculateAnalyticsService;
import it.unisalento.pasproject.analyticsservice.service.UserCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.unisalento.pasproject.analyticsservice.security.SecurityConstants.*;
import static it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final CalculateAnalyticsService calculateAnalyticsService;
    private final UserCheckService userCheckService;
    private final AssignmentAnalyticsRepository assignmentAnalyticsRepository;

    @Autowired
    public AnalyticsController(CalculateAnalyticsService calculateAnalyticsService,
                               UserCheckService userCheckService,
                               AssignmentAnalyticsRepository assignmentAnalyticsRepository) {
        this.calculateAnalyticsService = calculateAnalyticsService;
        this.userCheckService = userCheckService;
        this.assignmentAnalyticsRepository = assignmentAnalyticsRepository;
    }

    @GetMapping("/debug/get")
    @Secured({ROLE_ADMIN})
    public List<AssignmentAnalytics> getAnalytics() {
       return assignmentAnalyticsRepository.findAll();
    }

    @GetMapping("/user/get")
    @Secured({ROLE_UTENTE})
    public UserAnalyticsDTO getUserAnalytics() {


        String emailUtente = userCheckService.getCurrentUserEmail();
        try {
            Optional<UserAnalyticsDTO> userAnalyticsDTO = calculateAnalyticsService.getUserAnalytics(emailUtente);

            if (userAnalyticsDTO.isEmpty()) {
                throw new MissingDataException(NO_DATA_FOUND_FOR_USER + emailUtente);
            }

            return userAnalyticsDTO.get();

        } catch (MissingDataException e) {
            throw new MissingDataException(e.getMessage());
        } catch (Exception e) {
            throw new MissingDataException(AnalyticsQueryConstants.ERROR + e.getMessage());
        }

    }

    @GetMapping("/user/get/task/all")
    @Secured({ROLE_UTENTE})
    public ListTaskAnalytics getUserAnalyticsByTask() {
        String emailUtente = userCheckService.getCurrentUserEmail();
        try {
            List<AssignmentAnalytics> allAssignments = assignmentAnalyticsRepository.findAllByEmailUtente(emailUtente);

            ListTaskAnalytics listDTO = new ListTaskAnalytics();
            List<TaskAnalyticsDTO> list = new ArrayList<>();

            for (AssignmentAnalytics assignment : allAssignments) {
                Optional<UserAnalyticsDTO> userAnalyticsDTO1 = calculateAnalyticsService.getTaskUserAnalytics(assignment.getTaskId());
                TaskAnalyticsDTO taskAnalyticsDTO = new TaskAnalyticsDTO();
                if (userAnalyticsDTO1.isPresent()) {
                    taskAnalyticsDTO.setTaskId(assignment.getTaskId());
                    taskAnalyticsDTO.setEnergySaved(userAnalyticsDTO1.get().getEnergySaved());
                    taskAnalyticsDTO.setComputingPowerUsed(userAnalyticsDTO1.get().getComputingPowerUsed());
                    list.add(taskAnalyticsDTO);
                }
            }

            listDTO.setList(list);
            return listDTO;

        } catch (Exception e) {
            throw new MissingDataException(ERROR + e.getMessage());
        }

    }

    @GetMapping("/user/get/filter")
    @Secured({ROLE_UTENTE})
    public UserAnalyticsDTO getUserAnalyticsByDate(@RequestParam(required = false) String taskId,@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        String emailUtente = userCheckService.getCurrentUserEmail();
        LocalDateTime start = null;
        LocalDateTime end = null;
        try {
            if (startDate != null) {
                start = LocalDateTime.parse(startDate);
            } else if (endDate != null) {
                end = LocalDateTime.now();
            }
        } catch (Exception e) {
            throw new MissingDataException("Wrong date format. Please use yyyy-MM-ddTHH:mm:ss format");
        }

        try {
            Optional<UserAnalyticsDTO> userAnalyticsDTO;

            if(taskId == null) {
               userAnalyticsDTO = calculateAnalyticsService.getUserAnalytics(emailUtente, start, end);
            }else{
                userAnalyticsDTO = calculateAnalyticsService.getTaskUserAnalytics(taskId);
            }

            if (userAnalyticsDTO.isEmpty()) {
                throw new MissingDataException("No data found for user " + emailUtente);
            }

            return userAnalyticsDTO.get();

        } catch (MissingDataException e) {
            throw new MissingDataException(e.getMessage());
        } catch (Exception e) {
            throw new MissingDataException(ERROR + e.getMessage());
        }

    }


    @GetMapping("/member/get")
    @Secured({ROLE_MEMBRO})
    public MemberAnalyticsDTO getMemberAnalytics() {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String emailMembro = userDetails.getUsername();

        try {
            Optional<MemberAnalyticsDTO> memberAnalyticsDTO = calculateAnalyticsService.getMemberAnalytics(emailMembro);

            if (memberAnalyticsDTO.isEmpty()) {
                throw new MissingDataException(NO_DATA_FOUND_FOR_USER + emailMembro);
            }

            return memberAnalyticsDTO.get();

        } catch (MissingDataException e) {
            throw new MissingDataException(e.getMessage());
        } catch (Exception e) {
            throw new MissingDataException(ERROR + e.getMessage());
        }

    }

    @GetMapping("/member/get/filter")
    @Secured({ROLE_MEMBRO})
    public MemberAnalyticsDTO getMemberAnalyticsByDate(@RequestParam String startDate, @RequestParam String endDate) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String emailMembro = userDetails.getUsername();

        LocalDateTime start;
        LocalDateTime end;

        try {
            start = LocalDateTime.parse(startDate);
            end = LocalDateTime.parse(endDate);
        } catch (Exception e) {
            throw new BadFormatRequestException("Wrong date format. Please use yyyy-MM-ddTHH:mm:ss format");
        }

        try {
            Optional<MemberAnalyticsDTO> memberAnalyticsDTO = calculateAnalyticsService.getMemberAnalytics(emailMembro, start, end);

            if (memberAnalyticsDTO.isEmpty()) {
                throw new MissingDataException(NO_DATA_FOUND_FOR_USER + emailMembro);
            }

            return memberAnalyticsDTO.get();

        } catch (MissingDataException e) {
            throw new MissingDataException(e.getMessage());
        } catch (Exception e) {
            throw new MissingDataException(ERROR + e.getMessage());
        }

    }

    @GetMapping("/get")
    @Secured({ROLE_ADMIN})
    public AnalyticsDTO getAllAnalytics() {
        try {
            return calculateAnalyticsService.getOverallAnalytics();
        } catch (Exception e) {
            throw new MissingDataException(ERROR + e.getMessage());
        }
    }

    @GetMapping("/admin/get/user")
    @Secured({ROLE_ADMIN})
    public UserAnalyticsDTO getUserAnalytics(@RequestParam String email) {
        try {
            Optional<UserAnalyticsDTO> userAnalyticsDTO = calculateAnalyticsService.getUserAnalytics(email);

            if (userAnalyticsDTO.isEmpty()) {
                throw new MissingDataException(NO_DATA_FOUND_FOR_USER + email);
            }

            return userAnalyticsDTO.get();

        } catch (MissingDataException e) {
            throw new MissingDataException(e.getMessage());
        } catch (Exception e) {
            throw new MissingDataException(AnalyticsQueryConstants.ERROR + e.getMessage());
        }

    }


}
