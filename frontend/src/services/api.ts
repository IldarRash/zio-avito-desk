import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || '/',
  withCredentials: true,
});

// Items
export async function getItems() {
  const { data } = await api.get('/items');
  return data;
}

export async function searchItems(query: string) {
  const { data } = await api.get(`/items/search/${encodeURIComponent(query)}`);
  return data;
}

export async function getItem(id: string) {
  const { data } = await api.get(`/items/${id}`);
  return data;
}

export async function createItem(payload: { name: string; description: string; price: number; categoryId: string; location: string; }) {
  const { data } = await api.post('/items', payload);
  return data;
}

// Categories
export async function getCategories() {
  const { data } = await api.get('/categories');
  return data;
}

export default api;
