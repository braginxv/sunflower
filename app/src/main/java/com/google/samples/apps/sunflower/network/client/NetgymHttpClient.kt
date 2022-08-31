package com.google.samples.apps.sunflower.network.client

import android.util.Log
import org.techlook.Either
import org.techlook.http.PipeliningConnection
import org.techlook.http.adapters.Response
import org.techlook.http.adapters.SimpleHttpClient
import org.techlook.http.adapters.StringResponse
import kotlin.coroutines.suspendCoroutine

typealias NamedArguments = Set<Pair<String, String>>

class NetgymHttpClient(
    val baseUrl: String,
    userAgent: String = AGENT_CLIENT,
    connectionType: SimpleHttpClient.ConnectionType = SimpleHttpClient.ConnectionType.Single,
    basicHeaders: NamedArguments = emptySet(),
    pipelineSendingInterval: Long = PipeliningConnection.DEFAULT_SENDING_INTERVAL
) {
    private val client: SimpleHttpClient = run {
        val httpClient = SimpleHttpClient(baseUrl)
            .addHeader(AGENT_HEADER, userAgent)
            .addHeaders(toTechlook(basicHeaders))

        if (connectionType == SimpleHttpClient.ConnectionType.Pipelining) {
            httpClient.configurePipeliningConnection(pipelineSendingInterval)
        } else {
            httpClient.configureConnection(connectionType)
        }
    }

    suspend fun GET(
        resource: String, additionalHeaders: NamedArguments = emptySet(),
        parameters: NamedArguments = emptySet()
    ): StringResponse {
        return suspendCoroutine { continuation ->
            client.asyncGET(resource, toTechlook(parameters), toTechlook(additionalHeaders)) { response ->
                continuation.resumeWith(responseToCoroutineResult(response))
            }
        }
    }

    suspend fun rawGET(
        resource: String, additionalHeaders: NamedArguments = emptySet(),
        parameters: NamedArguments = emptySet()
    ): Response {
        return suspendCoroutine { continuation ->
            client.asyncRawGET(resource, toTechlook(parameters), toTechlook(additionalHeaders)) { response ->
                continuation.resumeWith(responseToCoroutineResult(response))
            }
        }
    }

    companion object {
        const val AGENT_HEADER: String = "User-Agent"
        const val AGENT_CLIENT: String = "Netgym network library (https://github.com/braginxv/netgym)"

        fun <L, R> responseToCoroutineResult(response: Either<L, R>): Result<R> {
            var result: Result<R> = Result.failure(NetworkClientException("result is unset"))

            response.right().apply {
                Log.d("Success HTTP response", "")
                result = Result.success(it)
            }
            response.left().apply {
                Log.e("Wrong HTTP response", it.toString())
                result = Result.failure(NetworkClientException(it.toString()))
            }

            return result
        }

        private fun <K, V> toTechlook(keyValueSet: Set<Pair<K, V>>):
                Set<org.techlook.http.Pair<K, V>> = keyValueSet.map { (key, value) ->
            org.techlook.http.Pair(key, value)
        }.toSet()
    }
}

class NetworkClientException(message: String) : Exception(message)
