import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Post from '../post/Post';
import Comment from '../comment/comment.jsx';
import CreatePost from '../forms/create-post/create-post.jsx';
import Navbar from '../navbar/navbar.jsx';
import './post-details.css';

function PostDetails() {
    const { postId } = useParams();
    const [post, setPost] = useState(null);
    const [comments] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchPostDetails = async () => {
        const token = localStorage.getItem('token');

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
                    user: 'User' + data.post.userId,
                    content: data.post.content,
                    likes: data.likeCount,
                    likedByMe: data.likedByMe,
                    comments: data.commentCount,
                    timestamp: data.post.createdAt,
                };
                setPost(postDetails);
                setLoading(false);
            } else {
                console.error('Error fetching post details:', data.message);
            }
        } catch (error) {
            console.error('Error:', error);
        }
    };

    useEffect(() => {
        fetchPostDetails();
    }, [postId]);

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <div className="post-details-wrapper">
            <Navbar />
            <div className="post-details-container">
                <Post post={post} />

                <CreatePost placeholder="Write a comment..." />

                <div className="comments-section">
                    <h3>Comments</h3>
                    {comments.length === 0 ? (
                        <p>No comments yet.</p>
                    ) : (
                        comments.map(comment => (
                            <Comment key={comment.id} comment={comment} />
                        ))
                    )}
                </div>
            </div>
        </div>
    );
}

export default PostDetails;
