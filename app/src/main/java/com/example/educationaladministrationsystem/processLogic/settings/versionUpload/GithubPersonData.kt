package com.example.educationaladministrationsystem.processLogic.settings.versionUpload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubPersonData(
    val login:String="",
    val id:String="",
    @SerialName("avatar_url")  val avatarUrl:String="",
    @SerialName("gravatar_id")  val gravatarId:String="",
    val url:String="",
    @SerialName("html_url")  val htmlUrl:String="",
    @SerialName("followers_url")  val followersUrl:String="",
    @SerialName("following_url")  val followingUrl:String="",
    @SerialName("gists_url")  val gistsUrl:String="",
    @SerialName("starred_url")  val starredUrl:String="",
    @SerialName("subscriptions_url")  val subscriptionsUrl:String="",
    @SerialName("organizations_url")  val organizationsUrl:String="",
    @SerialName("repos_url")  val reposUrl:String="",
    @SerialName("events_url")  val eventsUrl:String="",
    @SerialName("received_events_url")  val receivedEventsUrl:String="",
    @SerialName("type")  val type:String="",
    @SerialName("user_view_type") val userViewType:String="",
    @SerialName("site_admin")  val siteAdmin:Boolean=false,
    val name:String="",
    @SerialName("company")  val company:String?=null,
    @SerialName("blog")  val blog:String="",
    @SerialName("location")  val location:String?=null,
    @SerialName("email")  val email:String?=null,
    @SerialName("hireable")  val hireable:String?=null,
    @SerialName("bio")  val bio:String?=null,
    @SerialName("twitter_username")  val twitterUsername:String?=null,
    @SerialName("public_repos")  val publicRepos:Int=0,
    @SerialName("public_gists")  val publicGists:Int=0,
    @SerialName("followers")  val followers:Int=0,
    @SerialName("following")  val following:Int=0,
    @SerialName("created_at")  val createdAt:String="",
    @SerialName("updated_at")  val updatedAt:String="",

)
