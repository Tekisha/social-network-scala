import React, { useState, useEffect } from "react";
import Navbar from '../navbar/navbar.jsx';
import PostFeed from '../post-feed/post-feed.jsx';
import CreatePost from '../forms/create-post/create-post.jsx';
import './main-page.css';

function MainPage() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchPosts = async () => {
            const token = localStorage.getItem("token");

            try {
                const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts/friends`, {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${token}`,
                    },
                });

                const data = await response.json();

                if (response.ok) {
                    const formattedPosts = data.map((item) => ({
                        id: item.post.id,
                        username: item.username,
                        content: item.post.content,
                        likes: item.likeCount,
                        likedByMe: item.likedByMe,
                        timestamp: item.post.createdAt,
                        comments: item.commentCount,
                        userId: item.post.userId,
                        profilePhoto: item.profilePhoto,
                    }));
                    setPosts(formattedPosts);
                } else {
                    console.error("Failed to fetch posts", data.message);
                }
            } catch (error) {
                console.error("Error:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchPosts();
    }, []);

    const handleCreatePost = async (newPostContent) => {
        const token = localStorage.getItem("token");

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
                    username: data.username,
                    content: data.post.content,
                    likes: data.likeCount,
                    likedByMe: data.likedByMe,
                    timestamp: data.post.createdAt,
                    comments: data.commentCount,
                    userId: data.post.userId,
                    profilePhoto: data.profilePhoto,
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
                {loading ? <div className="spinner"></div> : <PostFeed posts={posts} />}
            </div>
        </div>
    );
}

export default MainPage;
