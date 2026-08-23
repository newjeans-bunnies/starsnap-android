# StarSnap Android

StarSnap의 Android 네이티브 클라이언트다. 로그인, 스냅 피드·업로드, 스타 탐색, 친구, 프로필, 실시간 채팅과 FCM 알림을 Jetpack Compose UI로 제공한다.

## 주요 기능

- Google 인증과 쿠키 기반 세션 유지
- 스냅·댓글·좋아요·저장·신고와 미디어 업로드
- 스타·스타 그룹·사용자 검색과 친구 관리
- WebSocket 채팅, 전송 제한 안내와 실패 초안 복구
- Firebase 토큰 등록, 친구·채팅 알림과 설정 ON/OFF

## 언어와 기술 스택

`app/build.gradle.kts`와 `gradle/libs.versions.toml` 기준이다.

| 구분 | 기술 | 버전 |
|---|---|---:|
| 언어/JVM | Kotlin / Java | 2.3.21 / 21 |
| Android | AGP, min/target/compile SDK | 8.13.1, 28/35/35 |
| UI | Jetpack Compose BOM | 2025.11.01 |
| DI | Hilt | 2.58 |
| HTTP | Retrofit / OkHttp | 2.9.0 / 4.12.0 |
| 비동기 | Coroutines | 1.10.2 |
| 저장/작업 | DataStore / WorkManager | 1.1.1 / 2.11.2 |
| 푸시 | Firebase BOM | 34.17.0 |

## 시스템 아키텍처

~~~mermaid
flowchart LR
    UI[Compose 화면] --> VM[ViewModel]
    VM --> Net[core:network]
    VM --> Store[core:datastore]
    Net -->|Retrofit + 쿠키| API[메인 API]
    Net -->|WebSocket| Chat[채팅]
    UI -->|presigned PUT| S3[S3]
    FCM[Firebase] --> Service[Messaging Service]
    Service --> Notify[Android 알림]
    DI[core:di / Hilt] --> VM
~~~

## 프로젝트 구조

~~~text
starsnap-android/
├─ app/                 # 앱 조립, Activity, FCM 서비스
├─ feature/main/        # 화면, 라우팅, ViewModel
├─ core/network/        # REST, WebSocket, 인증·쿠키
├─ core/model/          # 공통 모델
├─ core/di/             # Hilt 의존성 조립
├─ core/designsystem/   # Compose 테마와 공통 UI
├─ core/datastore/      # 사용자·알림 설정
└─ gradle/libs.versions.toml
~~~

## 외부 연동과 설정

메인 REST API, 채팅 WebSocket, S3 presigned 업로드, Firebase Authentication/Cloud Messaging, Google Identity를 사용한다. SDK 경로는 `local.properties`, Firebase 설정은 `app/google-services.json`에 두며 두 파일 모두 Git에 커밋하지 않는다.

## 빌드와 테스트

~~~powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
.\gradlew.bat test
.\gradlew.bat :app:assembleDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
~~~

계측 테스트에는 에뮬레이터나 실제 기기가 필요하다. 일반 FCM 수신과 실제 친구·채팅 업무 이벤트 발송은 구분해서 검증한다.

## 관련 문서

- [Main 통합 개요](../README.md)
- [메인 API 명세](../starsnap-backend/API_SPEC.md)
- [공통 디자인 시스템](../../DESIGN_SYSTEM.md)

