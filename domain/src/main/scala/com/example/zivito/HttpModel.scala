package com.example.zivito

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

object HttpModel {

  case class RequestInfo[T: Filter](filter: T, paging: Paging = Paging())

  object RequestInfo {
    implicit val decoder: JsonDecoder[RequestInfo] = DeriveJsonDecoder.gen[RequestInfo]
  }

  sealed trait Filter {
    def nameValue(): (String, String)
  }


  case class ResponseInfo[T](body: T, paging: Paging = Paging(), status: Int)

  object RequestInfo {
    implicit val encoder: JsonEncoder[ResponseInfo] = DeriveJsonEncoder.gen[ResponseInfo]
  }


  case class Paging(start: Int = 0, limit: Int = 10)

  object Paging {
    implicit val encoder: JsonEncoder[Paging] = DeriveJsonEncoder.gen[Paging]
    implicit val decoder: JsonDecoder[Paging] = DeriveJsonDecoder.gen[Paging]
  }

}


