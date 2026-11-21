package com.jofielramirez.gamevault

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

/** Error tracking. The app only talks to this interface, so tests and builds without Firebase still work. */
interface CrashReporter {
    /** False when the build has no Firebase configuration (no google-services.json). */
    val isAvailable: Boolean
    fun setEnabled(enabled: Boolean)

    /** Records a handled error (for example a failed search) without crashing the app. */
    fun recordError(error: Throwable)

    object None : CrashReporter {
        override val isAvailable = false
        override fun setEnabled(enabled: Boolean) = Unit
        override fun recordError(error: Throwable) = Unit
    }
}

/** Firebase Crashlytics: crashes and recorded errors appear in the Firebase console. */
class FirebaseCrashReporter(private val context: Context) : CrashReporter {

    override val isAvailable: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()

    override fun setEnabled(enabled: Boolean) {
        if (isAvailable) FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
    }

    override fun recordError(error: Throwable) {
        if (isAvailable) FirebaseCrashlytics.getInstance().recordException(error)
    }
}
