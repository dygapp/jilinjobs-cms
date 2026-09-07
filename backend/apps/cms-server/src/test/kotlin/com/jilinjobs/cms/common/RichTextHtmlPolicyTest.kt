package com.jilinjobs.cms.common

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RichTextHtmlPolicyTest {
    @Test
    fun `removes active content unsafe urls handlers and unsupported css`() {
        val sanitized = RichTextHtmlPolicy.sanitize(
            """
            <div onclick="alert(1)">
              <script>alert(1)</script>
              <style>body{display:none}</style>
              <a href="javascript:alert(1)" onmouseover="alert(2)">bad link</a>
              <img src="data:image/svg+xml,<svg onload=alert(1)></svg>" onerror="alert(3)">
              <span style="color:#333;position:fixed;background-image:url(javascript:alert(4))">safe text</span>
              <iframe src="https://example.com"></iframe>
              <object data="https://example.com"></object>
              <embed src="https://example.com">
              <form><input value="unsafe"></form>
              <svg><script>alert(5)</script></svg>
            </div>
            """.trimIndent(),
        )

        assertTrue(sanitized.contains("safe text"))
        assertTrue(sanitized.contains("bad link"))
        assertTrue(sanitized.contains("color"))
        assertFalse(sanitized.contains("<script"))
        assertFalse(sanitized.contains("<style"))
        assertFalse(sanitized.contains("onclick"))
        assertFalse(sanitized.contains("onmouseover"))
        assertFalse(sanitized.contains("onerror"))
        assertFalse(sanitized.contains("javascript:"))
        assertFalse(sanitized.contains("data:image"))
        assertFalse(sanitized.contains("position"))
        assertFalse(sanitized.contains("background-image"))
        assertFalse(sanitized.contains("<iframe"))
        assertFalse(sanitized.contains("<object"))
        assertFalse(sanitized.contains("<embed"))
        assertFalse(sanitized.contains("<form"))
        assertFalse(sanitized.contains("<input"))
        assertFalse(sanitized.contains("<svg"))
    }

    @Test
    fun `preserves safe authoring markup managed image urls and party legacy styles`() {
        val sanitized = RichTextHtmlPolicy.sanitize(
            """
            <h2>通知标题</h2>
            <p style="text-align:center"><span style="font-family:宋体;font-size:16px;color:#333333;background-color:#ffffff"><strong>党建历史正文</strong></span></p>
            <ul><li><em>列表内容</em></li></ul>
            <table><tbody><tr><td colspan="2">表格内容</td></tr></tbody></table>
            <p><a href="/page/about">站内链接</a></p>
            <p><a href="https://example.com/info">安全外链</a></p>
            <p><img src="/api/admin/resources/12/content" alt="正文图片"></p>
            """.trimIndent(),
        )

        assertTrue(sanitized.contains("通知标题"))
        assertTrue(sanitized.contains("党建历史正文"))
        assertTrue(sanitized.contains("<strong>"))
        assertTrue(sanitized.contains("font-family"))
        assertTrue(sanitized.contains("font-size"))
        assertTrue(sanitized.contains("color"))
        assertTrue(sanitized.contains("background-color"))
        assertTrue(sanitized.contains("text-align"))
        assertTrue(sanitized.contains("<table>"))
        assertTrue(sanitized.contains("colspan=\"2\""))
        assertTrue(sanitized.contains("href=\"/page/about\""))
        assertTrue(sanitized.contains("href=\"https://example.com/info\""))
        assertTrue(sanitized.contains("src=\"/api/admin/resources/12/content\""))
    }
}
