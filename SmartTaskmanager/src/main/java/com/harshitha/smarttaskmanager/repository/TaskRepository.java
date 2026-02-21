package com.harshitha.smarttaskmanager.repository;

import com.harshitha.smarttaskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // ✅ Get all tasks sorted by dueDate
    List<Task> findAllByOrderByDueDateAsc();

    // ✅ Delete completed tasks
    void deleteByCompletedTrue();

    // ✅ Delete expired tasks (not completed + dueDate < time)
    void deleteByCompletedFalseAndDueDateBefore(LocalDateTime time);
}