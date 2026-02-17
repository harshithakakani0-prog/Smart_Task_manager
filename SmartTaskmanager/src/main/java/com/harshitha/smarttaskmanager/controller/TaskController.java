package com.harshitha.smarttaskmanager.controller;

import com.harshitha.smarttaskmanager.entity.Task;
import com.harshitha.smarttaskmanager.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    // Constructor Injection
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ✅ Create Task
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }

    // ✅ Get All Tasks Sorted by Date & Time
    @GetMapping("/sorted")
    public List<Task> getAllTasksSorted() {
        return taskService.getAllTasksSorted();
    }

    // ✅ Get Task By ID
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    // ✅ Update Task
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id,
                           @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    // ✅ Delete Task
    // ✅ Delete Task
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "Task deleted successfully!";
    }
}