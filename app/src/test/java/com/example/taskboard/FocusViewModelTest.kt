package com.example.taskboard

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusViewModelTest {
    @Test fun setSessionMinutes_updatesRemainingTime() {
        val vm = FocusViewModel()
        vm.setSessionMinutes(15)
        assertEquals(15, vm.state.value.sessionMinutes)
        assertEquals(15 * 60, vm.state.value.remainingSeconds)
    }

    @Test fun setTopic_changesTopic() {
        val vm = FocusViewModel()
        vm.setTopic(Category.TERRAFORM)
        assertEquals(Category.TERRAFORM, vm.state.value.topic)
    }

    @Test fun reset_restoresFullSession() {
        val vm = FocusViewModel()
        vm.setSessionMinutes(45)
        vm.reset()
        assertEquals(45 * 60, vm.state.value.remainingSeconds)
    }
}
