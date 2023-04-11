import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, expect, it } from "vitest";
import {App} from "./App";

describe('app', () => {
  it('should render', () => {
    render(<App />);
        expect(screen.getByText("Hello World!")).toBeInTheDocument();
  });
});

