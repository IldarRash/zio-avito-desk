import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface AuthState {
  token: string | null;
  userRole: 'user' | 'admin' | null;
}

const initialState: AuthState = {
  token: localStorage.getItem('token'),
  userRole: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials(state, action: PayloadAction<{ token: string; userRole?: 'user' | 'admin' }>) {
      state.token = action.payload.token;
      if (action.payload.userRole) state.userRole = action.payload.userRole;
      localStorage.setItem('token', action.payload.token);
    },
    logout(state) {
      state.token = null;
      state.userRole = null;
      localStorage.removeItem('token');
    },
  },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;
