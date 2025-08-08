package com.example.zivito

import caliban._
import caliban.schema.GenericSchema
import zio._
import zhttp.http._
import zhttp.http.Method._
import java.util.UUID
import caliban.ZHttpAdapter

final case class Queries(itemService: ItemService, categoryService: CategoryService) {
  def items(query: Option[String]): Task[List[Domain.Item]] =
    query match {
      case Some(q) if q.nonEmpty => itemService.search(q)
      case _ => itemService.getAll
    }

  def item(id: UUID): Task[Option[Domain.Item]] = itemService.get(id)
  def categories: Task[List[Domain.Category]] = categoryService.getAllCategories
}

final case class Mutations(itemService: ItemService, categoryService: CategoryService) {
  case class CreateItemInput(name: String, description: String, price: BigDecimal, categoryId: UUID, location: String)
  case class CreateCategoryInput(name: String)

  def createItem(input: CreateItemInput): Task[Domain.Item] =
    itemService.create(Domain.Item(UUID.randomUUID(), input.name, input.description, input.price, input.categoryId, input.location))

  def createCategory(input: CreateCategoryInput): Task[Domain.Category] =
    categoryService.createCategory(Domain.Category(UUID.randomUUID(), input.name))
}

object GraphQLApi extends GenericSchema[Any]

object GraphQLRoutes extends GraphQLApi {

  implicit val itemSchema: Schema[Any, Domain.Item] = gen
  implicit val categorySchema: Schema[Any, Domain.Category] = gen

  final case class Api(queries: Queries, mutations: Mutations)

  def makeApi: ZIO[ItemService with CategoryService, Nothing, GraphQLInterpreter[Any, CalibanError]] = {
    for {
      is <- ZIO.service[ItemService]
      cs <- ZIO.service[CategoryService]
      queries = Queries(is, cs)
      mutations = Mutations(is, cs)
      api <- caliban.GraphQL.graphQL(RootResolver(queries, mutations)).interpreter
    } yield api
  }

  def routes: HttpApp[ItemService with CategoryService, Throwable] =
    Http.fromZIO(GraphQLApi.api.interpreter).flatMap { interpreter =>
      val graphQLApp = ZHttpAdapter.makeHttpService(interpreter)
      val graphiql = Http.collect[Request] { case Method.GET -> !! / "graphiql" =>
        Response.html(
          """
            |<!DOCTYPE html>
            |<html>
            |<head>
            |  <meta charset="utf-8"/>
            |  <title>GraphiQL</title>
            |  <meta name="viewport" content="width=device-width, initial-scale=1.0">
            |  <link rel="stylesheet" href="https://unpkg.com/graphiql/graphiql.min.css" />
            |</head>
            |<body style="margin:0;">
            |  <div id="graphiql" style="height:100vh;"></div>
            |  <script crossorigin src="https://unpkg.com/react/umd/react.production.min.js"></script>
            |  <script crossorigin src="https://unpkg.com/react-dom/umd/react-dom.production.min.js"></script>
            |  <script src="https://unpkg.com/graphiql/graphiql.min.js"></script>
            |  <script>
            |    const graphQLFetcher = graphQLParams => fetch('/graphql', {
            |      method: 'post',
            |      headers: { 'Content-Type': 'application/json' },
            |      body: JSON.stringify(graphQLParams),
            |      credentials: 'include'
            |    }).then(response => response.json());
            |    ReactDOM.render(
            |      React.createElement(GraphiQL, { fetcher: graphQLFetcher }),
            |      document.getElementById('graphiql'),
            |    );
            |  </script>
            |</body>
            |</html>
          |""".stripMargin)
      }
      graphQLApp.withDefaultErrorResponse @@ Middleware.cors() ++ graphiql
    }
}
