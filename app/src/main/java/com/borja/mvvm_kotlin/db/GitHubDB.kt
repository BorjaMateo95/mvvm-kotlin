package com.borja.mvvm_kotlin.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.borja.mvvm_kotlin.model.Contributor
import com.borja.mvvm_kotlin.model.Repo
import com.borja.mvvm_kotlin.model.RepoSearchResult
import com.borja.mvvm_kotlin.model.User

@Database(
    entities = [
        User::class,
        Repo::class,
        Contributor::class,
        RepoSearchResult::class
    ],
    version = 1
)
abstract class GitHubDB : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun repoDao(): RepoDao
}