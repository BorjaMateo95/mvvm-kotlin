package com.borja.mvvm_kotlin.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.borja.mvvm_kotlin.AppExecutors
import com.borja.mvvm_kotlin.api.ApiResponse
import com.borja.mvvm_kotlin.api.ApiSuccessResponse
import com.borja.mvvm_kotlin.api.GithubApi
import com.borja.mvvm_kotlin.db.GitHubDB
import com.borja.mvvm_kotlin.db.RepoDao
import com.borja.mvvm_kotlin.model.Contributor
import com.borja.mvvm_kotlin.model.Repo
import com.borja.mvvm_kotlin.model.RepoSearchResponse
import com.borja.mvvm_kotlin.model.RepoSearchResult
import com.borja.mvvm_kotlin.utils.AbsentLiveData
import com.borja.mvvm_kotlin.utils.RateLimiter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepoRepository @Inject constructor(
    private val executors: AppExecutors,
    private val db: GitHubDB,
    private val repoDao: RepoDao,
    private val githubApi: GithubApi
){

    private val repoListRateLimiter = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun loadRepos(owner: String) : LiveData<Resource<List<Repo>>> {

        return object: NetworkBoundResource<List<Repo>, List<Repo>>(executors) {
            override fun saveCallResult(item: List<Repo>) {
                repoDao.insertRepos(item)
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return data == null || data.isEmpty() || repoListRateLimiter.shoulFetch(owner)
            }

            override fun loadFromDB(): LiveData<List<Repo>> {
                return repoDao.loadRepositories(owner)
            }

            override fun createCall(): LiveData<ApiResponse<List<Repo>>> {
                return githubApi.getRepos(owner)
            }

            override fun onFetchFailed() {
                repoListRateLimiter.reset(owner)
            }

        }.asLiveData()
    }

    fun loadRepo(owner: String, name: String): LiveData<Resource<Repo>> {

        return object : NetworkBoundResource<Repo, Repo>(executors) {
            override fun saveCallResult(item: Repo) {
                repoDao.insert(item)
            }

            override fun shouldFetch(data: Repo?): Boolean {
                return data == null
            }

            override fun loadFromDB(): LiveData<Repo> {
                return repoDao.load(owner, name)
            }

            override fun createCall(): LiveData<ApiResponse<Repo>> {
                return githubApi.getRepo(owner, name)
            }

        }.asLiveData()
    }

    fun loadContributors(owner: String, name: String): LiveData<Resource<List<Contributor>>> {
        return object : NetworkBoundResource<List<Contributor>, List<Contributor>>(executors) {
            override fun saveCallResult(item: List<Contributor>) {
                item.forEach {
                    it.repoName = name
                    it.repoOwner = owner
                }

                db.runInTransaction {
                    repoDao.createRepoIfNoExits(
                        Repo(id= Repo.UNKOWN_ID,
                        name = name,
                        fullName = "$owner/$name",
                        description = "",
                        owner = Repo.Owner(owner, null),
                        stars = 0)
                    )
                }

                repoDao.insertContributors(item)
            }

            override fun shouldFetch(data: List<Contributor>?): Boolean {
                return data == null || data.isEmpty()
            }

            override fun loadFromDB(): LiveData<List<Contributor>> {
               return repoDao.loadContributors(owner, name)
            }

            override fun createCall(): LiveData<ApiResponse<List<Contributor>>> {
                return githubApi.getContributors(owner, name)
            }

        }.asLiveData()
    }

    fun searchNextPage(query: String) : LiveData<Resource<Boolean>> {
        val fetchNextSearchPageTask = FetchNextSearchPageTask(
            query = query,
            githubApi = githubApi,
            db = db
        )
        executors.networkIO().execute(fetchNextSearchPageTask)
        return fetchNextSearchPageTask.liveData
    }


    fun search(query: String): LiveData<Resource<List<Repo>>> {
        return object : NetworkBoundResource<List<Repo>, RepoSearchResponse>(executors) {
            override fun saveCallResult(item: RepoSearchResponse) {
                val reposIds = item.items.map { it.id }
                val repoSearResult = RepoSearchResult(
                    query = query,
                    repoIds = reposIds,
                    totalCount = item.total,
                    next = item.nextPage
                )

                db.runInTransaction{
                    repoDao.insertRepos(item.items)
                    repoDao.insert(repoSearResult)
                }
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return data == null || data.isEmpty()
            }

            override fun loadFromDB(): LiveData<List<Repo>> {
                return Transformations.switchMap(repoDao.search(query)) {searchData->
                    if (searchData == null) {
                        AbsentLiveData.create()
                    } else {
                        repoDao.loadOrdered(searchData.repoIds)
                    }

                }
            }

            override fun createCall(): LiveData<ApiResponse<RepoSearchResponse>> {
               return githubApi.searchRepos(query)
            }

            override fun processResponse(response: ApiSuccessResponse<RepoSearchResponse>): RepoSearchResponse {
                val body = response.body
                body.nextPage = response.nextPage
                return body
            }

        }.asLiveData()
    }
}