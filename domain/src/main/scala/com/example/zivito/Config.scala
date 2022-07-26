package com.example.zivito

import com.example.zivito.Config.ApplicationConfig

case class Config(appConfig: ApplicationConfig)

object Config {

  final case class ElasticSearchConfig(url: String, user: String, password: String)
  final case class ServerConfig(url: String, port:Int)
  final case class ApplicationConfig(elasticSearchConfig: ElasticSearchConfig, serverConfig: ServerConfig)

}
