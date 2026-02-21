package com.harshitha.smarttaskmanager.service;

import com.harshitha.smarttaskmanager.entity.Task;
import com.harshitha.smarttaskmanager.exception.TaskNotFoundException;
import com.harshitha.smarttaskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    // ✅ Create
    public Task createTask(Task task) {
        // make sure defaults are safe
        if (task.getDueDate() == null) {
            throw new IllegalArgumentException("dueDate is required");
        }
        task.setCompleted(false);
        task.setNotificationSent(false);
        return taskRepository.save(task);
    }

    // ✅ Get all pending tasks sorted by dueDate/time
    public List<Task> getAllTasksSorted() {
        return taskRepository.findByCompletedFalseOrderByDueDateAsc();
    }

    // ✅ Get by id
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    // ✅ Update full task (title/description/dueDate/completed etc.)
    public Task updateTask(Long id, Task updated) {
        Task existing = getTaskById(id);

        // Update fields
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setDueDate(updated.getDueDate());
        existing.setCompleted(updated.isCompleted());

        // If task is marked incomplete again, allow reminders again
        if (!updated.isCompleted()) {
            existing.setNotificationSent(false);
        }

        return taskRepository.save(existing);
    }

    // ✅ Mark completed (used if you call /{id}/complete)
    public Task markCompleted(Long id) {
        Task task = getTaskById(id);
        task.setCompleted(true);
        return taskRepository.save(task);
    }

    // ✅ Delete manually
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    // ✅ For scheduler: tasks due in next N minutes and not notified yet (sorted)
    public List<Task> getReminderTasks(LocalDateTime start, LocalDateTime end) {
        return taskRepository.findByCompletedFalseAndNotificationSentFalseAndDueDateBetweenOrderByDueDateAsc(start, end);
    }

    // ✅ Mark notification sent (so it won't send again)
    public Task markNotificationSent(Long id) {
        Task task = getTaskById(id);
        task.setNotificationSent(true);
        return taskRepository.save(task);
    }

    // ✅ Auto delete completed tasks
    @Transactional
    public void deleteCompletedTasks() {
        taskRepository.deleteByCompletedTrue();
    }

    // ✅ Auto delete tasks whose due time is already passed and NOT completed
    // (this is your "automatic delete after time got completed")
    @Transactional
    public void deleteExpiredPendingTasks() {
        taskRepository.deleteByCompletedFalseAndDueDateBefore(LocalDateTime.now());
    }
}