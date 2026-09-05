package com.jilinjobs.cms.common

import org.owasp.html.CssSchema
import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory

object RichTextHtmlPolicy {
    private val safeCss = CssSchema.withProperties(
        setOf(
            "font-size",
            "font-family",
            "color",
            "background-color",
            "text-align",
        ),
    )

    private val policy: PolicyFactory = HtmlPolicyBuilder()
        .allowElements(
            "p", "br", "div", "span", "hr",
            "h1", "h2", "h3", "h4", "h5", "h6",
            "strong", "b", "em", "i", "u", "s", "strike", "sub", "sup",
            "ul", "ol", "li", "blockquote", "pre", "code",
            "a", "img",
            "table", "caption", "thead", "tbody", "tfoot", "tr", "th", "td",
        )
        .allowAttributes("href", "title").onElements("a")
        .allowAttributes("src", "alt", "title").onElements("img")
        .allowAttributes("colspan", "rowspan").onElements("th", "td")
        .allowUrlProtocols("http", "https")
        .allowStyling(safeCss)
        .toFactory()

    fun sanitize(html: String): String = policy.sanitize(html)
}
