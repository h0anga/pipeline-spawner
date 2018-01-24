package com.sky.ukiss.spawner.jobs

import com.sky.ukiss.spawner.ProdConfiguration
import net.liftweb.http.SessionVar

object Jobs extends SessionVar[Set[JobData]](ProdConfiguration.jobEvents.vend.currentJobs)
