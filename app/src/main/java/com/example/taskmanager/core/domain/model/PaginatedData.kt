package com.example.taskmanager.core.domain.model

data class PaginatedData<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean
)