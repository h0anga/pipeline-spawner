package com.sky.ukiss.spawner.jobs

case class HookData(
                     before: String,
                     after: String,
                     repository: Repository,
                     total_commits_count: Int
                   )

case class Repository(
                       url: String,
                       homepage: String,
                       name: String,
                       description: String
                     )