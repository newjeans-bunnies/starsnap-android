package com.photo.starsnap.main.viewmodel.main

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

internal const val MAX_SNAP_PHOTO_BYTES = 15L * 1024L * 1024L

private val SUPPORTED_SNAP_PHOTO_TYPES = setOf(
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/webp"
)

internal data class ValidatedSnapPhoto(
    val contentType: String,
    val sizeBytes: Long
)

internal class UploadInputException(message: String) : IOException(message)

internal fun validateSnapPhoto(
    contentType: String?,
    sizeBytes: Long?
): ValidatedSnapPhoto {
    val normalizedContentType = contentType
        ?.substringBefore(';')
        ?.trim()
        ?.lowercase(Locale.ROOT)

    if (normalizedContentType == null || normalizedContentType !in SUPPORTED_SNAP_PHOTO_TYPES) {
        throw UploadInputException("JPEG, PNG, WebP 사진만 업로드할 수 있어요.")
    }
    if (sizeBytes == null || sizeBytes <= 0L) {
        throw UploadInputException("사진 크기를 확인할 수 없어요. 다른 사진을 선택해 주세요.")
    }
    if (sizeBytes > MAX_SNAP_PHOTO_BYTES) {
        throw UploadInputException("사진 한 장은 15 MiB 이하여야 해요.")
    }

    return ValidatedSnapPhoto(
        contentType = normalizedContentType,
        sizeBytes = sizeBytes
    )
}

internal fun ContentResolver.createSnapPhotoRequestBody(
    uri: Uri
): Pair<ValidatedSnapPhoto, RequestBody> {
    val photo = validateSnapPhoto(
        contentType = getType(uri),
        sizeBytes = queryOpenableSize(uri)
    )
    return photo to ContentResolverRequestBody(
        contentResolver = this,
        uri = uri,
        mediaType = photo.contentType.toMediaType(),
        expectedLength = photo.sizeBytes
    )
}

private fun ContentResolver.queryOpenableSize(uri: Uri): Long? {
    val queriedSize = query(
        uri,
        arrayOf(OpenableColumns.SIZE),
        null,
        null,
        null
    )?.use { cursor ->
        val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (sizeColumn >= 0 && cursor.moveToFirst() && !cursor.isNull(sizeColumn)) {
            cursor.getLong(sizeColumn).takeIf { it >= 0L }
        } else {
            null
        }
    }
    if (queriedSize != null) return queriedSize

    return openAssetFileDescriptor(uri, "r")?.use { descriptor ->
        descriptor.length.takeIf { it >= 0L }
    }
}

private class ContentResolverRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val mediaType: MediaType,
    private val expectedLength: Long
) : RequestBody() {

    override fun contentType(): MediaType = mediaType

    override fun contentLength(): Long = expectedLength

    override fun writeTo(sink: BufferedSink) {
        val input = contentResolver.openInputStream(uri)
            ?: throw IOException("선택한 사진을 열 수 없습니다.")

        input.use {
            copyExactLength(it, sink, expectedLength)
        }
    }
}

internal fun copyExactLength(
    input: InputStream,
    sink: BufferedSink,
    expectedLength: Long
) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var remaining = expectedLength

    while (remaining > 0L) {
        val read = input.read(
            buffer,
            0,
            minOf(buffer.size.toLong(), remaining).toInt()
        )
        if (read <= 0) {
            throw IOException("업로드 중 사진 크기가 변경되었습니다.")
        }
        sink.write(buffer, 0, read)
        remaining -= read
    }

    if (input.read() != -1) {
        throw IOException("업로드 중 사진 크기가 변경되었습니다.")
    }
}

internal class SingleUploadGuard {
    private val active = AtomicBoolean(false)

    fun tryStart(): Boolean = active.compareAndSet(false, true)

    fun finish() {
        active.set(false)
    }
}
