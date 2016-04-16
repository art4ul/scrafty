package com.art4ul.scrafty.protocol

/**
  * Created by artsemsemianenka on 4/9/16.
  */
trait Request

trait RaftRequest extends Request {
  val term: Int
}

case class AppendEntryRequest(override val term: Int,
                              leaderId: String) extends RaftRequest

case class VoteRequest(override val term: Int,
                       candidateId: String) extends RaftRequest

case class NewCommandRequest(command: String) extends Request

trait Response

trait RaftResponse extends Response {
  val term: Int
  val result: Boolean
}

case class AppendEntryResponse(override val term: Int,
                               override val result: Boolean) extends RaftResponse

case class VoteResponse(override val term: Int,
                        override val result: Boolean) extends RaftResponse

case class NewCommandResponse(commited: Boolean = false, redirect: Boolean, redirectTo: Option[String] = None) extends Response
