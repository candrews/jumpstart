import React from 'react';
import { render } from '@testing-library/react';
import {App} from "./App";

describe('app', () => {
  it('should render', () => {
    const { getByText } = render(<App />);
    expect(getByText("Hello World!")).toBeTruthy();
  });
});

