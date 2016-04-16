package com.art4ul.raft.actor

import java.util.concurrent.TimeUnit

import akka.actor.FSM
import com.art4ul.raft.ExecutionConfig
import com.art4ul.raft.state._
import com.art4ul.raft.utils.StateLogger
import com.art4ul.scrafty.protocol.{NewCommandRequest, VoteResponse}

import scala.concurrent.duration.FiniteDuration

/**
  * Created by artsemsemianenka on 4/16/16.
  */
trait CandidateLogic extends StateLogger {
  selfRef: FSM[State, Data] with CommonRaft=>

  val electionPeriod = FiniteDuration(ExecutionConfig.electionPeriodMs, TimeUnit.MILLISECONDS)

  when(Candidate, stateTimeout =  electionPeriod) {
    case Event(msg@StateTimeout, data: Data) =>
      logState(s"message:$msg")
      if (isVotesReached(data.votes)) {
        val newData = data.copy(currentTerm = data.currentTerm, votedFor = Some(id), votes = 0)
        goto(Leader) using newData
      } else {
        val newData = data.copy(currentTerm = data.currentTerm, votedFor =  None, votes = 0)
        goto(Follower).forMax(electionTimeout) using newData
      }

    case Event(voteResponse: VoteResponse, data: Data) =>
      logState(s"message:$voteResponse")
      val votes = if (voteResponse.result) data.votes + 1 else data.votes
      if (isVotesReached(votes)) {
        goto(Leader) using data.copy(votes = 0)
      } else {
        stay using data.copy(votes = votes)
      }

    case Event(msg: NewCommandRequest, data: Data) => ???
  }

}
