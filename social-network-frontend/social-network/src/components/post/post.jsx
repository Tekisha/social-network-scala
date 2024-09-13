import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { decodeJWT } from '../../utils/jwtUtils';
import './post.css';

function Post({ post }) {
    const navigate = useNavigate();
    const token = localStorage.getItem("token");
    const decodedToken = decodeJWT(token);
    const loggedInUserId = decodedToken.userId;

    const [liked, setLiked] = useState(post.likedByMe);
    const [likesCount, setLikesCount] = useState(post.likes);

    const formatTime = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleDateString() + ' at ' + date.toLocaleTimeString();
    };

    const handlePostClick = () => {
        navigate(`/post/${post.id}`);
    };

    const handleProfileClick = (e) => {
        e.stopPropagation();
        navigate(`/profile/${post.userId}`);
    };

    const handleLike = async (e) => {
        e.stopPropagation();

        try {
            const endpoint = liked
                ? `${import.meta.env.VITE_BACKEND_URL}/posts/${post.id}/unlike`
                : `${import.meta.env.VITE_BACKEND_URL}/posts/${post.id}/like`;

            const method = liked ? "DELETE" : "POST";

            const response = await fetch(endpoint, {
                method,
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
            });

            if (response.ok) {
                setLiked(!liked);
                setLikesCount(liked ? likesCount - 1 : likesCount + 1);
            } else {
                const data = await response.json();
                console.error("Failed to like/unlike the post:", data.message);
            }
        } catch (error) {
            console.error("Error:", error);
        }
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
                <div className="user-info" onClick={handleProfileClick}>
                    <img src={`${import.meta.env.VITE_BACKEND_URL}${post.profilePhoto || "/assets/images/default-user.png"}`}
                         alt="User" className="post-user-icon" />
                    <span className="post-user">{post.username}</span>
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
