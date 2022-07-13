package com.google.samples.apps.sunflower.network.client

import org.techlook.ClientSystem
import org.techlook.Either
import org.techlook.SocketClient
import org.techlook.http.HttpConnection
import org.techlook.http.PipeliningConnection
import org.techlook.http.SequentialConnection
import org.techlook.http.SingleConnection
import org.techlook.http.adapters.*
import org.techlook.http.client.HttpListener
import java.lang.RuntimeException
import java.net.URL
import java.nio.charset.Charset
import kotlin.coroutines.suspendCoroutine

typealias NamedArguments = Set<Pair<String, String>>

class SimpleHttpClient(
    baseUrl: URL,
    connectionType: HttpRequestBuilder.ConnectionType = HttpRequestBuilder.ConnectionType.Persistent,
    basicHeaders: NamedArguments = emptySet(),
    userAgent: String? = null
) {
    private val basePath = baseUrl.path.takeIf { it.endsWith(SLASH) } ?: (baseUrl.path + SLASH)

    private val commonHeaders: Set<Pair<String, String>> = userAgent?.let {
        basicHeaders + Pair(AGENT_HEADER, it)
    } ?: basicHeaders

    private val client: SocketClient = when (baseUrl.protocol) {
        HTTP -> ClientSystem.client()
        HTTPS -> ClientSystem.sslClient()
        else -> throw RuntimeException("unsupported protocol: ${baseUrl.protocol}")
    }

    private val connection: HttpConnection = run {
        val port = if (baseUrl.port > 0) baseUrl.port else baseUrl.defaultPort

        when (connectionType) {
            HttpRequestBuilder.ConnectionType.Single -> SingleConnection(baseUrl.host, port, client)
            HttpRequestBuilder.ConnectionType.Persistent -> SequentialConnection(baseUrl.host, port, client)
            HttpRequestBuilder.ConnectionType.Pipelining -> PipeliningConnection(
                baseUrl.host, port, client,
                PIPELINING_SEND_INTERVAL
            )
        }
    }

    suspend fun GET(resource: String, additionalHeaders: NamedArguments = emptySet(),
                    parameters: NamedArguments = emptySet()): StringResponse {
        return suspendCoroutine { continuation ->
            connection.get(basePath + resource,
                commonHeaders.plus(additionalHeaders).map(Companion::toTechlookPair), parameters.map(Companion::toTechlookPair),
                object: StringResponseListener() {
                    override fun respondString(response: Either<String, StringResponse>) {
                        continuation.resumeWith(responseToCoroutineResult(response))
                    }
                }
            )
        }
    }

    suspend fun rawGET(resource: String, additionalHeaders: NamedArguments = emptySet(),
                    parameters: NamedArguments = emptySet()): Response {
        return suspendCoroutine { continuation ->
            connection.get(basePath + resource,
                commonHeaders.plus(additionalHeaders).map(Companion::toTechlookPair), parameters.map(Companion::toTechlookPair),
                object: ByteResponseListener() {
                    override fun respond(response: Either<String, Response>) {
                        continuation.resumeWith(responseToCoroutineResult(response))
                    }
                }
            )
        }
    }

    companion object {
        const val AGENT_HEADER: String = "User-Agent"
        const val PIPELINING_SEND_INTERVAL: Long = 5
        const val HTTP = "http"
        const val HTTPS = "https"
        const val SLASH = "/"

        fun <L, R> responseToCoroutineResult(response: Either<L, R>): Result<R> {
            var result: Result<R> = Result.failure(NetworkClientException("result is unset"))

            response.right().apply { result = Result.success(it) }
            response.left().apply {
                result = Result.failure(NetworkClientException(it.toString()))
            }

            return result
        }

        private fun <K, V> toTechlookPair(pair: Pair<K, V>): org.techlook.http.Pair<K, V> {
            val (key, value) = pair

            return org.techlook.http.Pair(key, value)
        }

    }
}

class NetworkClientException(message: String) : Exception(message)
