package com.harshitha.smarttaskmanager.scheduler;

import com.harshitha.smarttaskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskCleanupScheduler {

    private final TaskService taskService;

    // ✅ daily 2 AM delete completed tasks
    @Scheduled(cron = "0 0 2 * * *")
    public void deleteCompletedTasksDaily() {
        taskService.deleteCompletedTasks();
        System.out.println("🧹 Auto-deleted completed tasks");
    }

    // ✅ every 1 minute delete expired tasks
    @Scheduled(fixedRate = 60000)
    public void deleteExpiredTasksAutomatically() {
        taskService.deleteExpiredTasks();
        System.out.println("⏳ Auto-deleted expired tasks");
    }
}