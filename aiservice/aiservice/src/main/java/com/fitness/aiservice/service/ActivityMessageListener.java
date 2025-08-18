package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ActivityMessageListener {

    private final ActivityAiService aiService;

    private final RecommendationRepository recommendationRepository;

    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity){
        log.info("Received activity message: {} for processing", activity.getId());
        log.info("Generated Recommendation : {}", aiService.generateRecommendation(activity));

        Recommendation recommendation = aiService.generateRecommendation(activity);
        log.info("Saving recommendation: {}", recommendation);
        try {
            recommendationRepository.save(recommendation);
            log.info("Recommendation saved successfully");
        } catch (Exception e) {
            log.error("Error saving recommendation: {}", e.getMessage(), e);
        }
    }
}
