package com.photo.starsnap.network.snap

import com.photo.starsnap.network.dto.SliceResponseDto
import com.photo.starsnap.network.dto.StatusDto
import com.photo.starsnap.network.snap.dto.CommentDto
import com.photo.starsnap.network.snap.dto.CreateCommentRequestDto
import com.photo.starsnap.network.snap.dto.CreateSnapRequestDto
import com.photo.starsnap.network.snap.dto.SnapDto
import com.photo.starsnap.network.snap.dto.SnapLikeToggleDto
import com.photo.starsnap.network.snap.dto.SnapResponseDto
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Multipart

interface SnapApi {
    @Multipart
    @POST("/api/snap/create") // snap 생성
    suspend fun createSnap(
        @Part("snapDto") snapDto: CreateSnapRequestDto
    )

    @GET("/api/snap/send") // snap 조회
    @Headers("Auth: false")
    suspend fun sendSnap(@Query("size") size: Int, @Query("page") page: Int)

    @Multipart
    @PATCH("/api/snap/fix") // snap 수정
    suspend fun fixSnap(
        @Part("snap-id") snapId: RequestBody,
        @Part("image") image: RequestBody?,
        @Part("title") title: RequestBody,
        @Part("source") source: RequestBody,
        @Part("date-taken") dateTaken: RequestBody,
        @Part("ai-state") aiState: RequestBody,
        @Part("tag") tag: List<RequestBody>,
        @Part("star-id") starId: List<RequestBody>,
        @Part("star-group-id") starGroupId: List<RequestBody>
    ): SnapDto

    @GET("/api/snap/feed") // snap 기본 조회
    suspend fun getFeedSnap(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): SliceResponseDto<SnapResponseDto>

    @GET("/api/snap/{snapId}/related") // 얼굴 벡터 기반 연관 스냅 조회
    suspend fun getRelatedSnaps(
        @Path("snapId") snapId: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): SliceResponseDto<SnapResponseDto>

    @POST("/api/snap/like") // snap 좋아요 토글
    suspend fun toggleSnapLike(
        @Query("snap-id") snapId: String
    ): SnapLikeToggleDto


    @PATCH("/api/snap/delete") // snap 삭제 (소프트 삭제)
    suspend fun deleteSnap(
        @Query("snap-id") snapId: String
    ): StatusDto

    @GET("/api/snap")
    suspend fun getSnap(
        size: Int,
        page: Int,
        tag: List<String>,
        title: String,
        userId: String,
        starId: List<String>,
        starGroupId: List<String>
    ): SliceResponseDto<SnapResponseDto>

    @POST("/api/snap/save") // snap 저장
    suspend fun saveSnap(
        @Query("snap-id") snapId: String
    ): StatusDto

    @DELETE("/api/snap/un-save") // snap 저장 취소
    suspend fun unSaveSnap(
        @Query("snap-id") snapId: String
    ): StatusDto

    @GET("/api/snap/saved") // 저장한 snap 목록
    suspend fun getSavedSnaps(): List<SnapResponseDto>

    @GET("/api/snap/star-group") // 스타그룹 연결 snap 목록
    suspend fun getSnapsByStarGroup(
        @Query("star-group-id") starGroupId: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): SliceResponseDto<SnapResponseDto>

    @POST("/api/snap/comment/create") // 댓글 작성
    suspend fun createComment(
        @Body body: CreateCommentRequestDto
    ): CommentDto

}
