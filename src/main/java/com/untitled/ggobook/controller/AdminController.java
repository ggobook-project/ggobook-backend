package com.untitled.ggobook.controller;

import com.untitled.ggobook.dto.AdminDashboardDTO;
import com.untitled.ggobook.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 🌟 대시보드 데이터를 통합적으로 반환하는 API
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        AdminDashboardDTO dashboardData = adminService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }
}