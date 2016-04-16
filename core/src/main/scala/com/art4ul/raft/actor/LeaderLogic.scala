package com.art4ul.raft.actor

import java.util.concurrent.TimeUnit

import akka.actor.FSM
import com.art4ul.raft.ExecutionConfig
import com.art4ul.raft.state.{CommandModel, Leader, Data, State}
import com.art4ul.raft.utils.StateLogger
import com.art4ul.scrafty.protocol.{AppendEntryRequest, Response, NewCommandRequest, AppendEntryResponse}

import scala.concurrent.duration.FiniteDuration

/**
  * Created by artsemsemianenka on 4/16/16.
  */
trait LeaderLogic extends StateLogger {
  selfRef: FSM[State, Data] with CommonRaft=>

  val heartBeatTimeout = FiniteDuration(ExecutionConfig.heartBeatTimeoutMs, TimeUnit.MILLISECONDS)

  when(Leader, stateTimeout = heartBeatTimeout) {
    case Event(msg@StateTimeout, data: Data) =>
      logState(s"message:$msg")
      sendHeartBeat(data.currentTerm)
      stay

    case Event(response: AppendEntryResponse, data: Data) =>
      logState(s"message:$response")
      stay

    case Event(msg: NewCommandRequest, data: Data) =>
      val connectionRef = sender
      val newLog = data.log :+ CommandModel(data.currentTerm, msg.command, connectionRef)
      stay() using (data.copy(log = newLog))
  }

  def sendHeartBeat(term: Int): Unit = {
    val responseHandler: Response => Unit = response => self ! response
    broadcast(AppendEntryRequest(term, id), responseHandler)
  }

}
