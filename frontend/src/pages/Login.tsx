import React from 'react';
import { useForm } from 'react-hook-form';
import { gql, useMutation } from '@apollo/client';

const LOGIN = gql`
  mutation Login($email: String!, $password: String!) {
    login(arg1: $email, arg2: $password) { value }
  }
`;

type FormValues = { email: string; password: string };

export default function Login() {
  const { register, handleSubmit } = useForm<FormValues>();
  const [login, { loading, error }] = useMutation(LOGIN);

  const onSubmit = async (values: FormValues) => {
    const res = await login({ variables: { email: values.email, password: values.password } });
    const token = res.data?.login?.value;
    if (token) {
      localStorage.setItem('jwt', token);
      window.location.href = '/';
    }
  };

  return (
    <div className="max-w-md mx-auto p-6">
      <h2 className="text-2xl font-bold mb-4">Login</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
        <input className="border rounded px-3 py-2 w-full" placeholder="Email" {...register('email')} />
        <input className="border rounded px-3 py-2 w-full" placeholder="Password" type="password" {...register('password')} />
        {error && <div className="text-red-600 text-sm">{error.message}</div>}
        <button className="bg-blue-600 text-white px-4 py-2 rounded" disabled={loading}>
          {loading ? '...' : 'Login'}
        </button>
      </form>
    </div>
  );
}

