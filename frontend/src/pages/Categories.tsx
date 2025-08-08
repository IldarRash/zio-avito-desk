import React from 'react';
import { gql, useQuery } from '@apollo/client';
import { List, ListItemButton, ListItemText, Typography } from '@mui/material';

const CATEGORIES_QUERY = gql`
  query Categories {
    categories {
      id
      name
    }
  }
`;

export const CategoriesPage: React.FC = () => {
  const { data } = useQuery(CATEGORIES_QUERY);
  return (
    <>
      <Typography variant="h5" gutterBottom>Категории</Typography>
      <List>
        {data?.categories?.map((c: any) => (
          <ListItemButton key={c.id}>
            <ListItemText primary={c.name} />
          </ListItemButton>
        ))}
      </List>
    </>
  );
};

export default CategoriesPage;
