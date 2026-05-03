package com.untitled.ggobook.controller;

import com.untitled.ggobook.repository.ReadingRepository;
import com.untitled.ggobook.service.ReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;

    @GetMapping("/{contentId}")
    public ResponseEntity<List<Long>> getReadEpisodeIds(
            @AuthenticationPrincipal Long id,
            @PathVariable Long contentId
    ) {
        return ResponseEntity.ok(readingService.getReadEpisodeIds(id, contentId));
    }

}
