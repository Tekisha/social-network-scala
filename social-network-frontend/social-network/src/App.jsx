import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Login from "./components/forms/login/Login";
import Register from "./components/forms/register/Register";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="*" element={<Login />} /> {/* Default route */}
            </Routes>
        </Router>
    );
}

export default App;
