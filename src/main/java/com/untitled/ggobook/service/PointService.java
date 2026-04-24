package com.untitled.ggobook.service;

import com.untitled.ggobook.domain.Point;
import com.untitled.ggobook.domain.User;
import com.untitled.ggobook.repository.PointRepository;
import com.untitled.ggobook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

// 포인트 서비스
@Service
@Slf4j
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

//    public ResponseEntity<Point> getPonitBalance(Long id) {
//        User user = userRepository.findById(id);
//        return pointRepository.findByUser(user);
//    }
}
