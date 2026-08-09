package com.aget.notesba

import android.app.Application
import com.aget.notesba.di.AppContainer

class NotesBaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)
    }
}