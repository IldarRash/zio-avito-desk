import React from 'react';
import {useAuth} from '../context/AuthContext';
import {AuthMode} from './AuthForm';

interface NavbarProps {
    search: string;
    onSearchChange: (value: string) => void;
    onSearchSubmit: () => void;
    onHome: () => void;
    onNewAd: () => void;
    onOpenAuth: (mode: AuthMode) => void;
}

function Navbar({search, onSearchChange, onSearchSubmit, onHome, onNewAd, onOpenAuth}: NavbarProps) {
    const {user, loading, logout} = useAuth();

    const submit = (e: React.FormEvent<HTMLFormElement>): void => {
        e.preventDefault();
        onSearchSubmit();
    };

    return (
        <header className="navbar">
            <div className="navbar__inner">
                <button type="button" className="brand" onClick={onHome} aria-label="Avito Desk home">
                    <span className="brand__mark" aria-hidden="true">A</span>
                    <span className="brand__name">Avito&nbsp;Desk</span>
                </button>

                <form className="searchbar" onSubmit={submit} role="search">
                    <span className="searchbar__icon" aria-hidden="true">⌕</span>
                    <input
                        type="search"
                        className="searchbar__input"
                        value={search}
                        onChange={e => onSearchChange(e.target.value)}
                        placeholder="Search ads…"
                        aria-label="Search ads"
                    />
                    <button type="submit" className="searchbar__submit">Search</button>
                </form>

                <div className="navbar__actions">
                    {!loading && user && (
                        <button type="button" className="btn btn--primary navbar__cta" onClick={onNewAd}>
                            <span aria-hidden="true">＋</span> New ad
                        </button>
                    )}
                    {!loading &&
                        (user ? (
                            <div className="navbar__user">
                                <span className="navbar__greeting" title={user.email}>
                                    Hi, {user.displayName}
                                </span>
                                <button type="button" className="btn btn--ghost" onClick={() => void logout()}>
                                    Log out
                                </button>
                            </div>
                        ) : (
                            <div className="navbar__user">
                                <button
                                    type="button"
                                    className="btn btn--ghost"
                                    onClick={() => onOpenAuth('login')}
                                >
                                    Log in
                                </button>
                                <button
                                    type="button"
                                    className="btn btn--primary"
                                    onClick={() => onOpenAuth('register')}
                                >
                                    Sign up
                                </button>
                            </div>
                        ))}
                </div>
            </div>
        </header>
    );
}

export default Navbar;
