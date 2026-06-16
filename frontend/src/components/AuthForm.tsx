import React, {useState} from 'react';
import Modal from './Modal';
import {useAuth} from '../context/AuthContext';

export type AuthMode = 'login' | 'register';

interface AuthFormProps {
    mode: AuthMode;
    onClose: () => void;
    onSwitchMode: (mode: AuthMode) => void;
}

interface FieldErrors {
    email?: string;
    password?: string;
    displayName?: string;
}

function AuthForm({mode, onClose, onSwitchMode}: AuthFormProps) {
    const {login, register} = useAuth();
    const [email, setEmail] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [displayName, setDisplayName] = useState<string>('');
    const [submitting, setSubmitting] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

    const isRegister = mode === 'register';
    const title = isRegister ? 'Create your account' : 'Welcome back';

    const validate = (): FieldErrors => {
        const errors: FieldErrors = {};
        if (!email.trim()) {
            errors.email = 'Enter your email';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
            errors.email = 'Enter a valid email';
        }
        if (!password) {
            errors.password = 'Enter your password';
        } else if (isRegister && password.length < 8) {
            errors.password = 'Password must be at least 8 characters';
        }
        if (isRegister && !displayName.trim()) {
            errors.displayName = 'Pick a display name';
        }
        return errors;
    };

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>): Promise<void> => {
        e.preventDefault();
        setError(null);
        const errors = validate();
        setFieldErrors(errors);
        if (Object.keys(errors).length > 0) {
            return;
        }
        setSubmitting(true);
        try {
            if (isRegister) {
                await register({email: email.trim(), password, displayName: displayName.trim()});
            } else {
                await login({email: email.trim(), password});
            }
            onClose();
        } catch (err: unknown) {
            setError(err instanceof Error ? err.message : 'Something went wrong');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Modal title={title} onClose={onClose} className="modal--form">
            <h2 className="form__title">{title}</h2>
            <p className="form__subtitle">
                {isRegister
                    ? 'Sign up to post and manage your own ads.'
                    : 'Log in to post and manage your ads.'}
            </p>

            {error && <div className="banner banner--error" role="alert">{error}</div>}

            <form className="form" onSubmit={handleSubmit} noValidate>
                {isRegister && (
                    <div className="field">
                        <label className="field__label" htmlFor="auth-name">Display name</label>
                        <input
                            id="auth-name"
                            type="text"
                            className={`field__input${fieldErrors.displayName ? ' is-invalid' : ''}`}
                            value={displayName}
                            onChange={e => setDisplayName(e.target.value)}
                            placeholder="Jane Doe"
                            autoComplete="name"
                        />
                        {fieldErrors.displayName && (
                            <span className="field__error">{fieldErrors.displayName}</span>
                        )}
                    </div>
                )}

                <div className="field">
                    <label className="field__label" htmlFor="auth-email">Email</label>
                    <input
                        id="auth-email"
                        type="email"
                        className={`field__input${fieldErrors.email ? ' is-invalid' : ''}`}
                        value={email}
                        onChange={e => setEmail(e.target.value)}
                        placeholder="you@example.com"
                        autoComplete="email"
                    />
                    {fieldErrors.email && <span className="field__error">{fieldErrors.email}</span>}
                </div>

                <div className="field">
                    <label className="field__label" htmlFor="auth-password">Password</label>
                    <input
                        id="auth-password"
                        type="password"
                        className={`field__input${fieldErrors.password ? ' is-invalid' : ''}`}
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        placeholder={isRegister ? 'At least 8 characters' : 'Your password'}
                        autoComplete={isRegister ? 'new-password' : 'current-password'}
                    />
                    {fieldErrors.password && (
                        <span className="field__error">{fieldErrors.password}</span>
                    )}
                </div>

                <div className="form__actions">
                    <button type="submit" className="btn btn--primary" disabled={submitting}>
                        {submitting ? (
                            <>
                                <span className="spinner spinner--sm" aria-hidden="true" />{' '}
                                {isRegister ? 'Creating…' : 'Logging in…'}
                            </>
                        ) : isRegister ? (
                            'Create account'
                        ) : (
                            'Log in'
                        )}
                    </button>
                </div>
            </form>

            <p className="form__switch">
                {isRegister ? 'Already have an account?' : "Don't have an account?"}{' '}
                <button
                    type="button"
                    className="link-btn"
                    onClick={() => {
                        setError(null);
                        setFieldErrors({});
                        onSwitchMode(isRegister ? 'login' : 'register');
                    }}
                >
                    {isRegister ? 'Log in' : 'Sign up'}
                </button>
            </p>
        </Modal>
    );
}

export default AuthForm;
