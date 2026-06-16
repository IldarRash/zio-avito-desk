import React, {createContext, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {LoginRequest, RegisterRequest, User} from '../types/api';
import {api} from '../services/api';

interface AuthContextValue {
    user: User | null;
    /** True while the initial GET /auth/me hydration is in flight. */
    loading: boolean;
    login: (body: LoginRequest) => Promise<void>;
    register: (body: RegisterRequest) => Promise<void>;
    logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({children}: {children: React.ReactNode}) {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        let active = true;
        api.me()
            .then(u => {
                if (active) setUser(u);
            })
            .catch(() => {
                // 401 means logged-out; network errors are also treated as
                // logged-out for hydration purposes.
                if (active) setUser(null);
            })
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, []);

    const login = useCallback(async (body: LoginRequest): Promise<void> => {
        setUser(await api.login(body));
    }, []);

    const register = useCallback(async (body: RegisterRequest): Promise<void> => {
        setUser(await api.register(body));
    }, []);

    const logout = useCallback(async (): Promise<void> => {
        await api.logout();
        setUser(null);
    }, []);

    const value = useMemo<AuthContextValue>(
        () => ({user, loading, login, register, logout}),
        [user, loading, login, register, logout],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
    const ctx = useContext(AuthContext);
    if (!ctx) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return ctx;
}
