package com.jarves.mh.runtime

import com.jarves.mh.model.ProviderKind
import com.jarves.mh.model.ProviderProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeLlmApiLaunchConfigTest {
    @Test
    fun detectsSupportedLocalFreeLlmApiPortsOnly() {
        assertTrue(isFreeLlmApiEndpoint("http://127.0.0.1:3001"))
        assertTrue(isFreeLlmApiEndpoint("http://localhost:31415"))
        assertTrue(isFreeLlmApiEndpoint("http://192.168.1.8:3001"))
        assertFalse(isFreeLlmApiEndpoint("https://api.example.com"))
        assertFalse(isFreeLlmApiEndpoint("http://127.0.0.1:8080"))
    }

    @Test
    fun freeLlmApiRequiresItsUnifiedKeyButOtherCustomServersKeepPlaceholderSupport() {
        val freeLlmApi = ProviderProfile(
            kind = ProviderKind.CUSTOM,
            baseUrl = "http://127.0.0.1:3001",
        )
        val customServer = ProviderProfile(
            kind = ProviderKind.CUSTOM,
            baseUrl = "http://127.0.0.1:8080",
        )

        assertEquals("", localServerPlaceholderKey(freeLlmApi))
        assertEquals("no-key-required", localServerPlaceholderKey(customServer))
    }

    @Test
    fun routesFreeLlmApiBackgroundWorkToCheaperClaudeAlias() {
        val profile = ProviderProfile(
            kind = ProviderKind.CUSTOM,
            baseUrl = "http://127.0.0.1:3001",
            model = "claude-opus-4-5",
        )

        val launch = RuntimeLaunchConfigBuilder.build(profile, authToken = "freellmapi-test-key")
        val env = launch.environment

        assertEquals("claude-opus-4-5", env["ANTHROPIC_MODEL"])
        assertEquals("claude-opus-4-5", env["ANTHROPIC_DEFAULT_OPUS_MODEL"])
        assertEquals("claude-sonnet-4-5", env["ANTHROPIC_DEFAULT_SONNET_MODEL"])
        assertEquals("claude-haiku-4-5", env["ANTHROPIC_DEFAULT_HAIKU_MODEL"])
        assertEquals("claude-haiku-4-5", env["ANTHROPIC_SMALL_MODEL"])
        assertEquals("claude-haiku-4-5", env["ANTHROPIC_FAST_MODEL"])
        assertEquals("claude-haiku-4-5", env["CLAUDE_CODE_SUBAGENT_MODEL"])
        assertTrue(env["ANTHROPIC_CUSTOM_HEADERS"].orEmpty().contains("X-FreeLLM-Compress: standard"))
        assertTrue(env["ANTHROPIC_CUSTOM_HEADERS"].orEmpty().contains("X-FreeLLM-Task-Type: code"))
    }

    @Test
    fun ordinaryCustomGatewayKeepsItsSelectedModelForAllAliases() {
        val profile = ProviderProfile(
            kind = ProviderKind.CUSTOM,
            baseUrl = "https://gateway.example.com",
            model = "my-custom-model",
        )

        val env = RuntimeLaunchConfigBuilder.build(profile, authToken = "test-key").environment

        assertEquals("my-custom-model", env["ANTHROPIC_MODEL"])
        assertEquals("my-custom-model", env["ANTHROPIC_DEFAULT_HAIKU_MODEL"])
        assertEquals("my-custom-model", env["CLAUDE_CODE_SUBAGENT_MODEL"])
        assertFalse(env.containsKey("ANTHROPIC_CUSTOM_HEADERS"))
    }
}
