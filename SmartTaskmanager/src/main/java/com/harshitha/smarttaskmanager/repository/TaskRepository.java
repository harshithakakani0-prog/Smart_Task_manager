package com.harshitha.smarttaskmanager.repository;

import com.harshitha.smarttaskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // ✅ 1) Reminder tasks (due between now and next 10 minutes) + sorted
    List<Task> findByCompletedFalseAndNotificationSentFalseAndDueDateBetweenOrderByDueDateAsc(
            LocalDateTime start,
            LocalDateTime end
    );

    // ✅ 2) Get all pending tasks sorted by due date/time
    List<Task> findByCompletedFalseOrderByDueDateAsc();

    // ✅ 3) Optional: get completed tasks
    List<Task> findByCompletedTrue();

    // ✅ 4) Auto delete completed tasks
    void deleteByCompletedTrue();

    // ✅ 5) Auto delete expired tasks (due time already passed & not completed)
    void deleteByCompletedFalseAndDueDateBefore(LocalDateTime time);
}