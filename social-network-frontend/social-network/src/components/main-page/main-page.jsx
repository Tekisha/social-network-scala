import React, { useState, useEffect } from "react";
import Navbar from '../navbar/navbar.jsx';
import PostFeed from '../post-feed/post-feed.jsx';
import CreatePost from '../forms/create-post/create-post.jsx';
import './main-page.css';

function MainPage() {
    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        setLoading(true);
        setTimeout(() => {
            const dummyPosts = [
                {
                    id: 1,
                    user: "JohnDoe",
                    content: "Had a great day today!",
                    likes: 10,
                    likedByMe: false,
                    timestamp: new Date().toISOString(),
                },
                {
                    id: 2,
                    user: "JaneSmith",
                    content: "Loving this new app!",
                    likes: 5,
                    likedByMe: true,
                    timestamp: new Date().toISOString(),
                },
            ];
            setPosts(dummyPosts);
            setLoading(false);
        }, 1000);
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

    const handleCreatePost = (newPostContent) => {
        const newPost = {
            id: posts.length + 1,
            user: "CurrentUser",
            content: newPostContent,
            likes: 0,
            likedByMe: false,
            timestamp: new Date().toISOString(),
        };
        setPosts([newPost, ...posts]);
    };

    return (
        <div className="main-page-wrapper">
            <Navbar />
            <div className="main-page-container">
                <CreatePost onSubmit={handleCreatePost} />
                {loading ? <div className="spinner"></div> : <PostFeed posts={posts} handleLike={handleLike} />}
            </div>
        </div>
    );
}

export default MainPage;
