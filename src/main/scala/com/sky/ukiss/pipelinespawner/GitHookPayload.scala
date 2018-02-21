package com.sky.ukiss.pipelinespawner

case class GitHookPayload(user_name: String, repository: Repository, commits: List[Commit], total_commits_count: Int)
case class Repository(name: String, url: String, description: String, homepage: String)
case class Commit(id: String, message: String, url: String, author: Author)
case class Author(name: String, email: String)