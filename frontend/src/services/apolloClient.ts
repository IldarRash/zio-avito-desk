import { ApolloClient, InMemoryCache, ApolloLink, createHttpLink } from '@apollo/client';

const GRAPHQL_ENDPOINT = process.env.REACT_APP_GRAPHQL_URL || '/graphql';

const authLink = new ApolloLink((operation, forward) => {
  const token = localStorage.getItem('token');
  operation.setContext(({ headers = {} }) => ({
    headers: {
      ...headers,
      Authorization: token ? `Bearer ${token}` : '',
    },
  }));
  return forward(operation);
});

const httpLink = createHttpLink({
  uri: GRAPHQL_ENDPOINT,
  credentials: 'include',
});

export const apolloClient = new ApolloClient({
  link: authLink.concat(httpLink as unknown as ApolloLink),
  cache: new InMemoryCache(),
});
