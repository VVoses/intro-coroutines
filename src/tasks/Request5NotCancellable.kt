package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsNotCancellable(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos: List<Repo> = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val deferreds = repos.map { repo ->
        async {
            log("starting loading for ${repo.name}")
            service
                .getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
        }
    }
    return@coroutineScope deferreds.awaitAll().flatten().aggregate()
}