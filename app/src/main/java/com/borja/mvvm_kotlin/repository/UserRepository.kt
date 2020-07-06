package com.borja.mvvm_kotlin.repository

import androidx.lifecycle.LiveData
import com.borja.mvvm_kotlin.AppExecutors
import com.borja.mvvm_kotlin.api.ApiResponse
import com.borja.mvvm_kotlin.api.GithubApi
import com.borja.mvvm_kotlin.db.UserDao
import com.borja.mvvm_kotlin.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val userDao: UserDao,
    private val githubApi: GithubApi
){

    fun loadUser(login: String) : LiveData<Resource<User>> {
        return object : NetworkBoundResource<User, User>(appExecutors) {
            override fun saveCallResult(item: User) {
                userDao.insert(item)
            }

            override fun shouldFetch(data: User?): Boolean {
                return data == null
            }

            override fun loadFromDB(): LiveData<User> {
                return userDao.findByLogin(login)
            }

            override fun createCall(): LiveData<ApiResponse<User>> {
                return githubApi.getUser(login)
            }

        }.asLiveData()
    }
}