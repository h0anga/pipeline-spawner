package com.sky.ukiss.spawner.jobs

import net.liftweb.http.SessionVar

import scala.collection.mutable

object Jobs extends SessionVar[mutable.Set[JobData]](mutable.Set())
