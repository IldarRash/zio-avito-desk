const priceFormatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
});

export function formatPrice(value: number): string {
    return priceFormatter.format(value);
}

const ACCENT_HUES: readonly number[] = [18, 142, 205, 262, 330, 42];

/**
 * Deterministic soft gradient + initial for an item that has no image.
 * The same id always yields the same colours.
 */
export function placeholderStyle(seed: string): React.CSSProperties {
    let hash = 0;
    for (let i = 0; i < seed.length; i += 1) {
        hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
    }
    const hue = ACCENT_HUES[hash % ACCENT_HUES.length];
    const hue2 = (hue + 28) % 360;
    return {
        backgroundImage: `linear-gradient(135deg, hsl(${hue} 62% 88%), hsl(${hue2} 58% 78%))`,
        color: `hsl(${hue} 55% 28%)`,
    };
}

export function initialOf(name: string): string {
    const trimmed = name.trim();
    return trimmed.length > 0 ? trimmed.charAt(0).toUpperCase() : '?';
}
