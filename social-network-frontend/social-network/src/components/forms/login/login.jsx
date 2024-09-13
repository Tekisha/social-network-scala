import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import '../forms.css';

function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");

        const requestBody = {
            username,
            password,
        };

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(requestBody),
            });

            const data = await response.json();

            if (response.ok) {
                console.log("Logged in successfully");
                localStorage.setItem("token", data.token);

                navigate("/home");
            } else {
                setError(data.message || "Invalid credentials, please try again.");
            }
        } catch (error) {
            setError("Something went wrong. Please try again later.");
        }

        setLoading(false);
    };

    return (
        <div className="form-page-container">
            <div className="left-container">
                <img src="/src/assets/login.png" alt="Login Preview" className="preview-image" />
            </div>
            <div className="right-container">
                <form onSubmit={handleSubmit} className="form-container">
                    <h2 className="form-title">Login</h2>

                    {error && <p className="error-message">{error}</p>}

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

                    <button type="submit" className="login-button" disabled={loading}>
                        {loading ? <div className="spinner"></div> : "Log in"}
                    </button>

                    <p className="form-redirect">
                        Don't have an account? <a href="/register">Sign up</a>
                    </p>
                </form>
            </div>
        </div>
    );
}

export default Login;
