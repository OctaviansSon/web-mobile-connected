package com.example.taskboard

import android.app.Application
import com.example.taskboard.data.Repository
import com.example.taskboard.data.SessionStore

/** Application object = tiny dependency container (one session store and one repository for the whole app). */
class PlannerApp : Application() {
    lateinit var session: SessionStore
        private set
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        session = SessionStore(this)
        repository = Repository(session)
    }
}
