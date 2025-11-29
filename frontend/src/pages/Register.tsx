import React from 'react';
import { useForm } from 'react-hook-form';
import { gql, useMutation } from '@apollo/client';

const REGISTER = gql`
  mutation Register($name: String!, $email: String!, $password: String!) {
    register(arg1: $name, arg2: $email, arg3: $password) { id email name }
  }
`;

type FormValues = { name: string; email: string; password: string };

export default function Register() {
  const { register, handleSubmit } = useForm<FormValues>();
  const [mutate, { loading, error }] = useMutation(REGISTER);

  const onSubmit = async (values: FormValues) => {
    await mutate({ variables: values });
    window.location.href = '/login';
  };

  return (
    <div className="max-w-md mx-auto p-6">
      <h2 className="text-2xl font-bold mb-4">Register</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
        <input className="border rounded px-3 py-2 w-full" placeholder="Name" {...register('name')} />
        <input className="border rounded px-3 py-2 w-full" placeholder="Email" {...register('email')} />
        <input className="border rounded px-3 py-2 w-full" placeholder="Password" type="password" {...register('password')} />
        {error && <div className="text-red-600 text-sm">{error.message}</div>}
        <button className="bg-blue-600 text-white px-4 py-2 rounded" disabled={loading}>
          {loading ? '...' : 'Register'}
        </button>
      </form>
    </div>
  );
}

