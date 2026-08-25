package com.photo.starsnap.network.file

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FileApiRepositoryImplTest {

    @Test
    fun `presigned upload headers exclude credentials and use validated content type`() {
        val headers = buildPresignedUploadHeaders(
            contentType = "image/png",
            requiredHeaders = mapOf(
                "Authorization" to "secret",
                "Cookie" to "session=secret",
                "Proxy-Authorization" to "proxy-secret",
                "content-type" to "image/heic",
                "x-amz-meta-source" to "camera"
            )
        )

        assertEquals("image/png", headers["Content-Type"])
        assertEquals("camera", headers["x-amz-meta-source"])
        assertFalse(headers.keys.any { it.equals("Authorization", ignoreCase = true) })
        assertFalse(headers.keys.any { it.equals("Cookie", ignoreCase = true) })
        assertFalse(headers.keys.any { it.equals("Proxy-Authorization", ignoreCase = true) })
    }
}
