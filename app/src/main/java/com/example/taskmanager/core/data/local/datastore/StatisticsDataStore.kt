package com.example.taskmanager.core.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.taskmanager.core.utils.DataStoreConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
class StatisticsDataStore @Inject constructor(
    private val context: Context
){
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DataStoreConstants.STATISTICS_DATASTORE_KEY)
    private val adminCount = intPreferencesKey(DataStoreConstants.ADMIN_COUNT)
    private val departmentCount = intPreferencesKey(DataStoreConstants.DEPARTMENT_COUNT)
    private val taskCount = intPreferencesKey(DataStoreConstants.TASK_COUNT)
    private val managerCount = intPreferencesKey(DataStoreConstants.MANAGER_COUNT)
    private val employeeCount = intPreferencesKey(DataStoreConstants.EMPLOYEE_COUNT)

    val adminCountFlow = context.dataStore.data.map { preferences ->
        preferences[adminCount] ?: 0
    }

    val departmentCountFlow = context.dataStore.data.map { preferences ->
        preferences[departmentCount] ?: 0
    }

    val taskCountFlow = context.dataStore.data.map { preferences ->
        preferences[taskCount] ?: 0
    }

    val managerCountFlow = context.dataStore.data.map { preferences ->
        preferences[managerCount] ?: 0
    }

    val employeeCountFlow = context.dataStore.data.map { preferences ->
        preferences[employeeCount] ?: 0
    }

    suspend fun saveAdminCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[adminCount] = count
        }
    }

    suspend fun saveDepartmentCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[departmentCount] = count
        }
    }

    suspend fun saveTaskCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[taskCount] = count
        }
    }

    suspend fun saveManagerCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[managerCount] = count
        }
    }

    suspend fun saveEmployeeCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[employeeCount] = count
        }
    }

    suspend fun clear(){
        context.dataStore.edit {
            it.clear()
        }
    }
}