import React from 'react';
import './Post.css';

function Post({ post, handleLike }) {

    const formatTime = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleDateString() + ' at ' + date.toLocaleTimeString();
    };

    return (
        <div className="post">
            <div className="post-header">
                <div className="user-info">
                    <img src="/src/assets/user-icon.png" alt="User" className="post-user-icon" />
                    <span className="post-user">{post.user}</span>
                </div>
                <span className="post-time">{formatTime(post.timestamp)}</span>
            </div>
            <p className="post-content">{post.content}</p>
            <div className="post-actions">
                <div onClick={() => handleLike(post.id)} className={`like-button ${post.likedByMe ? 'liked' : ''}`}>
                    <i className="fas fa-thumbs-up"></i>
                    <span className="like-count">{post.likes}</span>
                </div>
            </div>
        </div>
    );
}

export default Post;
