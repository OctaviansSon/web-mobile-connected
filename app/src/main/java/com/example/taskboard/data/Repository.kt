package com.example.taskboard.data

import com.example.taskboard.*
import com.google.gson.JsonParseException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Single entry point to the backend: builds the Retrofit client and turns exceptions into AppResult. */
class Repository(private val session: SessionStore) {

    private var service: ApiService? = null
    private var serviceUrl: String = ""

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    private fun service(): ApiService {
        val url = session.baseUrl
        val current = service
        if (current != null && url == serviceUrl) return current
        val created = ApiClient.create(
            baseUrl = url,
            tokenProvider = { session.token },
            onUnauthorized = {
                session.clear()
                _sessionExpired.tryEmit(Unit)
            }
        )
        service = created
        serviceUrl = url
        return created
    }

    suspend fun <T> call(block: suspend ApiService.() -> T): AppResult<T> = try {
        AppResult.Ok(service().block())
    } catch (e: HttpException) {
        AppResult.Err(parseErrorBody(e.code(), e.response()?.errorBody()?.string()), e.code())
    } catch (e: IOException) {
        AppResult.Err("Cannot reach the server. Check the address and that the backend is running.")
    } catch (e: JsonParseException) {
        AppResult.Err("Unexpected response from the server")
    } catch (e: IllegalArgumentException) {
        AppResult.Err("Invalid server address")
    }

    /** For DELETE endpoints that answer 204 No Content. */
    suspend fun callEmpty(block: suspend ApiService.() -> Response<Unit>): AppResult<Unit> = call {
        val response = block()
        if (!response.isSuccessful) throw HttpException(response)
    }

    // ---- auth ----
    suspend fun login(baseUrl: String, email: String, password: String): AppResult<UserDto> {
        session.baseUrl = baseUrl.trim()
        val tokenResult = call { signIn(LoginBody(email.trim(), password)) }
        val accessToken = when (tokenResult) {
            is AppResult.Ok -> tokenResult.value.accessToken
            is AppResult.Err -> return tokenResult
        }
        session.token = accessToken
        val me = call { currentUser() }
        me.onOk { user ->
            session.username = user.username
            session.email = user.email
            session.userId = user.id
        }.onErr {
            session.clear()
        }
        return me
    }

    suspend fun register(baseUrl: String, username: String, email: String, password: String): AppResult<UserDto> {
        session.baseUrl = baseUrl.trim()
        val created = call { signUp(RegisterBody(username.trim(), email.trim(), password)) }
        if (created is AppResult.Err) return created
        return login(baseUrl, email, password)
    }
}
