package com.example.zivito

import caliban.ZHttpAdapter
import zhttp.http._
import zio._

object GraphQLRoutes {
  def routes: HttpApp[ItemService with CategoryService with ChatService with AuthService with CartService with OrderService, Throwable] =
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
