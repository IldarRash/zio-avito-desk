import { ApolloClient, InMemoryCache, HttpLink, from } from '@apollo/client';

const httpLink = new HttpLink({ uri: import.meta?.env?.VITE_API_HTTP || '/graphql', credentials: 'include' });

export const apollo = new ApolloClient({
  link: from([httpLink]),
  cache: new InMemoryCache(),
});
