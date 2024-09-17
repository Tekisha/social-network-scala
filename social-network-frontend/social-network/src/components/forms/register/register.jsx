import React, {useEffect, useState} from "react";
import { useNavigate } from "react-router-dom";
import '../forms.css';
import {isAuthenticated} from "../../../utils/auth.js";

function Register() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        if (isAuthenticated()) {
            navigate("/home");
        }
    }, [navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");
        setSuccessMessage("");

        if (password !== confirmPassword) {
            setLoading(false);
            setError("Passwords do not match.");
            return;
        }

        const requestBody = {
            username,
            password,
        };

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/register`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(requestBody),
            });

            const data = await response.json();

            if (response.ok) {
                setSuccessMessage("Registration successful!");
                setTimeout(() => {
                    navigate("/login");
                }, 2000);
            } else {
                setError(data.message || "Registration failed. Please try again.");
            }
        } catch (error) {
            setError("Something went wrong. Please try again later.");
        }

        setLoading(false);
    };

    return (
        <div className="form-page-container">
            <div className="left-container">
                <img src="/src/assets/register.png" alt="Register Preview" className="preview-image" />
            </div>
            <div className="right-container">
                <form onSubmit={handleSubmit} className="form-container">
                    <h2 className=".form-title">Register</h2>

                    {error && <p className="error-message">{error}</p>}
                    {successMessage && <p className="success-message">{successMessage}</p>}

                    <div className="input-group">
                        <label>Username</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="Enter your username"
                            required
                        />
                    </div>

                    <div className="input-group">
                        <label>Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Enter your password"
                            required
                        />
                    </div>

                    <div className="input-group">
                        <label>Confirm Password</label>
                        <input
                            type="password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            placeholder="Confirm your password"
                            required
                        />
                    </div>

                    <button type="submit" className="login-button" disabled={loading}>
                        {loading ? <div className="spinner"></div> : "Sign Up"}
                    </button>

                    <p className="form-redirect">
                        Already have an account? <a href="/login">Log in</a>
                    </p>
                </form>
            </div>
        </div>
    );
}

export default Register;
