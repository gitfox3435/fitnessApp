package com.fitness.activityService.controller;

import com.fitness.activityService.dto.ActivityRequestDto;
import com.fitness.activityService.dto.ActivityResponseDto;
import com.fitness.activityService.services.ActivityService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
@Log4j2
public class ActivityController {

    private ActivityService activityService;

    @PostMapping
    public ResponseEntity<ActivityResponseDto> trackActivity(@RequestBody ActivityRequestDto request,  @RequestHeader ("X-User-Id") String userId) {
        if(userId != null) {
            request.setUserId(userId);
        }
        log.info("Received activity tracking request for userId: {}", request.getUserId());
        return ResponseEntity.ok(activityService.trackActivity(request));
    }

    @GetMapping
    public ResponseEntity <List<ActivityResponseDto>> getUserActivities(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(activityService.getUserActivities(userId));
    }

    @GetMapping("/{activityId}")
    public ResponseEntity <ActivityResponseDto> getActivity(@PathVariable String activityId) {
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }
}
