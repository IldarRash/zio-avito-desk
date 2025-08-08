import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Box, Typography } from '@mui/material';
import { getItem } from '../services/api';

export const ItemPage: React.FC = () => {
  const { id } = useParams();
  const [it, setItem] = useState<any | null>(null);

  useEffect(() => {
    if (id) getItem(id).then(setItem);
  }, [id]);

  if (!it) return null;

  return (
    <Box>
      <Typography variant="h4" gutterBottom>{it.name}</Typography>
      <Typography variant="h6" color="success.main">{Number(it.price)} ₽</Typography>
      <Typography variant="body1" sx={{ my: 2 }}>{it.description}</Typography>
      <Typography variant="body2" color="text.secondary">{it.location}</Typography>
    </Box>
  );
};

export default ItemPage;
