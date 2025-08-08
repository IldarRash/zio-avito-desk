import React from 'react';
import { Card, CardContent, CardMedia, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

interface Props {
  id: string;
  name: string;
  description?: string;
  price?: number;
  location?: string;
  imageUrl?: string | null;
}

export const ItemCard: React.FC<Props> = ({ id, name, description, price, location, imageUrl }) => {
  return (
    <Card component={RouterLink} to={`/items/${id}`} sx={{ textDecoration: 'none' }}>
      {imageUrl ? (
        <CardMedia component="img" height="160" image={imageUrl} alt={name} />
      ) : null}
      <CardContent>
        <Typography variant="h6">{name}</Typography>
        {description ? <Typography variant="body2" color="text.secondary">{description}</Typography> : null}
        <Typography variant="subtitle1" color="success.main">{price != null ? `${price} ₽` : ''}</Typography>
        <Typography variant="caption" color="text.secondary">{location}</Typography>
      </CardContent>
    </Card>
  );
};

export default ItemCard;
