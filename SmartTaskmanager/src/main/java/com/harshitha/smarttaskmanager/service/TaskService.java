package com.harshitha.smarttaskmanager.service;

import com.harshitha.smarttaskmanager.entity.Task;
import com.harshitha.smarttaskmanager.exception.TaskNotFoundException;
import com.harshitha.smarttaskmanager.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // ✅ Get all tasks (sorted by dueTime ascending)
    public List<Task> getAllTasks() {
        return taskRepository.findAll(
                Sort.by(Sort.Direction.ASC, "dueTime")
        );
    }

    // ✅ Create new task
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    // ✅ Get task by ID
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException("Task not found with id: " + id));
    }

    // ✅ Update task
    public Task updateTask(Long id, Task updatedTask) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException("Task not found with id: " + id));

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setDueDate(updatedTask.getDueDate());
        task.setDueTime(updatedTask.getDueTime());
        task.setCompleted(updatedTask.isCompleted());

        return taskRepository.save(task);
    }

    // ✅ Delete task
    public void deleteTask(Long id) {

        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with id: " + id);
        }

        taskRepository.deleteById(id);
    }
}
