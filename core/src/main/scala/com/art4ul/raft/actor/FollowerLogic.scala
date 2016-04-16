package com.art4ul.raft.actor

import akka.actor.FSM
import akka.actor.FSM.Event
import com.art4ul.raft.state.{State, Candidate, Data, Follower}
import com.art4ul.raft.utils.StateLogger
import com.art4ul.scrafty.protocol._

/**
  * Created by artsemsemianenka on 4/16/16.
  */
trait FollowerLogic extends StateLogger {
  selfRef: FSM[State, Data] with CommonRaft=>

  when(Follower, electionTimeout) {
    case Event(msg: AppendEntryRequest, data: Data) =>
      logState(s"message:$msg")
      handleRequest(msg) {
        case term if (term == data.currentTerm) => stay -> AppendEntryResponse(data.currentTerm, true)
        case term if (term >= data.currentTerm) =>
          val newData = data.copy(currentTerm = term, votedFor = Some(msg.leaderId))
          stay.using(newData) -> AppendEntryResponse(term, true)
        case _ => stay() -> AppendEntryResponse(data.currentTerm, false)
      }

    case Event(msg@StateTimeout, data: Data) =>
      logState(s"message:$msg")
      val newData = data.copy(currentTerm = data.currentTerm + 1,
        votedFor = Some(id),
        votes = 1)
      goto(Candidate) using newData

    case Event(msg: VoteRequest, data: Data) =>
      logState(s"message:$msg")
      handleRequest(msg) {
        case term if (term > data.currentTerm) || (term == data.currentTerm && data.votedFor.isEmpty) =>
          val newData = data.copy(currentTerm = term,
            votedFor = Some(msg.candidateId),
            votes = 0)
          stay.using(newData) -> VoteResponse(term, true)

        case _ => stay -> VoteResponse(data.currentTerm, false)
      }

    case Event(msg: NewCommandRequest, data: Data) =>
      sender ! NewCommandResponse(redirect = true, redirectTo = data.votedFor)
      stay()
  }

}
