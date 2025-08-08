import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import FeedPage from './pages/Feed';
import ItemPage from './pages/Item';
import AuthPage from './pages/Auth';
import ProfilePage from './pages/Profile';
import ItemCreatePage from './pages/ItemCreate';
import CategoriesPage from './pages/Categories';
import ChatPage from './pages/Chat';
import AdminPage from './pages/Admin';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/items" replace />} />
        <Route path="/items" element={<FeedPage />} />
        <Route path="/items/new" element={<ProtectedRoute />}> 
          <Route index element={<ItemCreatePage />} />
        </Route>
        <Route path="/items/:id" element={<ItemPage />} />
        <Route path="/categories" element={<CategoriesPage />} />
        <Route path="/chat" element={<ProtectedRoute />}> 
          <Route index element={<ChatPage />} />
        </Route>
        <Route path="/auth/login" element={<AuthPage />} />
        <Route path="/profile" element={<ProtectedRoute />}> 
          <Route index element={<ProfilePage />} />
        </Route>
        <Route path="/admin" element={<ProtectedRoute role="admin" />}> 
          <Route index element={<AdminPage />} />
        </Route>
      </Routes>
    </Layout>
  );
}

export default App;
