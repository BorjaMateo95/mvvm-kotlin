package com.borja.mvvm_kotlin.model

import com.google.gson.annotations.SerializedName

class RepoSearchResponse (
    @SerializedName("total_count")
    val total: Int = 0,

    @SerializedName("items")
    val items: List<Repo>
) {
    var nextPage: Int? = null
}