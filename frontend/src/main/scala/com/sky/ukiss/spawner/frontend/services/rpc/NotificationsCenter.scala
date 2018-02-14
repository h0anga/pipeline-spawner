package com.sky.ukiss.spawner.frontend.services.rpc

import com.sky.ukiss.spawner.shared.model.chat.ChatMessage
import com.sky.ukiss.spawner.shared.model.jobs.JobMessage
import io.udash.utils.{CallbacksHandler, Registration}

/** Provides notifications about new messages and connections status. */
class NotificationsCenter {
  private[rpc] val msgListeners: CallbacksHandler[ChatMessage] = new CallbacksHandler[ChatMessage]
  private[rpc] val connectionsListeners: CallbacksHandler[Int] = new CallbacksHandler[Int]
  private[rpc] val jobListeners: CallbacksHandler[JobMessage] = new CallbacksHandler[JobMessage]

  def onNewMsg(callback: msgListeners.CallbackType): Registration =
    msgListeners.register(callback)

  def onConnectionsCountChange(callback: connectionsListeners.CallbackType): Registration =
    connectionsListeners.register(callback)

  def onJobMessage(callback: jobListeners.CallbackType) = jobListeners.register(callback)
}
