import React, { useEffect, useState } from 'react';
import { Grid } from '@mui/material';
import SearchBar from '../components/SearchBar';
import ItemCard from '../components/ItemCard';
import { getItems, searchItems } from '../services/api';

export const FeedPage: React.FC = () => {
  const [q, setQ] = useState('');
  const [items, setItems] = useState<any[]>([]);

  useEffect(() => {
    getItems().then(setItems);
  }, []);

  const onSearch = async () => {
    if (!q) {
      const data = await getItems();
      setItems(data);
    } else {
      const data = await searchItems(q);
      setItems(data);
    }
  };

  return (
    <>
      <SearchBar value={q} onChange={setQ} onSearch={onSearch} />
      <Grid container spacing={2} sx={{ mt: 2 }}>
        {items?.map((it: any) => (
          <Grid key={it.id} item xs={12} sm={6} md={4} lg={3}>
            <ItemCard id={it.id} name={it.name} description={it.description} price={Number(it.price)} location={it.location} />
          </Grid>
        ))}
      </Grid>
    </>
  );
};

export default FeedPage;
