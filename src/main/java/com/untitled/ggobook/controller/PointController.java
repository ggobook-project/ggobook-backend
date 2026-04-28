package com.untitled.ggobook.controller;

import com.untitled.ggobook.domain.Point;
import com.untitled.ggobook.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 포인트 컨트롤러
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping
    public ResponseEntity<List<Point>> getPointHistory(
            @AuthenticationPrincipal Long id
    ) {
        return ResponseEntity.ok(pointService.getPointHistory(id));
    }

}
