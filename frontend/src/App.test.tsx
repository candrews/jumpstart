import React from 'react';
import { shallow } from 'enzyme';
import {App} from "./App";

describe('app', () => {
  it('should render', () => {
    const wrapper = shallow(<App />);
    expect(wrapper.exists({id:'helloWorld'})).toEqual(true);
  });
});

