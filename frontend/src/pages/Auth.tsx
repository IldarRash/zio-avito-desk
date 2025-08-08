import React from 'react';
import { Box, Button, Tab, Tabs, TextField } from '@mui/material';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAppDispatch } from '../store';
import { setCredentials } from '../store/slices/authSlice';

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
});

type LoginForm = z.infer<typeof loginSchema>;

export const AuthPage: React.FC = () => {
  const [tab, setTab] = React.useState(0);
  const dispatch = useAppDispatch();

  const { register, handleSubmit, formState: { errors } } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = (data: LoginForm) => {
    // TODO: call real mutation; mock token for now
    const fakeToken = 'token';
    dispatch(setCredentials({ token: fakeToken, userRole: 'user' }));
  };

  return (
    <Box>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="Войти" />
        <Tab label="Регистрация" />
      </Tabs>
      <Box component="form" onSubmit={handleSubmit(onSubmit)} display="flex" flexDirection="column" gap={2}>
        <TextField label="Email" {...register('email')} error={!!errors.email} helperText={errors.email?.message} />
        <TextField label="Пароль" type="password" {...register('password')} error={!!errors.password} helperText={errors.password?.message} />
        <Button type="submit" variant="contained">Продолжить</Button>
      </Box>
    </Box>
  );
};

export default AuthPage;
