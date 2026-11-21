# GameVault release build (R8).
# Retrofit, kotlinx.serialization, Room, Coil and Firebase ship their own keep rules;
# these only cover the app's own reflection-free serializable classes as a safety net.

# JSON models read from the RAWG API and the saved form state
-keep,includedescriptorclasses class com.jofielramirez.gamevault.data.remote.** { *; }
-keepclassmembers class com.jofielramirez.gamevault.ui.form.GameFormState { *; }

# Keep file and line numbers so Crashlytics stack traces are readable
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
