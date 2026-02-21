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
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (task.getDueDate() == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }

        // Safety defaults
        if (task.getDescription() == null) task.setDescription("");
        task.setCompleted(false);
        task.setNotificationSent(false);

        return taskRepository.save(task);
    }

    // ✅ Get all sorted by dueDate
    public List<Task> getAllTasksSorted() {
        return taskRepository.findAllByOrderByDueDateAsc();
    }

    // ✅ Get by id
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    // ✅ Update (React sends full object)
    public Task updateTask(Long id, Task updatedTask) {
        Task existing = getTaskById(id);

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setDueDate(updatedTask.getDueDate());
        existing.setCompleted(updatedTask.isCompleted());

        // Optional: if task becomes completed, no need reminder
        if (existing.isCompleted()) {
            existing.setNotificationSent(true);
        }

        return taskRepository.save(existing);
    }

    // ✅ Delete
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    // ==========================================================
    // ✅ AUTO DELETE METHODS (used by scheduler)
    // ==========================================================

    // ✅ Delete completed tasks (transaction needed for delete queries)
    @Transactional
    public void deleteCompletedTasks() {
        taskRepository.deleteByCompletedTrue();
    }

    // ✅ Delete expired tasks where dueDate has passed and task not completed
    @Transactional
    public void deleteExpiredTasks() {
        LocalDateTime nowMinus1Min = LocalDateTime.now().minusMinutes(1);
        taskRepository.deleteByCompletedFalseAndDueDateBefore(nowMinus1Min);
    }
}