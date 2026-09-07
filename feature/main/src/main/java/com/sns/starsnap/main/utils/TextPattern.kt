package com.sns.starsnap.main.utils

object TextPattern {
    // 닉네임 정규식
    val USER_NAME = Regex("^[a-zA-Z0-9]{4,12}\$")
    // 비밀번호 정규식
    val PASSWORD = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*\\d)(?=.*[~․!@#\$%^&*()_\\-+=|\\\\;:‘“<>,.?/]).{8,50}\$")
    // 이메일 정규식
    val EMAIL = Regex("^[a-zA-Z0-9+-_.]+@[0-9a-zA-Z]+\\.[a-zA-Z]{2,3}\$")
}