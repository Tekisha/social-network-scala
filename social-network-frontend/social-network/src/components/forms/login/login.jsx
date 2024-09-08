import React, { useState } from "react";
import '../forms.css';

function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");

        setTimeout(() => {
            if (email === "test" && password === "password") {
                console.log("Logged in successfully");
                setLoading(false);
            } else {
                setLoading(false);
                setError("Invalid credentials, please try again.");
            }
        }, 2000);
    };

    return (
        <div className="form-page-container">
            <div className="left-container">
                <img src="/src/assets/login.png" alt="Login Preview" className="preview-image" />
            </div>
            <div className="right-container">
                <form onSubmit={handleSubmit} className="form-container">
                    <h2 className=".form-title">Login</h2>

                    {error && <p className="error-message">{error}</p>}

                    <div className="input-group">
                        <label>Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="Enter your email"
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
