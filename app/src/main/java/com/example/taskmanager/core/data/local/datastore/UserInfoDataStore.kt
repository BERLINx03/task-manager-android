package com.example.taskmanager.core.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.taskmanager.core.domain.model.User
import com.example.taskmanager.core.utils.DataStoreConstants.USER_INFO_KEY
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */

class UserInfoDataStore @Inject constructor(
    private val context: Context
) {
    private val Context.dataStore by preferencesDataStore("user_info")
    private val userInfoKey = stringPreferencesKey(USER_INFO_KEY)


    private val gson = Gson()

    val userInfoFlow: Flow<User> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val userJson = preferences[userInfoKey]
            if (userJson != null) {
                try {
                    gson.fromJson(userJson, User::class.java)
                } catch (e: Exception) {
                    User.Empty
                }
            } else {
                User.Empty
            }
        }

    suspend fun saveUserInfo(user: User) {
        context.dataStore.edit { preferences ->
            preferences[userInfoKey] = gson.toJson(user)
        }
    }

    suspend fun clearUserInfo() {
        context.dataStore.edit { preferences ->
            preferences.remove(userInfoKey)
        }
    }
}