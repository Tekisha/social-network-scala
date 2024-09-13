import React, { useState } from 'react';
import CreatePost from '../forms/create-post/create-post.jsx';
import "./comment.css";

function Comment({ comment }) {
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
                <span className="comment-user">User {comment.comment.id}</span>
                <span className="comment-time">{new Date(comment.comment.createdAt).toLocaleString()}</span>
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

            {replying && <CreatePost onSubmit={handleReply} placeholder="Write a reply..." />}

            {showReplies && (
                <div className="replies">
                    {replies.map(reply => (
                        <Comment key={reply.comment.id} comment={reply} />
                    ))}
                </div>
            )}
        </div>
    );
}

export default Comment;
