package com.example.educationaladministrationsystem.processLogic.settings.versionUpload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    val url: String = "",
    @SerialName("assets_url")      val assetsUrl: String = "",
    @SerialName("upload_url")      val uploadUrl: String = "",
    @SerialName("html_url")        val htmlUrl: String = "",
    val id: Long = 0,
    val author: GitHubUser? = null,                 // 可能缺失 → 可空
    @SerialName("node_id")         val nodeId: String = "",
    @SerialName("tag_name")        val tagName: String = "",
    @SerialName("target_commitish")val targetCommitish: String = "",
    val name: String? = null,                       // GitHub 中可能为 null
    val draft: Boolean = false,
    val immutable: Boolean = false,                 // 新字段，老数据可能没有
    val prerelease: Boolean = false,
    @SerialName("created_at")      val createdAt: String = "",
    @SerialName("updated_at")      val updatedAt: String = "",
    @SerialName("published_at")    val publishedAt: String? = null,  // 草稿时为 null
    val assets: List<GitHubAsset> = emptyList(),
    @SerialName("tarball_url")     val tarballUrl: String = "",
    @SerialName("zipball_url")     val zipballUrl: String = "",
    val body: String = ""
)


@Serializable
data class GitHubAsset(
    val url: String = "",
    val id: Long = 0,
    @SerialName("node_id")          val nodeId: String = "",
    val name: String = "",
    val label: String? = null,                      // 示例中为 null
    val uploader: GitHubUser? = null,
    @SerialName("content_type")     val contentType: String = "",
    val state: String = "",
    val size: Long = 0,
    val digest: String? = null,                     // 新字段，可空
    @SerialName("download_count")   val downloadCount: Int = 0,
    @SerialName("created_at")       val createdAt: String = "",
    @SerialName("updated_at")       val updatedAt: String = "",
    @SerialName("browser_download_url") val browserDownloadUrl: String = ""
)


@Serializable
data class GitHubUser(
    val login: String = "",
    val id: Long = 0,
    @SerialName("node_id")             val nodeId: String = "",
    @SerialName("avatar_url")          val avatarUrl: String = "",
    @SerialName("gravatar_id")         val gravatarId: String = "",
    val url: String = "",
    @SerialName("html_url")            val htmlUrl: String = "",
    @SerialName("followers_url")       val followersUrl: String = "",
    @SerialName("following_url")       val followingUrl: String = "",
    @SerialName("gists_url")           val gistsUrl: String = "",
    @SerialName("starred_url")         val starredUrl: String = "",
    @SerialName("subscriptions_url")   val subscriptionsUrl: String = "",
    @SerialName("organizations_url")   val organizationsUrl: String = "",
    @SerialName("repos_url")           val reposUrl: String = "",
    @SerialName("events_url")          val eventsUrl: String = "",
    @SerialName("received_events_url") val receivedEventsUrl: String = "",
    val type: String = "",
    @SerialName("user_view_type")      val userViewType: String = "public",
    @SerialName("site_admin")          val siteAdmin: Boolean = false
)