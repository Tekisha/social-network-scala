import React, { useState, useEffect } from "react";
import Navbar from '../navbar/navbar.jsx';
import PostFeed from '../post-feed/post-feed.jsx';
import CreatePost from '../forms/create-post/create-post.jsx';
import { decodeJWT } from '../../utils/jwtUtils';
import './main-page.css';

function MainPage() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
    }, []);

    const handleLike = (postId) => {
        setPosts(
            posts.map((post) =>
                post.id === postId
                    ? {
                        ...post,
                        likedByMe: !post.likedByMe,
                        likes: post.likedByMe ? post.likes - 1 : post.likes + 1,
                    }
                    : post
            )
        );
    };

    const handleCreatePost = async (newPostContent) => {
        const token = localStorage.getItem("token");
        const decodedToken = decodeJWT(token);
        const username = decodedToken.username;

        const requestBody = {
            content: newPostContent,
        };

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify(requestBody),
            });

            const data = await response.json();

            if (response.ok) {
                const newPost = {
                    id: data.post.id,
                    user: username,
                    content: data.post.content,
                    likes: data.likeCount,
                    likedByMe: data.likedByMe,
                    timestamp: data.post.createdAt,
                    comments: data.commentCount,
                };
                setPosts([newPost, ...posts]);
            } else {
                console.error("Failed to create post", data.message);
            }
        } catch (error) {
            console.error("Error:", error);
        }
    };

    return (
        <div className="main-page-wrapper">
            <Navbar />
            <div className="main-page-container">
                <CreatePost onSubmit={handleCreatePost} placeholder="What's on your mind?" />
                {loading ? <div className="spinner"></div> : <PostFeed posts={posts} handleLike={handleLike} />}
            </div>
        </div>
    );
}

export default MainPage;
