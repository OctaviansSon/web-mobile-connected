package com.example.taskboard.data

import com.example.taskboard.*
import retrofit2.Response
import retrofit2.http.*

/** One function per backend endpoint (see docs/API.md of the backend). */
interface ApiService {
    // auth
    @POST("api/auth/register") suspend fun signUp(@Body body: RegisterBody): UserDto
    @POST("api/auth/login") suspend fun signIn(@Body body: LoginBody): TokenDto
    @GET("api/auth/me") suspend fun currentUser(): UserDto

    // teams
    @GET("api/teams") suspend fun listTeams(): List<TeamDto>
    @POST("api/teams") suspend fun newTeam(@Body body: TeamBody): TeamDto
    @GET("api/teams/{id}") suspend fun teamDetail(@Path("id") id: Int): TeamDetailDto
    @PUT("api/teams/{id}") suspend fun editTeam(@Path("id") id: Int, @Body body: TeamBody): TeamDto
    @DELETE("api/teams/{id}") suspend fun dropTeam(@Path("id") id: Int): Response<Unit>
    @POST("api/teams/{id}/members") suspend fun inviteMember(@Path("id") id: Int, @Body body: MemberAddBody): MemberDto
    @DELETE("api/teams/{id}/members/{userId}") suspend fun kickMember(@Path("id") id: Int, @Path("userId") userId: Int): Response<Unit>

    // tasks
    @GET("api/teams/{id}/tasks")
    suspend fun listTasks(
        @Path("id") teamId: Int,
        @Query("status") status: String? = null,
        @Query("category") category: String? = null,
        @Query("q") query: String? = null
    ): List<TaskDto>

    @POST("api/teams/{id}/tasks") suspend fun newTask(@Path("id") teamId: Int, @Body body: TaskBody): TaskDto
    @GET("api/tasks/{id}") suspend fun taskById(@Path("id") id: Int): TaskDto
    @PUT("api/tasks/{id}") suspend fun editTask(@Path("id") id: Int, @Body body: TaskBody): TaskDto
    @PATCH("api/tasks/{id}/status") suspend fun moveTask(@Path("id") id: Int, @Body body: StatusBody): TaskDto
    @DELETE("api/tasks/{id}") suspend fun dropTask(@Path("id") id: Int): Response<Unit>

    // stats
    @GET("api/teams/{id}/stats") suspend fun teamStats(@Path("id") id: Int): StatsDto
}
