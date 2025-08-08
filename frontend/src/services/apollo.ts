import { ApolloClient, InMemoryCache, HttpLink, from } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';

const httpUri = (process.env.REACT_APP_API_HTTP as string) || '/graphql';
const httpLink = new HttpLink({ uri: httpUri, credentials: 'include' });

const authLink = setContext((_, { headers }) => {
  const token = typeof window !== 'undefined' ? localStorage.getItem('jwt') : null;
  return {
    headers: {
      ...headers,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  };
});

export const apollo = new ApolloClient({
  link: from([authLink, httpLink]),
  cache: new InMemoryCache(),
});
