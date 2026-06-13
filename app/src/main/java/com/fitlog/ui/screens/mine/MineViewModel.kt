package com.fitlog.ui.screens.mine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlog.data.local.entity.UserProfile
import com.fitlog.data.prefs.BodyData
import com.fitlog.data.prefs.UserPreferencesRepository
import com.fitlog.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MineViewModel(
    private val userRepository: UserRepository,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    val users: StateFlow<List<UserProfile>> = userRepository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentUserId: StateFlow<Long?> = userRepository.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val unit: StateFlow<String> = preferences.unit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "kg")

    val restSeconds: StateFlow<Int> = preferences.restSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 90)

    val bodyData: StateFlow<BodyData?> = userRepository.currentUserId
        .flatMapLatest { uid -> if (uid == null) flowOf(null) else preferences.bodyData(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setUnit(u: String) = viewModelScope.launch { preferences.setUnit(u) }

    fun setRestSeconds(s: Int) = viewModelScope.launch { preferences.setRestSeconds(s) }

    fun saveBodyData(heightCm: Double?, age: Int?, gender: String?) = viewModelScope.launch {
        val uid = currentUserId.value ?: return@launch
        preferences.setBodyData(uid, heightCm, age, gender)
    }

    fun deleteCurrentUser() = viewModelScope.launch {
        val uid = currentUserId.value ?: return@launch
        users.value.firstOrNull { it.id == uid }?.let { userRepository.deleteUser(it) }
    }
}
