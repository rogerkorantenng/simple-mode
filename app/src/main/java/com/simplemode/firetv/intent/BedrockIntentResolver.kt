package com.simplemode.firetv.intent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Calls the Bedrock proxy described in `server/bedrock_proxy.py`, which is
 * the actual intelligence behind "Tell Me": real Bedrock Converse calls to
 * `us.anthropic.claude-sonnet-4-5-20250929-v1:0`, verified working on this
 * machine's account. The proxy exists because an Android app should never
 * embed AWS secret keys; see the proxy's own doc comment for the full
 * reasoning.
 *
 * `10.0.2.2` is the standard Android-emulator alias for the host machine's
 * loopback interface; on a real Fire TV Stick this would point at whatever
 * host actually runs the proxy on the household's network, which is a real
 * limitation this build does not solve -- see SPEC.md.
 */
/** The model named a tile that was not among the ones it was given. */
class UnknownTileIdException(val tileId: String) :
    IllegalStateException("Model returned tile id '$tileId', which was not in the list sent to it")

class BedrockIntentResolver(
    private val baseUrl: String = "http://10.0.2.2:8798",
    private val timeoutMillis: Long = 15_000,
) : IntentResolver {

    override suspend fun resolve(utterance: String, tiles: List<TileSummary>): IntentMatch =
        withTimeout(timeoutMillis) {
            withContext(Dispatchers.IO) {
                val response = postResolveIntent(utterance, tiles)
                val tileId = response.optString("tileId", null.toString()).takeUnless { it == "null" || it.isBlank() }
                interpret(tileId, response.optString("reply", ""), tiles)
            }
        }

    private fun postResolveIntent(utterance: String, tiles: List<TileSummary>): JSONObject {
        val url = URL("$baseUrl/resolve-intent")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = timeoutMillis.toInt()
            readTimeout = timeoutMillis.toInt()
            setRequestProperty("Content-Type", "application/json")
        }
        connection.outputStream.use { stream ->
            OutputStreamWriter(stream).use { it.write(requestBody(utterance, tiles).toString()) }
        }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        connection.disconnect()
        if (status !in 200..299) error("Bedrock proxy returned $status: $body")
        return JSONObject(body)
    }

    companion object {
        /**
         * Turns the proxy's answer into an [IntentMatch], rejecting a tile
         * id that was not among the ones sent.
         *
         * "Never invent a tile id" was in the prompt and nowhere else. A
         * prompt is an instruction, not an enforcement. For this viewer a
         * hallucinated id is not cosmetic: she cannot work out why a button
         * did nothing, and she cannot find her way back from a screen she
         * did not ask for. Throwing rather than returning NoMatch is
         * deliberate -- [FallbackIntentResolver] catches it and gives the
         * offline keyword matcher a turn, which is a better answer than
         * giving up, and if that finds nothing either the screen says so
         * in words.
         *
         * Pure, and separate from the HTTP call, so the hallucination case
         * is directly testable without a proxy running.
         */
        fun interpret(tileId: String?, reply: String, tiles: List<TileSummary>): IntentMatch = when {
            tileId != null && tiles.none { it.id == tileId } -> throw UnknownTileIdException(tileId)
            tileId != null -> IntentMatch.Matched(tileId, reply, IntentSource.BEDROCK)
            else -> IntentMatch.NoMatch(
                reply.ifBlank { "I'm not sure. Try one of the buttons below." },
                IntentSource.BEDROCK,
            )
        }
    }

    private fun requestBody(utterance: String, tiles: List<TileSummary>): JSONObject = JSONObject().apply {
        put("utterance", utterance)
        put(
            "tiles",
            JSONArray().apply {
                tiles.forEach { tile ->
                    put(
                        JSONObject()
                            .put("id", tile.id)
                            .put("title", tile.title)
                            .put("shortDescription", tile.shortDescription),
                    )
                }
            },
        )
    }
}
