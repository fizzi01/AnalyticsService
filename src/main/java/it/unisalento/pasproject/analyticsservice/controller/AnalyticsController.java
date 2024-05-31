package it.unisalento.pasproject.analyticsservice.controller;

import it.unisalento.pasproject.analyticsservice.dto.AnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.MemberAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.dto.UserAnalyticsDTO;
import it.unisalento.pasproject.analyticsservice.exceptions.BadFormatRequestException;
import it.unisalento.pasproject.analyticsservice.exceptions.MissingDataException;
import it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants;
import it.unisalento.pasproject.analyticsservice.service.CalculateAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

import static it.unisalento.pasproject.analyticsservice.security.SecurityConstants.*;
import static it.unisalento.pasproject.analyticsservice.service.AnalyticsQueryConstants.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final CalculateAnalyticsService calculateAnalyticsService;


    @Autowired
    public AnalyticsController(CalculateAnalyticsService calculateAnalyticsService) {
        this.calculateAnalyticsService = calculateAnalyticsService;
    }

    @GetMapping("/user/get")
    @Secured({ROLE_UTENTE})
    public UserAnalyticsDTO getUserAnalytics() {


        String emailUtente = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

        try{
            Optional<UserAnalyticsDTO> userAnalyticsDTO = calculateAnalyticsService.getUserAnalytics(emailUtente);

            if (userAnalyticsDTO.isEmpty()) {
                throw new MissingDataException(NO_DATA_FOUND_FOR_USER + emailUtente);
            }

            return userAnalyticsDTO.get();

        }catch (MissingDataException e){
            throw new MissingDataException(e.getMessage());
        } catch (Exception e) {
            throw new MissingDataException(AnalyticsQueryConstants.ERROR + e.getMessage());
        }

    }

    @GetMapping("/user/get/filter")
    @Secured({ROLE_UTENTE})
    public UserAnalyticsDTO getUserAnalyticsByDate(@RequestParam String startDate, @RequestParam String endDate) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String emailUtente = userDetails.getUsername();
        LocalDateTime start = LocalDateTime.parse(startDate);
        LocalDateTime end = LocalDateTime.parse(endDate);
        try{
            Optional<UserAnalyticsDTO> userAnalyticsDTO = calculateAnalyticsService.getUserAnalytics(emailUtente, start, end);

            if (userAnalyticsDTO.isEmpty()) {
                throw new MissingDataException("No data found for user " + emailUtente);
            }

            return userAnalyticsDTO.get();

        }catch (MissingDataException e){
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
            Optional<MemberAnalyticsDTO> memberAnalyticsDTO = calculateAnalyticsService.getMemberAnalytics(emailMembro,start,end);

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
        try{
            return calculateAnalyticsService.getOverallAnalytics();
        } catch (Exception e) {
            throw new MissingDataException(ERROR + e.getMessage());
        }
    }


}
