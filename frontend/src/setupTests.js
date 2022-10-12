import Enzyme from "enzyme/build";
import Adapter from "enzyme-adapter-react-16/build";
import 'jest-enzyme';
import nock from 'nock';

Enzyme.configure({adapter: new Adapter()});
nock.disableNetConnect();
