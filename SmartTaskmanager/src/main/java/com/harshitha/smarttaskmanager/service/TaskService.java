package com.harshitha.smarttaskmanager.service;

import com.harshitha.smarttaskmanager.entity.Task;
import com.harshitha.smarttaskmanager.exception.TaskNotFoundException;
import com.harshitha.smarttaskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    // Constructor Injection
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // ✅ Create Task
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    // ✅ Get All Tasks (Sorted by Date & Time)
    public List<Task> getAllTasksSorted() {
        return taskRepository.findAllByOrderByDueDateAscDueTimeAsc();
    }

    // ✅ Get Task By ID
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException("Task not found with id: " + id));
    }

    // ✅ Update Task
    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = getTaskById(id);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setDueTime(updatedTask.getDueTime());
        existingTask.setCompleted(updatedTask.isCompleted());

        return taskRepository.save(existingTask);
    }

    // ✅ Delete Task
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }
}
