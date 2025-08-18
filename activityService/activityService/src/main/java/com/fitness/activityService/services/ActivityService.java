package com.fitness.activityService.services;

import com.fitness.activityService.dto.ActivityRequestDto;
import com.fitness.activityService.dto.ActivityResponseDto;
import com.fitness.activityService.model.Activity;
import com.fitness.activityService.repositories.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Log4j2
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routing;

    public ActivityResponseDto trackActivity(ActivityRequestDto request) {

        boolean isValidUser = userValidationService.validateUser(request.getUserId());
        if(!isValidUser) {
            log.error("Invalid user ID: {}", request.getUserId());
            throw new RuntimeException("Invalid user ID: " + request.getUserId());
        }

        Activity activity = Activity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .additionalMetrics(request.getAdditionalMetrics())
                .build();

        activity = activityRepository.save(activity);
        log.info("Activity tracked successfully for user: {}", request.getUserId());

        //Publish activity to RabbitMQ
        try{
            rabbitTemplate.convertAndSend(exchange, routing, activity);
            log.info("Activity published to RabbitMQ for user: {}", request.getUserId());
        }catch (Exception e){
            log.error("Failed to publish activity to RabbitMQ for user: {}. Error: {}", request.getUserId(), e.getMessage());
        }
        return mapToResponseDto(activity);
    }

    private ActivityResponseDto mapToResponseDto(Activity activity) {
        ActivityResponseDto response = new ActivityResponseDto();
        response.setId(activity.getId());
        response.setUserId(activity.getUserId());
        response.setType(activity.getType());
        response.setDuration(activity.getDuration());
        response.setCaloriesBurned(activity.getCaloriesBurned());
        response.setStartTime(activity.getStartTime());
        response.setAdditionalMetrics(activity.getAdditionalMetrics());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        return response;
    }

    public List<ActivityResponseDto> getUserActivities(String userId) {
        List<Activity> activities = activityRepository.findByUserId(userId);
        return activities.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public ActivityResponseDto getActivityById(String activityId) {
        return activityRepository.findById(activityId)
                .map(this::mapToResponseDto)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + activityId));
    }
}
