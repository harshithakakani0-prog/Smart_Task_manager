package com.harshitha.smarttaskmanager.repository;

import com.harshitha.smarttaskmanager.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    List<UserDevice> findAll();
}