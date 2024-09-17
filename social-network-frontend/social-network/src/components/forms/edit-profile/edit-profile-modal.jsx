import React, { useState } from 'react';
import './edit-profile-modal.css';

function EditProfileModal({ userInfo, onClose, refreshUser }) {
    const [username, setUsername] = useState(userInfo.username);
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [currentPassword, setCurrentPassword] = useState('');
    const [profilePic, setProfilePic] = useState(null);
    const [selectedFileName, setSelectedFileName] = useState('No file chosen');
    const [error, setError] = useState(null);
    const token = localStorage.getItem("token");

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        setProfilePic(file);
        setSelectedFileName(file ? file.name : 'No file chosen');
    };

    const handleSaveBasicInfo = async () => {
        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/users/me`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({ username })
            });

            if (!response.ok) {
                throw new Error('Failed to update user info');
            }

            const data = await response.json();
            localStorage.setItem("token", data.token);
            console.log("User info updated successfully:", data);
            refreshUser();
            alert("User info updated successfully!");
            onClose();
        } catch (error) {
            setError(error.message);
            console.error("Error updating user info:", error);
        }
    };

    const handleSavePassword = async () => {
        if (password !== confirmPassword) {
            setError("New passwords do not match");
            return;
        }

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/users/me/password`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({
                    oldPassword: currentPassword,
                    newPassword: password
                })
            });

            if (!response.ok) {
                throw new Error('Failed to update password');
            }

            const data = await response.json();
            console.log("Password updated successfully:", data);
            refreshUser();
            alert("Password updated successfully!");
            onClose();
        } catch (error) {
            setError(error.message);
            console.error("Error updating password:", error);
        }
    };

    const handleSaveProfilePic = async () => {
        if (!profilePic) {
            setError("Please select a profile picture to upload.");
            return;
        }

        const formData = new FormData();
        formData.append("profile_photo", profilePic);

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/users/profile-photo`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
                body: formData,
            });

            if (!response.ok) {
                throw new Error('Failed to update profile photo');
            }

            const data = await response.json();
            console.log("Profile photo updated successfully:", data);
            refreshUser();
            alert("Profile photo updated successfully!");
            onClose();
        } catch (error) {
            setError(error.message);
            console.error("Error updating profile photo:", error);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <button className="close-modal-button" onClick={onClose}>
                    <i className="fas fa-times"></i>
                </button>
                <h3>Edit Profile</h3>

                <div className="modal-body">
                    <label>Username</label>
                    <input
                        type="text"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />
                    <div className="modal-footer">
                        <button className="save-button" onClick={handleSaveBasicInfo}>Save Basic Info</button>
                    </div>
                </div>

                <div className="modal-body">
                    <label>Current Password</label>
                    <input
                        type="password"
                        value={currentPassword}
                        onChange={(e) => setCurrentPassword(e.target.value)}
                    />

                    <label>New Password</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />

                    <label>Confirm New Password</label>
                    <input
                        type="password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                    />

                    {error && <p className="error-message">{error}</p>}

                    <div className="modal-footer">
                        <button className="save-button" onClick={handleSavePassword}>Save Password</button>
                    </div>
                </div>

                <div className="modal-body">
                    <label>Upload Profile Picture</label>
                    <label className="custom-file-upload">
                        <input
                            type="file"
                            accept="image/*"
                            onChange={handleFileChange}
                        />
                        Choose File
                    </label>
                    <span className="file-name">{selectedFileName}</span>
                    <div className="modal-footer">
                        <button className="save-button" onClick={handleSaveProfilePic}>Save Profile Picture</button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default EditProfileModal;
