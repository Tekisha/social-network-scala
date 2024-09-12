import React, { useState } from 'react';
import CreatePost from '../forms/create-post/create-post.jsx';
import "./comment.css";

function Comment({ comment, handleLike, handleReplySubmit }) {
    const [showReplies, setShowReplies] = useState(false);
    const [replies, setReplies] = useState(comment.replies || []);
    const [replying, setReplying] = useState(false);

    const toggleReplies = () => {
        setShowReplies(!showReplies);
    };

    const handleReply = (replyText) => {
        const newReply = {
            id: replies.length + 1,
            user: 'CurrentUser',
            content: replyText,
            likes: 0,
            comments: 0,
            likedByMe: false,
            timestamp: new Date(),
            replies: []
        };
        setReplies([...replies, newReply]);
        setReplying(false);
    };

    return (
        <div className="comment">
            <div className="comment-header">
                <span className="comment-user">{comment.user}</span>
                <span className="comment-time">{new Date(comment.timestamp).toLocaleString()}</span>
            </div>
            <p className="comment-content">{comment.content}</p>
            <div className="comment-actions">
                <div onClick={() => handleLike(comment.id)} className={`like-button ${comment.likedByMe ? 'liked' : ''}`}>
                    <i className="fas fa-thumbs-up"></i>
                    <span className="like-count">{comment.likes}</span>
                </div>
                <div onClick={toggleReplies} className="reply-button">
                    <i className="fas fa-reply"></i>
                    <span>{showReplies ? `Hide Replies (${replies.length})` : `Show Replies (${replies.length})`}</span>
                </div>
                <div onClick={() => setReplying(!replying)} className="reply-button">
                    <i className="fas fa-comment"></i>
                    <span>Reply</span>
                </div>
            </div>

            <CreatePost onSubmit={handleReplySubmit} placeholder="Write a reply..." />

            {showReplies && (
                <div className="replies">
                    {replies.map(reply => (
                        <Comment
                            key={reply.id}
                            comment={reply}
                            handleLike={handleLike}
                            handleReplySubmit={handleReplySubmit}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}

export default Comment;
