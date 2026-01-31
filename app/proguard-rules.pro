# ProGuard rules для LIVE-kotlin

# Сохраняем номера строк для отладки крашей
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============ Retrofit + Gson ============
# Сохраняем data классы для API
-keep class ru.liveap.app.data.model.** { *; }
-keep class ru.liveap.app.data.api.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ============ Room ============
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ============ Vosk (офлайн распознавание речи) ============
-keep class org.vosk.** { *; }
-keep class com.alphacephei.vosk.** { *; }
-dontwarn org.vosk.**

# ============ Firebase ============
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ============ ML Kit ============
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ============ CameraX ============
-keep class androidx.camera.** { *; }

# ============ Hilt ============
# Hilt генерирует свои правила автоматически через ksp

# ============ Coroutines ============
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============ Kotlin Serialization (на будущее) ============
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
