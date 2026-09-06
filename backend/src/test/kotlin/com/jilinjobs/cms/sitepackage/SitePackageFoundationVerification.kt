package com.jilinjobs.cms.sitepackage

import com.jilinjobs.cms.CmsApplication
import com.jilinjobs.cms.column.ColumnDraft
import com.jilinjobs.cms.column.ColumnService
import com.jilinjobs.cms.common.ContentImagePolicy
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import tools.jackson.databind.ObjectMapper
import java.nio.file.Path

fun main(args: Array<String>) {
    val packageRoot = Path.of(args.firstOrNull() ?: "src/test/resources/site-packages/foundation")
        .toAbsolutePath()
        .normalize()
    val springArgs = args.drop(1).toMutableList()
    if (springArgs.none { it.startsWith("--spring.flyway.target=") }) {
        springArgs += "--spring.flyway.target=1"
    }

    val context = SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(*springArgs.toTypedArray())
    try {
        val provisioner = context.getBean(SitePackageProvisioner::class.java)
        val columnService = context.getBean(ColumnService::class.java)

        val first = provisioner.apply(packageRoot)
        require(first.conflicts == 0 && first.inserted == 2 && first.updated == 0) {
            "Fresh schema 第一次 Site Package provision 结果不符合预期：$first"
        }

        val root = requireNotNull(columnService.findByAlias("foundation-root"))
        val child = requireNotNull(columnService.findByAlias("foundation-child"))
        require(root.preset && child.preset && child.parentId == root.id) {
            "Site Package Column preset / parent relationship 未正确恢复"
        }

        val operator = columnService.create(
            ColumnDraft(
                parentId = null,
                name = "Operator Fixture",
                sortOrder = 999,
                enabled = true,
                alias = "operator-fixture",
                coverPolicy = ContentImagePolicy.OPTIONAL,
            ),
        )
        require(!operator.preset) { "普通 Runtime Column 不应被 provision 为 preset" }

        val second = provisioner.apply(packageRoot)
        require(
            second.conflicts == 0 &&
                second.inserted == 0 &&
                second.updated == 0 &&
                second.unchanged == 2
        ) {
            "第二次 Site Package provision 必须幂等：$second"
        }

        val operatorAfter = requireNotNull(columnService.findByAlias("operator-fixture"))
        require(!operatorAfter.preset && operatorAfter.name == "Operator Fixture") {
            "Site Package apply 不得 broad-reset package 外 operator-created Runtime 数据"
        }

        val objectMapper = context.getBean(ObjectMapper::class.java)
        println(
            "EU37_SITE_PACKAGE_FOUNDATION " +
                objectMapper.writeValueAsString(
                    mapOf(
                        "first" to first,
                        "second" to second,
                        "operatorPreset" to operatorAfter.preset,
                    ),
                ),
        )
    } finally {
        context.close()
    }
}
