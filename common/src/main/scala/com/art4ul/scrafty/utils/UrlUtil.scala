package com.art4ul.scrafty.utils

/**
  * Created by artsemsemianenka on 4/16/16.
  */
object UrlUtil {

  def parseHostAndPort(url:String):(String,Int) ={
    val separated = url.split(":")
    (separated(0), separated(1).toInt)
  }

}
