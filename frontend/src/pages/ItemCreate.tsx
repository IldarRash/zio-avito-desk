import React from 'react';
import { Box, Button, TextField } from '@mui/material';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';

const schema = z.object({
  name: z.string().min(2),
  price: z.coerce.number().min(0),
  description: z.string().optional(),
});

type Form = z.infer<typeof schema>;

export const ItemCreatePage: React.FC = () => {
  const { register, handleSubmit, formState: { errors } } = useForm<Form>({ resolver: zodResolver(schema) });

  const onSubmit = (data: Form) => {
    console.log(data);
  };

  return (
    <Box component="form" onSubmit={handleSubmit(onSubmit)} display="flex" flexDirection="column" gap={2}>
      <TextField label="Название" {...register('name')} error={!!errors.name} helperText={errors.name?.message} />
      <TextField label="Цена" type="number" {...register('price')} error={!!errors.price} helperText={errors.price?.message} />
      <TextField label="Описание" multiline rows={4} {...register('description')} />
      <Button type="submit" variant="contained">Создать</Button>
    </Box>
  );
};

export default ItemCreatePage;
