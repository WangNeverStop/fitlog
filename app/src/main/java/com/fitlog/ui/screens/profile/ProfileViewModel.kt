package com.fitlog.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitlog.data.local.entity.UserProfile
import com.fitlog.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs the profile-select screen: lists profiles, creates them, and sets the active
 * one. Demonstrates the data-isolation hook — switching users only changes which id
 * the rest of the app reads against.
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    val users: StateFlow<List<UserProfile>> = userRepository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentUserId: StateFlow<Long?> = userRepository.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun createUser(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { userRepository.createUser(name) }
    }

    fun selectUser(id: Long) {
        viewModelScope.launch { userRepository.selectUser(id) }
    }

    fun deleteUser(user: UserProfile) {
        viewModelScope.launch { userRepository.deleteUser(user) }
    }
}
