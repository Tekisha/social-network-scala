import React from "react";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Login from "./components/forms/login/Login";
import Register from "./components/forms/register/Register";
import MainPage from "./components/main-page/main-page.jsx";
import ProfilePage from "./components/profile-page/profile-page.jsx";
import SearchPage from "./components/search-page/search-page.jsx";
import PostDetails from "./components/post-details/post-details.jsx";
import FriendRequestsPage from "./components/friend-requests-page/friend-requests-page.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";

function App() {
    return (
        <Router>
            <Routes>
                {/* Public routes */}
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />

                {/* Protected routes */}
                <Route path="/home" element={<ProtectedRoute element={MainPage} />} />
                <Route path="/profile/:userId" element={<ProtectedRoute element={ProfilePage} />} />
                <Route path="/search" element={<ProtectedRoute element={SearchPage} />} />
                <Route path="/post/:postId" element={<ProtectedRoute element={PostDetails} />} />
                <Route path="/friend-requests" element={<ProtectedRoute element={FriendRequestsPage} />} />

                {/* Default route */}
                <Route path="*" element={<Login />} />
            </Routes>
        </Router>
    );
}

export default App;

