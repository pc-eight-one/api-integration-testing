package dev.codersbox.eng.lib.cli.ci

data class CiEnvironment(
    val name: String,
    val buildId: String?,
    val branch: String?,
    val commitSha: String?,
    val pullRequestNumber: String?,
    val buildUrl: String?,
    val jobName: String?,
    val isPullRequest: Boolean
)

object CiDetector {
    fun detect(): CiEnvironment {
        return when {
            // GitHub Actions
            System.getenv("GITHUB_ACTIONS") != null -> CiEnvironment(
                name = "GitHub Actions",
                buildId = System.getenv("GITHUB_RUN_ID"),
                branch = System.getenv("GITHUB_REF_NAME"),
                commitSha = System.getenv("GITHUB_SHA"),
                pullRequestNumber = System.getenv("GITHUB_REF")?.let {
                    if (it.startsWith("refs/pull/")) it.split("/")[2] else null
                },
                buildUrl = System.getenv("GITHUB_SERVER_URL")?.let { 
                    "$it/${System.getenv("GITHUB_REPOSITORY")}/actions/runs/${System.getenv("GITHUB_RUN_ID")}"
                },
                jobName = System.getenv("GITHUB_JOB"),
                isPullRequest = System.getenv("GITHUB_EVENT_NAME") == "pull_request"
            )
            
            // Jenkins
            System.getenv("JENKINS_URL") != null -> CiEnvironment(
                name = "Jenkins",
                buildId = System.getenv("BUILD_ID"),
                branch = System.getenv("GIT_BRANCH") ?: System.getenv("BRANCH_NAME"),
                commitSha = System.getenv("GIT_COMMIT"),
                pullRequestNumber = System.getenv("CHANGE_ID"),
                buildUrl = System.getenv("BUILD_URL"),
                jobName = System.getenv("JOB_NAME"),
                isPullRequest = System.getenv("CHANGE_ID") != null
            )
            
            // GitLab CI
            System.getenv("GITLAB_CI") != null -> CiEnvironment(
                name = "GitLab CI",
                buildId = System.getenv("CI_PIPELINE_ID"),
                branch = System.getenv("CI_COMMIT_REF_NAME"),
                commitSha = System.getenv("CI_COMMIT_SHA"),
                pullRequestNumber = System.getenv("CI_MERGE_REQUEST_IID"),
                buildUrl = System.getenv("CI_PIPELINE_URL"),
                jobName = System.getenv("CI_JOB_NAME"),
                isPullRequest = System.getenv("CI_MERGE_REQUEST_IID") != null
            )
            
            // CircleCI
            System.getenv("CIRCLECI") != null -> CiEnvironment(
                name = "CircleCI",
                buildId = System.getenv("CIRCLE_BUILD_NUM"),
                branch = System.getenv("CIRCLE_BRANCH"),
                commitSha = System.getenv("CIRCLE_SHA1"),
                pullRequestNumber = System.getenv("CIRCLE_PR_NUMBER"),
                buildUrl = System.getenv("CIRCLE_BUILD_URL"),
                jobName = System.getenv("CIRCLE_JOB"),
                isPullRequest = System.getenv("CIRCLE_PULL_REQUEST") != null
            )
            
            // Travis CI
            System.getenv("TRAVIS") != null -> CiEnvironment(
                name = "Travis CI",
                buildId = System.getenv("TRAVIS_BUILD_ID"),
                branch = System.getenv("TRAVIS_BRANCH"),
                commitSha = System.getenv("TRAVIS_COMMIT"),
                pullRequestNumber = System.getenv("TRAVIS_PULL_REQUEST")?.takeIf { it != "false" },
                buildUrl = System.getenv("TRAVIS_BUILD_WEB_URL"),
                jobName = System.getenv("TRAVIS_JOB_NAME"),
                isPullRequest = System.getenv("TRAVIS_PULL_REQUEST") != "false"
            )
            
            // Azure Pipelines
            System.getenv("TF_BUILD") != null -> CiEnvironment(
                name = "Azure Pipelines",
                buildId = System.getenv("BUILD_BUILDID"),
                branch = System.getenv("BUILD_SOURCEBRANCHNAME"),
                commitSha = System.getenv("BUILD_SOURCEVERSION"),
                pullRequestNumber = System.getenv("SYSTEM_PULLREQUEST_PULLREQUESTNUMBER"),
                buildUrl = System.getenv("SYSTEM_TEAMFOUNDATIONCOLLECTIONURI")?.let {
                    "$it${System.getenv("SYSTEM_TEAMPROJECT")}/_build/results?buildId=${System.getenv("BUILD_BUILDID")}"
                },
                jobName = System.getenv("AGENT_JOBNAME"),
                isPullRequest = System.getenv("BUILD_REASON") == "PullRequest"
            )
            
            // Default (local or unknown CI)
            else -> CiEnvironment(
                name = "Local",
                buildId = null,
                branch = null,
                commitSha = null,
                pullRequestNumber = null,
                buildUrl = null,
                jobName = null,
                isPullRequest = false
            )
        }
    }
}
