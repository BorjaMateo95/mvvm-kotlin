package com.borja.mvvm_kotlin.model

import androidx.room.Entity
import androidx.room.TypeConverters
import com.borja.mvvm_kotlin.db.GithubTypeConverters

@Entity(primaryKeys = ["query"])
@TypeConverters(GithubTypeConverters::class)
class RepoSearchResult (
    val query: String,
    val repoIds: List<Int>,
    val totalCount: Int,
    val next: Int?
)