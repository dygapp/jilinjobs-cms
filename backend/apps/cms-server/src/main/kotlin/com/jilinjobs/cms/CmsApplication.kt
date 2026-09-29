package com.jilinjobs.cms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(excludeName = ["org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration"])
class CmsApplication

fun main(args: Array<String>) {
    runApplication<CmsApplication>(*args)
}
