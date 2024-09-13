import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Login from "./components/forms/login/Login";
import Register from "./components/forms/register/Register";
import MainPage from "./components/main-page/main-page.jsx";
import ProfilePage from "./components/profile-page/profile-page.jsx";
import SearchPage from "./components/search-page/search-page.jsx";
import PostDetails from "./components/post-details/post-details.jsx";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/home" element={<MainPage />} />
                <Route path="/profile/:userId" element={<ProfilePage />} />
                <Route path="/search" element={<SearchPage />} />
                <Route path="/post/:postId" element={<PostDetails/>} />
                <Route path="*" element={<Login />} /> {/* Default route */}
            </Routes>
        </Router>
    );
}

export default App;

