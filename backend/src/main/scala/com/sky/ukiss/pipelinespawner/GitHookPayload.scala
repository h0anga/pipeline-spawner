package com.sky.ukiss.pipelinespawner

case class GitHookPayload(user_name: String, project: Project, commits: List[Commit], after: String, total_commits_count: Int)
case class Project(id: Long, name: String, web_url: String, description: String, git_ssh_url: String, git_http_url: String, namespace: String, homepage: String)
case class Commit(id: String, message: String, url: String, author: Author)
case class Author(name: String, email: String)