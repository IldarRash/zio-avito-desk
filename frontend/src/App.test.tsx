import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';
import { BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import { store } from './store';
import { ApolloProvider } from '@apollo/client';
import { apolloClient } from './services/apolloClient';


test('renders app title', () => {
  render(
    <ApolloProvider client={apolloClient}>
      <Provider store={store}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </Provider>
    </ApolloProvider>
  );
  expect(screen.getAllByText(/Avito Desk/i)[0]).toBeInTheDocument();
});
