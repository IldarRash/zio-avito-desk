package com.example.zivito

import com.example.zivito.Config.ApplicationConfig

case class Config(appConfig: ApplicationConfig)

object Config {

  final case class DynamodbConfig(url: String, driver: String, user: String, password: String)
  final case class ElasticSearchConfig(url: String, driver: String, user: String, password: String)
  final case class ServerConfig(url: String, port:Int)
  final case class ApplicationConfig(dynamodbConfig: DynamodbConfig, elasticSearchConfig: ElasticSearchConfig, serverConfig: ServerConfig)

}
