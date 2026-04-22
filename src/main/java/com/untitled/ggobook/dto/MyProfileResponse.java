package com.untitled.ggobook.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

//마이페이지 메인 화면용 전체 데이터
@Getter
@Builder
public class MyProfileResponse {
    private String userId;
    private String name;
    private String nickname;
    private String email;
    // 나중에 Point 도메인과 연동할 때 쓸 포인트 변수 (미리 세팅)
    // private int pointBalance;

    // 내가 작성한 글 목록을 함께 담아줍니다.
    private List<MyPostDto> myPosts;
}