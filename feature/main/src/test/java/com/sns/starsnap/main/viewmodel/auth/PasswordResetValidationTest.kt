package com.sns.starsnap.main.viewmodel.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordResetValidationTest {
    @Test
    fun `requires valid email six digit code and matching strong passwords`() {
        assertEquals("올바른 이메일 주소를 입력해 주세요.", passwordResetEmailError("invalid"))
        assertNull(passwordResetEmailError("user@example.com"))

        assertEquals("인증번호 6자리를 입력해 주세요.", passwordResetCodeError("12a456"))
        assertEquals("인증번호 6자리를 입력해 주세요.", passwordResetCodeError("١٢٣٤٥٦"))
        assertNull(passwordResetCodeError("123456"))

        assertEquals(
            "비밀번호가 일치하지 않습니다.",
            passwordResetPasswordError("Strong1!", "Strong2!"),
        )
        val overBcryptLimit = "Strong1!" + "가".repeat(22)
        assertEquals(
            "비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.",
            passwordResetPasswordError(overBcryptLimit, overBcryptLimit),
        )
        assertNull(passwordResetPasswordError("Strong1!", "Strong1!"))
    }

    @Test
    fun `mismatched password never runs confirmation and consumed challenge restarts`() {
        var confirmations = 0
        val error = runPasswordResetConfirmation("Strong1!", "Strong2!") {
            confirmations++
        }

        assertEquals("비밀번호가 일치하지 않습니다.", error)
        assertEquals(0, confirmations)

        val restarted = PasswordResetUiState(
            code = "123456",
            newPassword = "Strong1!",
            confirmPassword = "Strong1!",
            step = PasswordResetStep.PASSWORD,
            resendSecondsRemaining = 42,
        ).restartAtEmail()
        assertEquals(PasswordResetStep.EMAIL, restarted.step)
        assertEquals("", restarted.code)
        assertEquals("", restarted.newPassword)
        assertEquals(0, restarted.resendSecondsRemaining)
    }

    @Test
    fun `retry after accepts only bounded integer delay for rate limits`() {
        assertEquals(17L, passwordResetRetryAfterSeconds(429, " 17 "))
        assertEquals(0L, passwordResetRetryAfterSeconds(429, "0"))
        assertNull(passwordResetRetryAfterSeconds(429, "Wed, 21 Oct 2015 07:28:00 GMT"))
        assertNull(passwordResetRetryAfterSeconds(429, "-1"))
        assertNull(passwordResetRetryAfterSeconds(503, "17"))
        assertEquals(
            "17초 후에 다시 요청해 주세요.",
            PasswordResetUiState(resendSecondsRemaining = 17)
                .passwordResetRequestCooldownMessage(),
        )
        assertNull(PasswordResetUiState().passwordResetRequestCooldownMessage())
    }
}
