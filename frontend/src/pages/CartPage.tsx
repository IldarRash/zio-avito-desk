import React, { useEffect } from 'react';
import { gql, useQuery, useMutation } from '@apollo/client';
import { Link } from 'react-router-dom';

const GET_CART = gql`
  query MyCart($userId: String!) {
    myCart(arg1: $userId) {
      id
      name
      price
      location
    }
  }
`;

const REMOVE_FROM_CART = gql`
  mutation RemoveFromCart($userId: String!, $itemId: String!) {
    removeFromCart(arg1: $userId, arg2: $itemId)
  }
`;

const CHECKOUT = gql`
  mutation Checkout($userId: String!) {
    checkout(arg1: $userId) {
      id
      totalPrice
      status
    }
  }
`;

// Helper to get user ID from token (naive implementation for demo)
const getUserId = () => {
  const token = localStorage.getItem('jwt');
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub; // 'sub' is usually the user ID
  } catch (e) {
    return null;
  }
};

export default function CartPage() {
  const userId = getUserId();
  const { data, loading, refetch } = useQuery(GET_CART, {
    variables: { userId },
    skip: !userId,
    fetchPolicy: 'network-only'
  });

  const [removeItem] = useMutation(REMOVE_FROM_CART);
  const [checkout] = useMutation(CHECKOUT);

  if (!userId) return <div className="p-6">Please <Link to="/login" className="text-blue-600">login</Link> to view your cart.</div>;
  if (loading) return <div className="p-6">Loading cart...</div>;

  const items = data?.myCart || [];
  const total = items.reduce((sum: number, item: any) => sum + item.price, 0);

  const handleRemove = async (itemId: string) => {
    await removeItem({ variables: { userId, itemId } });
    refetch();
  };

  const handleCheckout = async () => {
    await checkout({ variables: { userId } });
    alert('Order placed successfully!');
    refetch();
  };

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-3xl font-bold mb-6">Shopping Cart</h1>
      {items.length === 0 ? (
        <p>Your cart is empty.</p>
      ) : (
        <>
          <div className="space-y-4 mb-8">
            {items.map((item: any) => (
              <div key={item.id} className="flex justify-between items-center border p-4 rounded shadow-sm">
                <div>
                  <h3 className="font-semibold text-lg">{item.name}</h3>
                  <p className="text-gray-600">{item.location}</p>
                </div>
                <div className="flex items-center gap-4">
                  <span className="font-bold text-green-600">{item.price} ₽</span>
                  <button
                    onClick={() => handleRemove(item.id)}
                    className="text-red-500 hover:text-red-700"
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>
          <div className="flex justify-end items-center gap-4 border-t pt-4">
            <div className="text-xl font-bold">Total: {total} ₽</div>
            <button
              onClick={handleCheckout}
              className="bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700"
            >
              Checkout
            </button>
          </div>
        </>
      )}
    </div>
  );
}

