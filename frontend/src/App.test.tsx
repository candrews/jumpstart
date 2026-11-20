import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { App } from './App';

describe('app', () => {
    it('should render the greeting as a heading', () => {
        render(<App />);

        expect(
            screen.getByRole('heading', {
                level: 1,
                name: 'Hello World!',
            })
        ).toBeInTheDocument();
    });
});
