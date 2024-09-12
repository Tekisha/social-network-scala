import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './post.css';

function Post({ post, loggedInUserId }) {
    const navigate = useNavigate();
    const [liked, setLiked] = useState(post.likedByMe);
    const [likesCount, setLikesCount] = useState(post.likes);

    const formatTime = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleDateString() + ' at ' + date.toLocaleTimeString();
    };

    const handlePostClick = () => {
        navigate(`/post/${post.id}`, { state: { loggedInUserId } });
    };

    const handleLike = (e) => {
        e.stopPropagation();
        setLiked(!liked);
        setLikesCount(liked ? likesCount - 1 : likesCount + 1);
    };

    const handleEdit = (e) => {
        e.stopPropagation();
        console.log("Edit post", post.id);
    };

    const handleDelete = (e) => {
        e.stopPropagation();
        console.log("Delete post", post.id);
    };

    return (
        <div className="post" onClick={handlePostClick}>
            <div className="post-header">
                <div className="user-info">
                    <img src="/src/assets/user-icon.png" alt="User" className="post-user-icon" />
                    <span className="post-user">{post.user}</span>
                </div>
                <span className="post-time">{formatTime(post.timestamp)}</span>
            </div>
            <p className="post-content">{post.content}</p>
            <div className="post-actions">
                <div onClick={handleLike} className={`like-button ${liked ? 'liked' : ''}`}>
                    <i className="fas fa-thumbs-up"></i>
                    <span className="like-count">{likesCount}</span>
                </div>
                <div className="comment-button">
                    <i className="fas fa-comment"></i>
                    <span className="comment-count">{post.comments}</span>
                </div>
                {post.userId === loggedInUserId && (
                    <div className="post-actions-extra">
                        <button className="edit-post-button" onClick={handleEdit}>
                            <i className="fas fa-edit"></i>
                        </button>
                        <button className="delete-post-button" onClick={handleDelete}>
                            <i className="fas fa-trash"></i>
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}

export default Post;
