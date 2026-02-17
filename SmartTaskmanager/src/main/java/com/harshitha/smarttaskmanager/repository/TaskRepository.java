package com.harshitha.smarttaskmanager.repository;

import com.harshitha.smarttaskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Sort by Date first, then Time
    List<Task> findAllByOrderByDueDateAscDueTimeAsc();

}
