package com.borja.mvvm_kotlin.di

import android.app.Application
import androidx.room.Room
import com.borja.mvvm_kotlin.api.GithubApi
import com.borja.mvvm_kotlin.db.GitHubDB
import com.borja.mvvm_kotlin.db.RepoDao
import com.borja.mvvm_kotlin.db.UserDao
import com.borja.mvvm_kotlin.utils.LiveDataCallAdapterFactory
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {

    @Singleton
    @Provides
    fun provideGithubApi(): GithubApi {
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build().create(GithubApi::class.java)
    }

    @Singleton
    @Provides
    fun provideDb(app: Application) : GitHubDB {
        return Room.databaseBuilder(app, GitHubDB::class.java, "github.db")
            .fallbackToDestructiveMigration()
            .build()
    }


    @Singleton
    @Provides
    fun providerUserDao(db: GitHubDB) : UserDao{
        return db.userDao()
    }


    @Singleton
    @Provides
    fun provideRepoDao(db: GitHubDB) : RepoDao {
        return db.repoDao()
    }
}