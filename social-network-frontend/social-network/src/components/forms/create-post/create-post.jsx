import React, { useState } from 'react';
import './create-post.css';

function CreatePost({ onSubmit, placeholder }) {
    const [newPost, setNewPost] = useState("");

    const handleSubmit = (e) => {
        e.preventDefault();
        if (newPost.trim() === "") return;
        onSubmit(newPost);
        setNewPost("");  // Clear input after submission
    };

    return (
        <form onSubmit={handleSubmit} className="create-post-form">
            <div className="create-post-input-container">
                <textarea
                    value={newPost}
                    onChange={(e) => setNewPost(e.target.value)}
                    placeholder={placeholder}  // Set custom placeholder
                    rows="3"
                    className="create-post-textarea"
                ></textarea>
                <button type="submit" className="submit-button">
                    <i className="fas fa-paper-plane"></i> {/* Send icon */}
                </button>
            </div>
        </form>
    );
}

export default CreatePost;
