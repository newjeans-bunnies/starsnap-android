package com.sns.starsnap.main.viewmodel.state

enum class AutoLoginState {
    Success, // 로그인 성공
    Failure, // 로그인 실패
    Idle, // 로그인 시도 전
    Loading // 로그인 시도 중
}