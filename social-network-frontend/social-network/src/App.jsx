import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Login from "./components/forms/login/Login";
import Register from "./components/forms/register/Register";
import MainPage from "./components/main-page/main-page.jsx";
import ProfilePage from "./components/profile-page/profile-page.jsx";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/home" element={<MainPage />} />
                <Route path="/profile" element={<ProfilePage />} />
                <Route path="*" element={<Login />} /> {/* Default route */}
            </Routes>
        </Router>
    );
}

export default App;

