package com.example.taskboard

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

// ---------- enums (names map to the lowercase strings used by the API) ----------
enum class Priority(val label: String) { LOW("Low"), MEDIUM("Medium"), HIGH("High") }

enum class TaskStatus(val label: String) {
    BACKLOG("Backlog"), IN_PROGRESS("In progress"), DONE("Done");

    fun next(): TaskStatus = values()[(ordinal + 1) % values().size]
}

enum class Category(val label: String) {
    LINUX("Linux"), NETWORKING("Networking"), DOCKER("Docker"), KUBERNETES("Kubernetes"),
    CICD("CI/CD"), AWS("AWS"), TERRAFORM("Terraform"), MONITORING("Monitoring"),
    DEVSECOPS("DevSecOps"), OTHER("Other")
}

val Priority.apiValue: String get() = name.lowercase()
val TaskStatus.apiValue: String get() = name.lowercase()
val Category.apiValue: String get() = name.lowercase()

fun String.toPriority(): Priority = Priority.values().firstOrNull { it.name.equals(this, ignoreCase = true) } ?: Priority.MEDIUM
fun String.toStatus(): TaskStatus = TaskStatus.values().firstOrNull { it.name.equals(this, ignoreCase = true) } ?: TaskStatus.BACKLOG
fun String.toCategory(): Category = Category.values().firstOrNull { it.name.equals(this, ignoreCase = true) } ?: Category.OTHER

// ---------- DTOs (JSON of the backend, see docs/API.md) ----------
data class UserDto(val id: Int, val username: String, val email: String)

data class TokenDto(@SerializedName("access_token") val accessToken: String)

data class RegisterBody(val username: String, val email: String, val password: String)
data class LoginBody(val email: String, val password: String)

data class TeamDto(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("members_count") val membersCount: Int,
    @SerializedName("my_role") val myRole: String
)

data class MemberDto(
    @SerializedName("user_id") val userId: Int,
    val username: String,
    val email: String,
    val role: String
)

data class TeamDetailDto(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("my_role") val myRole: String,
    val members: List<MemberDto>,
    @SerializedName("tasks_count") val tasksCount: Int
)

data class TeamBody(val name: String, val description: String)
data class MemberAddBody(val email: String)

data class TaskDto(
    val id: Int,
    @SerializedName("team_id") val teamId: Int,
    val title: String,
    val description: String?,
    val category: String,
    val priority: String,
    val status: String,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("assignee_id") val assigneeId: Int?
)

/** Body for create and update. Nulls are sent explicitly (Gson serializeNulls) so due date / assignee can be cleared. */
data class TaskBody(
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val status: String,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("assignee_id") val assigneeId: Int?
)

data class StatusBody(val status: String)

data class CertDto(
    val name: String,
    val done: Int,
    val total: Int,
    @SerializedName("progress_percent") val progressPercent: Double
)

data class MemberStatDto(
    @SerializedName("user_id") val userId: Int,
    val username: String,
    val assigned: Int,
    val done: Int
)

data class StatsDto(
    @SerializedName("total_tasks") val totalTasks: Int,
    @SerializedName("done_tasks") val doneTasks: Int,
    @SerializedName("progress_percent") val progressPercent: Double,
    @SerializedName("overdue_tasks") val overdueTasks: Int,
    @SerializedName("by_status") val byStatus: Map<String, Int>,
    @SerializedName("by_category") val byCategory: Map<String, Int>,
    val certifications: List<CertDto>,
    @SerializedName("by_member") val byMember: List<MemberStatDto>
)

/** True when the due date is in the past and the task is not done. */
fun TaskDto.isOverdue(today: LocalDate = LocalDate.now()): Boolean {
    val due = dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return false
    return due.isBefore(today) && status != TaskStatus.DONE.apiValue
}
