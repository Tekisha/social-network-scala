import React from 'react';
import './Navbar.css';
import logo from '/src/assets/logo.png';
import { useLocation, useNavigate } from "react-router-dom";
import { decodeJWT } from '../../utils/jwtUtils';

function Navbar() {
    const location = useLocation();
    const navigate = useNavigate();

    const token = localStorage.getItem('token');
    const decodedToken = token ? decodeJWT(token) : null;
    const loggedInUserId = decodedToken ? decodedToken.userId : null;

    const getActiveClass = (path) => {
        if (path === "/profile" && location.pathname.includes(`/profile/${loggedInUserId}`)) {
            return "active-nav-item";
        }
        return location.pathname === path ? "active-nav-item" : "";
    };

    const handleLogout = () => {
        localStorage.removeItem("token");
        navigate("/login");
    };

    return (
        <header className="navbar">
            <div className="logo-container">
                <img src={logo} alt="MySocialApp Logo" className="site-logo" />
                <h2 className="site-title">SocialNetwork</h2>
            </div>
            <nav>
                <a href="/home" className={getActiveClass("/home")}><i className="fas fa-home"></i> Home</a>
                <a href={`/profile/${loggedInUserId}`} className={getActiveClass("/profile")}><i
                    className="fas fa-user"></i> Profile</a>
                <a href="/search" className={getActiveClass("/search")}><i className="fas fa-search"></i> Search</a>
                <a href="/friend-requests" className={getActiveClass("/friend-requests")}><i className="fas fa-user-friends"></i> Requests</a>
                <a href="#" onClick={handleLogout}><i className="fas fa-sign-out-alt"></i> Logout</a>
            </nav>
        </header>
    );
}

export default Navbar;
