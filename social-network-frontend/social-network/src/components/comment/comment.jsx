import React, { useState } from 'react';
import CreatePost from '../forms/create-post/create-post.jsx';
import "./comment.css";
import { Link } from 'react-router-dom';

function Comment({ comment, postId, token }) {
    const [showReplies, setShowReplies] = useState(false);
    const [replies, setReplies] = useState(comment.replies || []);
    const [replying, setReplying] = useState(false);
    const [error, setError] = useState(null);

    const toggleReplies = () => {
        setShowReplies(!showReplies);
    };

    const handleReply = async (replyText) => {
        setError(null);
        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts/${postId}/comments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({
                    content: replyText,
                    parentCommentId: comment.comment.id,
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to post reply');
            }

            const data = await response.json();
            const newReply = {
                comment: data.comment,
                username: data.username,
                profilePhoto: data.profilePhoto,
                replies: [],
            };
            setReplies([...replies, newReply]);
            setReplying(false);
        } catch (error) {
            setError(error.message);
        }
    };

    return (
        <div className="comment">
            <div className="comment-header">
                <div className="comment-header-left">
                    <Link to={`/profile/${comment.comment.userId}`} className="comment-profile-link">
                        <img
                            src={`${import.meta.env.VITE_BACKEND_URL}${comment.profilePhoto || "/assets/images/default-user.png"}`}
                            alt={`${comment.username}'s profile`}
                            className="comment-user-photo"
                        />
                    </Link>
                    <span className="comment-user">
                        <Link to={`/profile/${comment.comment.userId}`} className="comment-user-link">
                            {comment.username}
                        </Link>
                    </span>
                </div>
                <span className="comment-time">
                    {new Date(comment.comment.createdAt).toLocaleString()}
                </span>
            </div>
            <p className="comment-content">{comment.comment.content}</p>
            <div className="comment-actions">
                <div onClick={toggleReplies} className="reply-button">
                    <i className="fas fa-reply"></i>
                    <span>{showReplies ? `Hide Replies (${replies.length})` : `Show Replies (${replies.length})`}</span>
                </div>
                <div onClick={() => setReplying(!replying)} className="reply-button">
                    <i className="fas fa-comment"></i>
                    <span>Reply</span>
                </div>
            </div>

            {replying && (
                <div className="create-reply">
                    <CreatePost onSubmit={handleReply} placeholder="Write a reply..." />
                    {error && <p className="error-message">{error}</p>}
                </div>
            )}

            {showReplies && (
                <div className="replies">
                    {replies.map(reply => (
                        <Comment key={reply.comment.id} comment={reply} postId={postId} token={token} />
                    ))}
                </div>
            )}
        </div>
    );
}

export default Comment;
