package com.example.taskboard

import com.example.taskboard.data.ApiClient
import com.example.taskboard.data.parseErrorBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ApiMappingTest {

    @Test fun enums_roundTripThroughApiStrings() {
        Category.values().forEach { assertEquals(it, it.apiValue.toCategory()) }
        Priority.values().forEach { assertEquals(it, it.apiValue.toPriority()) }
        TaskStatus.values().forEach { assertEquals(it, it.apiValue.toStatus()) }
    }

    @Test fun apiStrings_matchTheBackendContract() {
        assertEquals("in_progress", TaskStatus.IN_PROGRESS.apiValue)
        assertEquals("cicd", Category.CICD.apiValue)
        assertEquals("high", Priority.HIGH.apiValue)
    }

    @Test fun unknownValues_fallBackToDefaults() {
        assertEquals(Category.OTHER, "bogus".toCategory())
        assertEquals(TaskStatus.BACKLOG, "bogus".toStatus())
        assertEquals(Priority.MEDIUM, "bogus".toPriority())
    }

    @Test fun status_cyclesThroughThePipeline() {
        assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.BACKLOG.next())
        assertEquals(TaskStatus.DONE, TaskStatus.IN_PROGRESS.next())
        assertEquals(TaskStatus.BACKLOG, TaskStatus.DONE.next())
    }

    private fun task(due: String?, status: String) =
        TaskDto(1, 1, "t", null, "aws", "high", status, due, null)

    @Test fun overdue_onlyWhenPastDueAndNotDone() {
        val today = LocalDate.of(2026, 10, 22)
        assertTrue(task("2026-10-01", "backlog").isOverdue(today))
        assertFalse(task("2026-10-01", "done").isOverdue(today))
        assertFalse(task("2026-12-01", "in_progress").isOverdue(today))
        assertFalse(task(null, "backlog").isOverdue(today))
    }

    @Test fun errorBody_plainDetail() {
        assertEquals("Email is already registered", parseErrorBody(409, """{"detail":"Email is already registered"}"""))
    }

    @Test fun errorBody_validationList() {
        val body = """{"detail":[{"loc":["body","email"],"msg":"String should match pattern"}]}"""
        assertEquals("email: String should match pattern", parseErrorBody(422, body))
    }

    @Test fun errorBody_fallbacks() {
        assertEquals("Not found", parseErrorBody(404, null))
        assertEquals("Request failed (500)", parseErrorBody(500, "<html>oops</html>"))
    }

    @Test fun baseUrl_isNormalized() {
        assertEquals("http://10.0.2.2:8000/", ApiClient.normalizeBaseUrl("10.0.2.2:8000"))
        assertEquals("http://192.168.1.5:8000/", ApiClient.normalizeBaseUrl(" http://192.168.1.5:8000 "))
        assertEquals("https://api.example.com/", ApiClient.normalizeBaseUrl("https://api.example.com/"))
    }
}
