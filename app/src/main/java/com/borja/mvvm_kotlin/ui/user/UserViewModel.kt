package com.borja.mvvm_kotlin.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.borja.mvvm_kotlin.model.Repo
import com.borja.mvvm_kotlin.model.User
import com.borja.mvvm_kotlin.repository.RepoRepository
import com.borja.mvvm_kotlin.repository.Resource
import com.borja.mvvm_kotlin.repository.UserRepository
import com.borja.mvvm_kotlin.utils.AbsentLiveData
import javax.inject.Inject

class UserViewModel
@Inject constructor(userRepository: UserRepository, repoRepository: RepoRepository): ViewModel() {
    private val _login = MutableLiveData<String>()
    val login : LiveData<String> get() = _login

    val repositories: LiveData<Resource<List<Repo>>> = Transformations.switchMap(_login) {login->
        if (login==null) {
            AbsentLiveData.create()
        }else{
            repoRepository.loadRepos(login)
        }

    }

    val user: LiveData<Resource<User>> = Transformations.switchMap(_login){ login->
        if (login == null) {
            AbsentLiveData.create()
        }else{
            userRepository.loadUser(login)
        }
    }

    fun setLogin(login: String?) {
        if(_login.value != login) {
            _login.value = login
        }
    }

    fun retry() {
        _login.value.let { _login.value = it }
    }


}