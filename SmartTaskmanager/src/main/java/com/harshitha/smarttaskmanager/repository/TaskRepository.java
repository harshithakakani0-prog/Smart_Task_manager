package com.harshitha.smarttaskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.harshitha.smarttaskmanager.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}