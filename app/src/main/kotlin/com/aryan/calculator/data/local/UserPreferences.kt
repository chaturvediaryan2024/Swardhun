package com.aryan.calculator.data.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val name: String = "",
    val photoUri: String = ""
)

class UserPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("swardhun_prefs", Context.MODE_PRIVATE)

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private fun loadProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString("user_name", "") ?: "",
            photoUri = prefs.getString("user_photo", "") ?: ""
        )
    }

    fun setName(name: String) {
        prefs.edit().putString("user_name", name).apply()
        _profile.value = _profile.value.copy(name = name)
    }

    fun setPhotoUri(uri: String) {
        prefs.edit().putString("user_photo", uri).apply()
        _profile.value = _profile.value.copy(photoUri = uri)
    }

    fun getSelectedLanguages(): Set<String> {
        return prefs.getStringSet("languages", setOf("hindi", "english")) ?: setOf("hindi", "english")
    }

    fun setSelectedLanguages(languages: Set<String>) {
        prefs.edit().putStringSet("languages", languages).apply()
    }

    // Audio quality: "high" (320kbps) or "normal" (160kbps)
    private val _audioQuality = MutableStateFlow(prefs.getString("audio_quality", "high") ?: "high")
    val audioQuality: StateFlow<String> = _audioQuality.asStateFlow()

    fun setAudioQuality(quality: String) {
        prefs.edit().putString("audio_quality", quality).apply()
        _audioQuality.value = quality
    }
}
