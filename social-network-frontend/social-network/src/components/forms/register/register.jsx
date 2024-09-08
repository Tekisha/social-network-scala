import React, { useState } from "react";
import '../forms.css';

function Register() {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");

        if (password !== confirmPassword) {
            setLoading(false);
            setError("Passwords do not match.");
            return;
        }

        setTimeout(() => {
            console.log("Registered successfully:", { username, email, password });
            setLoading(false);
        }, 2000);
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
