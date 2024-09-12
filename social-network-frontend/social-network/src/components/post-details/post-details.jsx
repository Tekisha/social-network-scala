import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import Post from '../post/Post';
import Comment from '../comment/comment.jsx';
import CreatePost from '../forms/create-post/create-post.jsx';
import Navbar from '../navbar/navbar.jsx';
import './post-details.css';

function PostDetails({ postId }) {
    const location = useLocation();
    const loggedInUserId = location.state.loggedInUserId;

    const [post, setPost] = useState(null);
    const [comments, setComments] = useState([]);

    const fetchPostDetails = async () => {
        const mockPost = {
            id: postId,
            user: 'JohnDoe',
            content: 'This is a sample post content.',
            likes: 45,
            comments: 2,
            likedByMe: false,
            timestamp: new Date(),
        };

        const mockComments = [
            { id: 1, user: 'User1', content: 'Great post!', likes: 5, comments: 0, likedByMe: false, timestamp: new Date(), replies: [] },
            { id: 2, user: 'User2', content: 'Thanks for sharing!', likes: 3, comments: 0, likedByMe: false, timestamp: new Date(), replies: [] },
        ];

        setPost(mockPost);
        setComments(mockComments);
    };

    const handleCommentSubmit = async (commentText) => {
        const newCommentObj = { id: comments.length + 1, user: 'CurrentUser', content: commentText, likes: 0, comments: 0, likedByMe: false, timestamp: new Date(), replies: [] };
        setComments([...comments, newCommentObj]);
    };

    const handleLike = (postId) => {
        setPost((prevPost) => ({
            ...prevPost,
            likedByMe: !prevPost.likedByMe,
            likes: prevPost.likedByMe ? prevPost.likes - 1 : prevPost.likes + 1,
        }));
    };

    useEffect(() => {
        fetchPostDetails();
    }, [postId]);

    if (!post) {
        return <div>Loading...</div>;
    }

    return (
        <div className="post-details-wrapper">
            <Navbar />
            <div className="post-details-container">
                <Post post={post} loggedInUserId={loggedInUserId} handleLike={handleLike} />

                <CreatePost onSubmit={handleCommentSubmit} placeholder="Write a comment..." />

                <div className="comments-section">
                    <h3>Comments</h3>
                    {comments.length === 0 ? (
                        <p>No comments yet.</p>
                    ) : (
                        comments.map(comment => (
                            <Comment key={comment.id} comment={comment} handleLike={handleLike} handleReplySubmit={handleCommentSubmit} />
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}

export default PostDetails;
