import React from 'react';
import './Navbar.css';
import logo from '/src/assets/logo.png';
import {useLocation} from "react-router-dom";

function Navbar({ loggedInUserId }) {

    const location = useLocation();

    const getActiveClass = (path) => {
        if (path === "/profile" && location.pathname.includes(`/profile/${loggedInUserId}`)) {
            return "active-nav-item";
        }
        return location.pathname === path ? "active-nav-item" : "";
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
                    <a href="/login"><i className="fas fa-sign-out-alt"></i> Logout</a>
                </nav>
            </header>
    );
}

export default Navbar;
