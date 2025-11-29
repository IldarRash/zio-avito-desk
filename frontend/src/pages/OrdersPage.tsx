import React from 'react';
import { gql, useQuery } from '@apollo/client';
import { Link } from 'react-router-dom';

const GET_ORDERS = gql`
  query MyOrders($userId: String!) {
    myOrders(arg1: $userId) {
      id
      totalPrice
      status
      createdAt
    }
  }
`;

const getUserId = () => {
  const token = localStorage.getItem('jwt');
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub;
  } catch (e) {
    return null;
  }
};

export default function OrdersPage() {
  const userId = getUserId();
  const { data, loading } = useQuery(GET_ORDERS, {
    variables: { userId },
    skip: !userId
  });

  if (!userId) return <div className="p-6">Please <Link to="/login" className="text-blue-600">login</Link> to view your orders.</div>;
  if (loading) return <div className="p-6">Loading orders...</div>;

  const orders = data?.myOrders || [];

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-3xl font-bold mb-6">My Orders</h1>
      {orders.length === 0 ? (
        <p>No orders found.</p>
      ) : (
        <div className="space-y-4">
          {orders.map((order: any) => (
            <div key={order.id} className="border p-4 rounded shadow-sm">
              <div className="flex justify-between items-center mb-2">
                <span className="font-semibold">Order #{order.id.slice(0, 8)}</span>
                <span className={`px-2 py-1 rounded text-sm ${
                  order.status === 'CREATED' ? 'bg-blue-100 text-blue-800' : 'bg-green-100 text-green-800'
                }`}>
                  {order.status}
                </span>
              </div>
              <div className="text-gray-600 text-sm mb-2">
                Date: {new Date(order.createdAt).toLocaleString()}
              </div>
              <div className="font-bold text-lg">
                Total: {order.totalPrice} ₽
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

