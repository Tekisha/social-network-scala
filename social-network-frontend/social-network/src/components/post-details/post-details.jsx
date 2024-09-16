import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Post from '../post/Post';
import Comment from '../comment/comment.jsx';
import CreatePost from '../forms/create-post/create-post.jsx';
import Navbar from '../navbar/navbar.jsx';
import './post-details.css';
import { decodeJWT } from "../../utils/jwtUtils.js";

function PostDetails() {
    const { postId } = useParams();
    const [post, setPost] = useState(null);
    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(true);
    const token = localStorage.getItem('token');
    const decodedToken = decodeJWT(token);
    const loggedInUserId = decodedToken.userId;

    const fetchPostDetails = async () => {
        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts/${postId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            const data = await response.json();
            if (response.ok) {
                const postDetails = {
                    id: data.post.id,
                    username: data.username,
                    content: data.post.content,
                    likes: data.likeCount,
                    likedByMe: data.likedByMe,
                    comments: data.commentCount,
                    timestamp: data.post.createdAt,
                    userId: data.post.userId,
                    profilePhoto: data.profilePhoto,
                };
                setPost(postDetails);
            } else {
                console.error('Error fetching post details:', data.message);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    };

    const fetchComments = async () => {
        const token = localStorage.getItem('token');

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts/${postId}/comments?page=1&pageSize=100`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            const data = await response.json();
            if (response.ok) {
                setComments(data);
            } else {
                console.error('Error fetching comments:', data.message);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    };

    const handleCreateComment = async (commentText) => {
        const requestBody = {
            content: commentText,
            parentCommentId: null,
        };

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts/${postId}/comments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify(requestBody),
            });

            const data = await response.json();
            if (response.ok) {
                setComments((prevComments) => [...prevComments, data]);
            } else {
                console.error('Error creating comment:', data.message);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    };

    const handleDeleteComment = (commentId) => {
        setComments((prevComments) => prevComments.filter(comment => comment.comment.id !== commentId));
    };

    useEffect(() => {
        fetchPostDetails();
        fetchComments();
        setLoading(false);
    }, [postId]);

    if (loading || !post) {
        return <div>Loading...</div>;
    }

    return (
        <div className="post-details-wrapper">
            <Navbar />
            <div className="post-details-container">
                <Post post={post} />

                <CreatePost onSubmit={handleCreateComment} placeholder="Write a comment..." />

                <div className="comments-section">
                    <h3>Comments</h3>
                    {comments.length === 0 ? (
                        <p>No comments yet.</p>
                    ) : (
                        comments.map((comment) => (
                            <Comment
                                key={comment.comment.id}
                                comment={comment}
                                postId={postId}
                                token={token}
                                loggedInUserId={loggedInUserId}
                                onDeleteComment={handleDeleteComment}
                            />
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}

export default PostDetails;
