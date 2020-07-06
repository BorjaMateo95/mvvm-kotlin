package com.borja.mvvm_kotlin.api

import retrofit2.Response
import java.lang.NumberFormatException
import java.util.regex.Matcher
import java.util.regex.Pattern

sealed class ApiResponse<T> {
    companion object {
        fun <T> create(error: Throwable): ApiErrorResponse<T>{
            return ApiErrorResponse(error.message ?: "Error desconocido")
        }

        fun <T> create(response: Response<T>): ApiResponse<T> {
            return if (response.isSuccessful) {
                val body: T? = response.body()
                if (body == null || response.code() == 204) {
                    ApiEmtyResponse()
                } else{
                    ApiSuccessResponse(
                        body = body,
                        linksHeaders = response.headers()?.get("link")
                    )
                }
            }else {
                val msg = response.errorBody()?.string()
                val errorMsg = if (msg.isNullOrEmpty()) {
                    response.message()
                }else{
                    msg
                }

                ApiErrorResponse(errorMsg ?: "Error desconocido")
            }
        }
    }
}

class ApiEmtyResponse<T>: ApiResponse<T>()

data class ApiSuccessResponse<T> (
    val body: T,
    val links: Map<String, String>
) : ApiResponse<T>(){
    constructor(body: T, linksHeaders: String?): this(
        body = body,
        links = linksHeaders?.extractLinks()?: emptyMap()
    )

    val nextPage: Int? by lazy (LazyThreadSafetyMode.NONE) {
        links[NEXT_LINK]?.let {
            next->
            val matcher = PAGE_PATTERN.matcher(next)
            if (!matcher.find() || matcher.groupCount() != 1) {
                null
            } else {
                try {
                    Integer.parseInt(matcher.group(1))
                }catch (ex: NumberFormatException){
                    null
                }
            }
        }
    }

    companion object{
        private val LINK_PATTERN = Pattern.compile("<([^>]*)>[\\s]*;[\\s]*rel=\"([a-zA-Z0-9]+)\"")
        private val PAGE_PATTERN = Pattern.compile("\\bpage=(\\d+)")
        private const val NEXT_LINK = "next"

        private fun String.extractLinks(): Map<String, String>{
            val links: MutableMap<String, String> = mutableMapOf<String, String>()
            val matcher: Matcher = LINK_PATTERN.matcher(this)

            while (matcher.find()) {
                val count = matcher.groupCount()
                if (count == 2) {
                    links[matcher.group(2)] = matcher.group(1)
                }
            }

            return links
        }
    }
}

data class ApiErrorResponse<T>(
    val errorMessage: String
): ApiResponse<T>()