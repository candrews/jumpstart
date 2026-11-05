import './App.css';

import Typography from '@mui/material/Typography';

export const App: React.FC = () => {
    return (
        <Typography
            id="helloWorld"
            variant="h1"
            component="h1"
            color="textPrimary"
        >
            Hello World!
        </Typography>
    );
};
