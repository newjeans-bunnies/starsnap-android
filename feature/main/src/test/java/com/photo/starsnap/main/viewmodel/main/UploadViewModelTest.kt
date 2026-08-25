package com.photo.starsnap.main.viewmodel.main

import okio.Buffer
import java.io.ByteArrayInputStream
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class UploadViewModelTest {

    @Test
    fun `extracts photo key from virtual hosted presigned url`() {
        val url = "https://bucket.s3.ap-northeast-2.amazonaws.com/photo/abc-123?X-Amz-Signature=test"

        assertEquals("photo/abc-123", extractPhotoFileKey(url))
    }

    @Test
    fun `extracts photo key from path style presigned url`() {
        val url = "https://s3.ap-northeast-2.amazonaws.com/bucket/photo/nested%20name?X-Amz-Signature=test"

        assertEquals("photo/nested name", extractPhotoFileKey(url))
    }

    @Test
    fun `single upload guard rejects a second in flight publish`() {
        val guard = SingleUploadGuard()

        assertTrue(guard.tryStart())
        assertFalse(guard.tryStart())
        guard.finish()
        assertTrue(guard.tryStart())
    }

    @Test
    fun `accepts supported photo at the 15 MiB boundary`() {
        val photo = validateSnapPhoto(
            contentType = " IMAGE/WEBP; charset=binary ",
            sizeBytes = MAX_SNAP_PHOTO_BYTES
        )

        assertEquals("image/webp", photo.contentType)
        assertEquals(MAX_SNAP_PHOTO_BYTES, photo.sizeBytes)
    }

    @Test
    fun `rejects HEIC before requesting a presigned url`() {
        val error = assertThrows(UploadInputException::class.java) {
            validateSnapPhoto("image/heic", 1024L)
        }

        assertEquals("JPEG, PNG, WebP 사진만 업로드할 수 있어요.", error.message)
    }

    @Test
    fun `rejects a photo larger than 15 MiB`() {
        assertThrows(UploadInputException::class.java) {
            validateSnapPhoto("image/jpeg", MAX_SNAP_PHOTO_BYTES + 1L)
        }
    }

    @Test
    fun `streams exactly the declared bytes and rejects changed content`() {
        val source = byteArrayOf(1, 2, 3, 4)
        val sink = Buffer()

        copyExactLength(ByteArrayInputStream(source), sink, source.size.toLong())
        assertTrue(source.contentEquals(sink.readByteArray()))

        assertThrows(IOException::class.java) {
            copyExactLength(ByteArrayInputStream(source), Buffer(), 3L)
        }
    }
}
