package io.github.ubuntudroid.toggltracker.network.jira.model

import com.google.gson.annotations.SerializedName


data class Issue(
        @SerializedName("expand") val expand: String,
        @SerializedName("id") val id: String,
        @SerializedName("self") val self: String,
        @SerializedName("key") val key: String,
        @SerializedName("fields") val fields: Fields
)

data class Fields(
        @SerializedName("workratio") val workratio: Int, // -1 when not set
        @SerializedName("aggregatetimeoriginalestimate") val aggregatetimeoriginalestimate: Int?, // in seconds
        @SerializedName("aggregatetimespent") val aggregatetimespent: Int? // in seconds
)