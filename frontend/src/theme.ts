import { createTheme } from '@mui/material/styles';

// Avito/Ozon-inspired clean blue theme
export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1463FF',
    },
    secondary: {
      main: '#00A88E',
    },
    success: {
      main: '#2FB344',
    },
    background: {
      default: '#f6f7f9',
    },
  },
  shape: {
    borderRadius: 10,
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          transition: 'box-shadow .2s ease',
        },
      },
    },
  },
});
