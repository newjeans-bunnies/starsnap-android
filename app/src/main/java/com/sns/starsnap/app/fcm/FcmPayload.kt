package com.sns.starsnap.app.fcm

internal data class FcmPayload(
    val title: String,
    val body: String,
    val type: String,
    val actorUserId: String?,
    val snapId: String?,
    val eventId: String?,
    val roomId: String?,
    val messageId: String?,
    val senderUserId: String?,
)

internal object FcmPayloadParser {
    private const val DEFAULT_TITLE = "StarSnap"
    private const val DEFAULT_TYPE = "general"

    private val supportedTypes = setOf(
        "friend_request",
        "friend_accepted",
        "snap_liked",
        "chat_message",
        "general",
    )

    fun parse(
        notificationTitle: String?,
        notificationBody: String?,
        data: Map<String, String>,
    ): FcmPayload? {
        val body = notificationBody.clean(MAX_BODY_LENGTH)
            ?: data["body"].clean(MAX_BODY_LENGTH)
            ?: return null
        val title = notificationTitle.clean(MAX_TITLE_LENGTH)
            ?: data["title"].clean(MAX_TITLE_LENGTH)
            ?: DEFAULT_TITLE
        val type = data["type"]
            ?.trim()
            ?.takeIf(supportedTypes::contains)
            ?: DEFAULT_TYPE
        val actorUserId = data["actorUserId"].clean(MAX_ID_LENGTH)
        val snapId = data["snapId"].clean(MAX_ID_LENGTH)
        val eventId = data["eventId"].clean(MAX_ID_LENGTH)
        val roomId = data["roomId"].clean(MAX_ID_LENGTH)
        val messageId = data["messageId"].clean(MAX_ID_LENGTH)
        val senderUserId = data["senderUserId"].clean(MAX_ID_LENGTH)

        return FcmPayload(
            title = title,
            body = body,
            type = type,
            actorUserId = actorUserId,
            snapId = snapId,
            eventId = eventId,
            roomId = roomId,
            messageId = messageId,
            senderUserId = senderUserId,
        )
    }

    private fun String?.clean(maxLength: Int): String? = this
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.take(maxLength)

    private const val MAX_TITLE_LENGTH = 100
    private const val MAX_BODY_LENGTH = 500
    private const val MAX_ID_LENGTH = 128
}
