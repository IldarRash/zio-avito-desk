import React, {useEffect} from 'react';

interface ModalProps {
    title: string;
    onClose: () => void;
    children: React.ReactNode;
    className?: string;
}

function Modal({title, onClose, children, className}: ModalProps) {
    useEffect(() => {
        const onKeyDown = (e: KeyboardEvent): void => {
            if (e.key === 'Escape') {
                onClose();
            }
        };
        document.addEventListener('keydown', onKeyDown);
        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.removeEventListener('keydown', onKeyDown);
            document.body.style.overflow = previousOverflow;
        };
    }, [onClose]);

    return (
        <div className="overlay" onClick={onClose}>
            <div
                className={`modal${className ? ` ${className}` : ''}`}
                role="dialog"
                aria-modal="true"
                aria-label={title}
                onClick={e => e.stopPropagation()}
            >
                <button
                    type="button"
                    className="modal__close"
                    onClick={onClose}
                    aria-label="Close dialog"
                >
                    ×
                </button>
                {children}
            </div>
        </div>
    );
}

export default Modal;
