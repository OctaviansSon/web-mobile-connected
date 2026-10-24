package com.example.taskboard.data

import com.google.gson.JsonParser

/** Result of a network call: either a value or a human readable error. */
sealed interface AppResult<out T> {
    data class Ok<T>(val value: T) : AppResult<T>
    data class Err(val message: String, val code: Int = 0) : AppResult<Nothing>
}

inline fun <T> AppResult<T>.onOk(block: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Ok) block(value)
    return this
}

inline fun <T> AppResult<T>.onErr(block: (AppResult.Err) -> Unit): AppResult<T> {
    if (this is AppResult.Err) block(this)
    return this
}

/**
 * FastAPI errors look like {"detail": "text"} or {"detail": [{"loc": [...], "msg": "..."}]}.
 * Returns a short message for the user.
 */
fun parseErrorBody(code: Int, body: String?): String {
    if (!body.isNullOrBlank()) {
        val parsed = runCatching {
            val detail = JsonParser.parseString(body).asJsonObject.get("detail")
            when {
                detail == null || detail.isJsonNull -> null
                detail.isJsonPrimitive -> detail.asString
                detail.isJsonArray -> detail.asJsonArray.joinToString("; ") { element ->
                    val obj = element.asJsonObject
                    val field = obj.getAsJsonArray("loc")?.lastOrNull()?.asString ?: ""
                    val msg = obj.get("msg")?.asString ?: ""
                    if (field.isEmpty()) msg else "$field: $msg"
                }
                else -> null
            }
        }.getOrNull()
        if (!parsed.isNullOrBlank()) return parsed
    }
    return when (code) {
        401 -> "Not authorized"
        403 -> "You don't have access to this"
        404 -> "Not found"
        else -> "Request failed ($code)"
    }
}
