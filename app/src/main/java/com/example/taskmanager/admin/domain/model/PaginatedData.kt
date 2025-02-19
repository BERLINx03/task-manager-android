package com.example.taskmanager.admin.domain.model

data class PaginatedData<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean
)