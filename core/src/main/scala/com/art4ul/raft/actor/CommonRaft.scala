package com.art4ul.raft.actor

import java.util.concurrent.TimeUnit

import akka.actor.FSM
import com.art4ul.raft.ExecutionConfig
import com.art4ul.raft.client.Client
import com.art4ul.raft.state.{Follower, Data, State}
import com.art4ul.raft.utils.StateLogger
import com.art4ul.scrafty.protocol._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

/**
  * Created by artsemsemianenka on 4/16/16.
  */
trait CommonRaft extends StateLogger{ selfRef : FSM[State, Data] =>

  val servers: Map[String, Client]
  val id:String

  whenUnhandled {
    case Event(msg: AppendEntryRequest, data: Data) =>
      logState(s"message:$msg")
      handleRequest(msg) {
        case term if (term > data.currentTerm) =>
          val newData = data.copy(currentTerm = term, votedFor = Some(msg.leaderId))
          goto(Follower).using(newData) -> AppendEntryResponse(term, true)

        case _ => stay -> AppendEntryResponse(data.currentTerm, true)
      }

    case Event(msg: VoteRequest, data: Data) =>
      logState(s"message:$msg")
      handleRequest(msg) {
        case term if (term > data.currentTerm) =>
          val newData = data.copy(currentTerm = term, votedFor = Some(msg.candidateId))
          goto(Follower).using(newData) -> VoteResponse(term, true)

        case _ => stay -> VoteResponse(data.currentTerm, false)
      }

    case Event(e, s) =>
      println("Received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }

  def handleRequest[S, D](request: RaftRequest)(function: PartialFunction[Int, (FSM.State[S, D], RaftResponse)]): FSM.State[S, D] = {
    val (state, response) = function(request.term)
    sender ! response
    state
  }

  def isVotesReached(votes: Int) = votes >= (servers.size + 1) / 2 + 1

  def startElection(term: Int): Unit = {
    val responseHandler: Response => Unit = response => self ! response
    broadcast(VoteRequest(term, id), responseHandler)
  }

  def broadcast(request: Request, responseHandler: Response => Unit): Unit =
    servers.values.foreach { client =>
      client.send(request).onSuccess { case response => responseHandler(response) }
    }

  def electionTimeout = {
    val randomize = new Random()
    val initTimeout = ExecutionConfig.electionTimeoutMs
    val timeout = initTimeout + randomize.nextInt(initTimeout.toInt)
    println(s"electTimeout:$timeout")
    new FiniteDuration(timeout, TimeUnit.MILLISECONDS)
  }

}
