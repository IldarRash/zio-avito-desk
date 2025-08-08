import React, { useEffect, useState } from 'react';
import { List, ListItemButton, ListItemText, Typography } from '@mui/material';
import { getCategories } from '../services/api';

export const CategoriesPage: React.FC = () => {
  const [categories, setCategories] = useState<any[]>([]);

  useEffect(() => {
    getCategories().then((data) => setCategories(data as any[]));
  }, []);

  return (
    <>
      <Typography variant="h5" gutterBottom>Категории</Typography>
      <List>
        {categories.map((c: any) => (
          <ListItemButton key={c.id}>
            <ListItemText primary={c.name} />
          </ListItemButton>
        ))}
      </List>
    </>
  );
};

export default CategoriesPage;
