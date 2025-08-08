import React, {useMemo, useState} from 'react';
import './App.css';
import { gql, useQuery } from '@apollo/client';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';

const ITEMS_QUERY = gql`
  query Items($q: String) {
    items: items
    search(q: $q)
  }
`;

type Item = {
  id: string;
  name: string;
  description: string;
  price: number;
  location: string;
  categoryId: string;
}

function Home() {
  const [search, setSearch] = useState('');
  const variables = useMemo(() => ({ q: search || null }), [search]);
  const { data, loading } = useQuery(ITEMS_QUERY, { variables });
  const items: Item[] = search ? (data?.search ?? []) : (data?.items ?? []);

  return (
    <div className="container mx-auto p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Avito Desk</h1>
        <div className="flex gap-4 text-sm">
          <Link to="/login" className="text-blue-600">Login</Link>
          <Link to="/register" className="text-blue-600">Register</Link>
        </div>
      </div>
      <div className="flex justify-center gap-2 mb-6">
        <input
          className="border rounded px-3 py-2 w-1/2"
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search for items..."
        />
        <button className="bg-blue-600 text-white px-4 py-2 rounded" onClick={() => setSearch(search)}>
          Search
        </button>
      </div>
      {loading ? (
        <div className="text-center">Loading...</div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {items.map((item) => (
            <div key={item.id} className="border rounded p-4 shadow-sm">
              <h2 className="text-xl font-semibold mb-2">{item.name}</h2>
              <p className="text-gray-700 mb-2">{item.description}</p>
              <div className="flex justify-between text-sm text-gray-600">
                <span className="font-bold text-green-600">{item.price}</span>
                <span>{item.location}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Routes>
    </BrowserRouter>
  );
}
