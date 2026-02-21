package com.harshitha.smarttaskmanager.scheduler;

import com.harshitha.smarttaskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TaskCleanupScheduler {

    private final TaskRepository taskRepository;

    // ==========================================================
    // ✅ 1. Delete completed tasks DAILY at 2 AM
    // ==========================================================
    @Scheduled(cron = "0 0 2 * * *")
    public void deleteCompletedTasksDaily() {
        taskRepository.deleteByCompletedTrue();
        System.out.println("🧹 Auto-deleted completed tasks (2 AM cleanup)");
    }

    // ==========================================================
    // ✅ 2. Delete expired tasks (due time passed)
    // Runs every 1 minute
    // Grace time: 1 minute
    // ==========================================================
    @Scheduled(fixedRate = 60000)
    public void deleteExpiredTasksAutomatically() {

        LocalDateTime nowMinus1Min = LocalDateTime.now().minusMinutes(1);

        taskRepository.deleteByCompletedFalseAndDueDateBefore(nowMinus1Min);

        System.out.println("⏳ Auto-deleted expired tasks (due time passed)");
    }
}