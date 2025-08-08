import React, { PropsWithChildren } from 'react';
import { AppBar, Toolbar, Typography, Container, Box, Button } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { useAppSelector } from '../store';

export const Layout: React.FC<PropsWithChildren> = ({ children }) => {
  const token = useAppSelector((s) => s.auth.token);

  return (
    <Box display="flex" flexDirection="column" minHeight="100vh">
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component={RouterLink} to="/" sx={{ color: 'inherit', textDecoration: 'none', flexGrow: 1 }}>
            Avito Desk
          </Typography>
          <Button color="inherit" component={RouterLink} to="/items">Лента</Button>
          <Button color="inherit" component={RouterLink} to="/categories">Категории</Button>
          <Button color="inherit" component={RouterLink} to="/chat">Чат</Button>
          {token ? (
            <Button color="inherit" component={RouterLink} to="/profile">Профиль</Button>
          ) : (
            <Button color="inherit" component={RouterLink} to="/auth/login">Войти</Button>
          )}
        </Toolbar>
      </AppBar>
      <Container sx={{ py: 3, flex: 1 }}>{children}</Container>
      <Box component="footer" sx={{ py: 2, textAlign: 'center', bgcolor: 'grey.100' }}>
        <Typography variant="body2">© {new Date().getFullYear()} Avito Desk</Typography>
      </Box>
    </Box>
  );
};

export default Layout;
