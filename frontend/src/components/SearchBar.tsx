import React from 'react';
import { Box, Button, TextField } from '@mui/material';

interface Props {
  value: string;
  onChange: (v: string) => void;
  onSearch: () => void;
}

export const SearchBar: React.FC<Props> = ({ value, onChange, onSearch }) => {
  const onKey = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') onSearch();
  };
  return (
    <Box display="flex" gap={2}>
      <TextField fullWidth value={value} onChange={(e) => onChange(e.target.value)} onKeyDown={onKey} placeholder="Поиск..." />
      <Button variant="contained" onClick={onSearch}>Искать</Button>
    </Box>
  );
};

export default SearchBar;
