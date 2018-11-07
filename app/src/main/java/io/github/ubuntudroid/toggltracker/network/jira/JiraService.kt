package io.github.ubuntudroid.toggltracker.network.jira

import io.github.ubuntudroid.toggltracker.network.jira.model.Issue
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * See https://developer.atlassian.com/cloud/jira/platform/rest/ for the API documentation.
 */
interface JiraService {

@GET("issue/{issueId}")
fun getIssue(
        @Path("issueId") issueId: String
): Deferred<Issue>

}