package com.sky.ukiss.pipelinespawner

package object hooks {
  case class GithubPayload(user_name: String, project: Option[Project], repository: Repository, commits: List[Commit], after: String, total_commits_count: Int)
  case class Project(id: Long, name: String, web_url: String, description: String, git_ssh_url: String, git_http_url: String, namespace: String, homepage: String)
  case class Repository(name: String, url: String, description: String, homepage: String)
  case class Commit(id: String, message: String, url: String, author: Author)
  case class Author(name: String, email: String)
}
