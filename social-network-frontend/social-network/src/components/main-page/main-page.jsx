import React, { useState, useEffect } from "react";
import Navbar from '../navbar/navbar.jsx';
import PostFeed from '../post-feed/post-feed.jsx';
import CreatePost from '../forms/create-post/create-post.jsx';
import './main-page.css';

function MainPage() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(1);
    const [pageSize] = useState(4);
    const [loadingMore, setLoadingMore] = useState(false);
    const [hasMorePosts, setHasMorePosts] = useState(true);

    const fetchPosts = async (currentPage) => {
        const token = localStorage.getItem("token");

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts/friends?page=${currentPage}&pageSize=${pageSize}`, {
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

                setPosts((prevPosts) => [...prevPosts, ...formattedPosts]);

                if (formattedPosts.length < pageSize) {
                    setHasMorePosts(false);
                }
            } else {
                console.error("Failed to fetch posts", data.message);
            }
        } catch (error) {
            console.error("Error:", error);
        } finally {
            setLoading(false);
            setLoadingMore(false);
        }
    };

    useEffect(() => {
        fetchPosts(page);
    }, [page]);

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

                setPosts((prevPosts) => [newPost, ...prevPosts]);
            } else {
                console.error("Failed to create post", data.message);
            }
        } catch (error) {
            console.error("Error:", error);
        }
    };

    const handleDeletePost = (postId) => {
        setPosts((prevPosts) => prevPosts.filter(post => post.id !== postId));
    };

    const handleScroll = () => {
        const scrollPosition = window.innerHeight + document.documentElement.scrollTop;
        const documentHeight = document.documentElement.offsetHeight;

        if (scrollPosition >= documentHeight - 100 && !loadingMore && hasMorePosts) {
            setLoadingMore(true);
            setPage((prevPage) => prevPage + 1);
        }
    };

    useEffect(() => {
        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll);
    }, [loadingMore, hasMorePosts]);

    return (
        <div className="main-page-wrapper">
            <Navbar />
            <div className="main-page-container">
                <CreatePost onSubmit={handleCreatePost} placeholder="What's on your mind?" />
                {loading ? (
                    <div className="spinner"></div>
                ) : (
                    <PostFeed posts={posts} onDeletePost={handleDeletePost} />
                )}
                {loadingMore && <div className="spinner">Loading more posts...</div>}
            </div>
        </div>
    );
}

export default MainPage;
