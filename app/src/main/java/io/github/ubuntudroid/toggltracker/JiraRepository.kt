package io.github.ubuntudroid.toggltracker

import io.github.ubuntudroid.toggltracker.network.jira.JiraService
import io.github.ubuntudroid.toggltracker.network.jira.model.Issue
import kotlinx.coroutines.Deferred
import javax.inject.Inject

class JiraRepository @Inject constructor(
        private val jiraService: JiraService
) {

    fun getIssue(issueId: String): Deferred<Issue> = jiraService.getIssue(issueId)

}