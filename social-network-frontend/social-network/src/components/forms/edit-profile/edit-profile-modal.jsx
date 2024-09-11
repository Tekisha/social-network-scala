import React, { useState } from 'react';
import './edit-profile-modal.css';

function EditProfileModal({ userInfo, onClose, onSaveBasicInfo, onSavePassword, onSaveProfilePic }) {
    const [username, setUsername] = useState(userInfo.username);
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [currentPassword, setCurrentPassword] = useState('');
    const [profilePic, setProfilePic] = useState(null);
    const [selectedFileName, setSelectedFileName] = useState('No file chosen');
    const [error, setError] = useState(null);

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        setProfilePic(file);
        setSelectedFileName(file ? file.name : 'No file chosen');
    };

    const handleSaveBasicInfo = () => {
        if (username) {
            onSaveBasicInfo({ username });
        }
        onClose();
    };

    const handleSavePassword = () => {
        if (password !== confirmPassword) {
            setError("New passwords do not match");
            return;
        }
        onSavePassword({ currentPassword, newPassword: password });
        onClose();
    };

    const handleSaveProfilePic = () => {
        onSaveProfilePic({ profilePic });
        onClose();
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
