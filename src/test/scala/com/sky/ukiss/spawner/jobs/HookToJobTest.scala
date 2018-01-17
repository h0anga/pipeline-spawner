package com.sky.ukiss.spawner.jobs

import java.lang

import com.flextrade.jfixture.JFixtureSugar
import io.fabric8.kubernetes.api.model.Job
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.dsl.NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable
import org.mockito.ArgumentMatchers.any
import org.mockito.{Answers, ArgumentCaptor, Mockito}
import org.mockito.Mockito.verify
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}

class HookToJobTest extends FunSpec with Matchers with JFixtureSugar with MockitoSugar {

  private val kubernetes = mock[KubernetesClient](Answers.RETURNS_DEEP_STUBS)
  private val namespaceVisit = mock[NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable[Job, lang.Boolean]]

  describe("The HookToJob") {

    val hookToJob = new HookToJob(kubernetes)
    val hook = fixture[HookData]

    Mockito.when(kubernetes.resource(any[Job]())).thenReturn(namespaceVisit)

    it("should submit a hook as a kubernetes job") {
      hookToJob.submit(hook)

      verify(namespaceVisit).createOrReplace()
    }
  }
}
