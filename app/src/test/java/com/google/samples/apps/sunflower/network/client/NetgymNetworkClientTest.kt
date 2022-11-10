package com.google.samples.apps.sunflower.network.client

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class NetgymNetworkClientTest {
    @Test
    fun testBaseUrlAssemblyWithSingleUrlParts() {
        val urls = listOf(
            "protocol://www.domain.org/common/path/first/sub/",
            "protocol://www.domain.org/common/path/first/sub/resource.txt",
            "protocol://www.domain.org/common/path/firstpath/sub/request?param1=value1&param2=value2",
            "protocol://www.domain.org/common/path/second/path/request?param1=value1&param2=value2"
        )

        assertThat(NetgymHttpClient.baseUrlFor(urls), `is`("protocol://www.domain.org/common/path/"))
    }

    @Test
    fun testBaseUrlAssemblyWithPartialUrlParts() {
        val urls = listOf(
            "protocol://www.domain.org/common/path/first/sub/",
            "protocol://www.domain.org/common/path/first/sub/resource.txt",
            "protocol://www.domain.org/common/path/firstpath/sub/request?param1=value1&param2=value2",
            "protocol://www.domain.org/common/path/firstpath/request?param1=value1&param2=value2"
        )

        assertThat(NetgymHttpClient.baseUrlFor(urls), `is`("protocol://www.domain.org/common/path/"))
    }
}
