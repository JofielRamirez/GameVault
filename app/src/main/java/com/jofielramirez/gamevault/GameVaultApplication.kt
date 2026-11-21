package com.jofielramirez.gamevault

import android.app.Application
import android.os.StrictMode
import androidx.annotation.VisibleForTesting

class GameVaultApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) enableStrictMode()
        container = AppContainer.create(this)
    }

    /** Lets integration tests swap in an in-memory database and a fake game catalog. */
    @VisibleForTesting
    fun replaceContainer(testContainer: AppContainer) {
        container = testContainer
    }

    /**
     * Debug builds only: reports in Logcat (tag "StrictMode") any disk or network access on the
     * main thread, plus leaked database cursors, closeable objects and activities.
     */
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build()
        )
    }
}
