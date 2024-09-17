import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { decodeJWT } from '../../utils/jwtUtils';
import './post.css';

function Post({ post, onDelete }) {
    const navigate = useNavigate();
    const token = localStorage.getItem("token");
    const decodedToken = decodeJWT(token);
    const loggedInUserId = decodedToken.userId;

    const [liked, setLiked] = useState(post.likedByMe);
    const [likesCount, setLikesCount] = useState(post.likes);
    const [isEditing, setIsEditing] = useState(false);
    const [editedContent, setEditedContent] = useState(post.content);
    const [error, setError] = useState(null);

    const formatTime = (timestamp) => {
        const date = new Date(timestamp);
        return date.toLocaleDateString() + ' at ' + date.toLocaleTimeString();
    };

    const handlePostClick = (e) => {
        if (!isEditing) {
            navigate(`/post/${post.id}`);
        }
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
        setIsEditing(true);
    };

    const handleCancelEdit = (e) => {
        e.stopPropagation();
        setIsEditing(false);
        setEditedContent(post.content);
    };

    const handleSaveEdit = async (e) => {
        e.stopPropagation();
        if (!editedContent) {
            setError("Content cannot be empty.");
            return;
        }

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts/${post.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({ content: editedContent }),
            });

            if (!response.ok) {
                throw new Error('Failed to update post');
            }

            const data = await response.json();
            post.content = data.post.content;
            post.updatedAt = data.post.updatedAt;
            setIsEditing(false);
        } catch (error) {
            setError(error.message);
            console.error("Error updating post:", error);
        }
    };

    const handleDelete = async (e) => {
        e.stopPropagation();
        const confirmed = window.confirm("Are you sure you want to delete this post?");

        if (confirmed) {
            try {
                const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts/${post.id}`, {
                    method: 'DELETE',
                    headers: {
                        "Authorization": `Bearer ${token}`,
                    },
                });

                if (response.ok) {
                    console.log("Post deleted successfully");
                    onDelete(post.id);
                } else {
                    console.error("Failed to delete post");
                }
            } catch (error) {
                console.error("Error deleting post:", error);
            }
        }
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

            <div className="post-content" onClick={(e) => e.stopPropagation()}>
                {isEditing ? (
                    <>
                        <textarea
                            className="edit-post-textarea"
                            value={editedContent}
                            onChange={(e) => setEditedContent(e.target.value)}
                            rows="4"
                        />
                        {error && <p className="error-message">{error}</p>}
                        <div className="edit-actions">
                            <button className="save-button" onClick={handleSaveEdit}>Save</button>
                            <button className="cancel-button" onClick={handleCancelEdit}>Cancel</button>
                        </div>
                    </>
                ) : (
                    <p>{post.content}</p>
                )}
            </div>

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
                        {!isEditing && (
                            <>
                                <button className="edit-post-button" onClick={handleEdit}>
                                    <i className="fas fa-edit"></i>
                                </button>
                                <button className="delete-post-button" onClick={handleDelete}>
                                    <i className="fas fa-trash"></i>
                                </button>
                            </>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

export default Post;
