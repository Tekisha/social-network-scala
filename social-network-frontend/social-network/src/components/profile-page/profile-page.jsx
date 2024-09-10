import React, { useState, useEffect } from "react";
import Navbar from '../navbar/navbar.jsx';
import Post from '../post/post.jsx';
import './profile-page.css';

function ProfilePage() {
    const [userInfo, setUserInfo] = useState({
        username: "CurrentUser",
        email: "user@example.com",
        bio: "I love sharing my thoughts!",
        profilePic: "/src/assets/user-icon.png",
    });

    const [posts, setPosts] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        setLoading(true);
        setTimeout(() => {
            const userPosts = [
                {
                    id: 1,
                    user: "CurrentUser",
                    content: "Excited to start using this platform!",
                    likes: 8,
                    likedByMe: false,
                    timestamp: new Date().toISOString(),
                    comments: 3
                },
                {
                    id: 2,
                    user: "CurrentUser",
                    content: "Loving the features of this app!",
                    likes: 15,
                    likedByMe: true,
                    timestamp: new Date().toISOString(),
                    comments: 5
                },
            ];
            setPosts(userPosts);
            setLoading(false);
        }, 1000);
    }, []);

    const handleLike = (postId) => {
        setPosts(posts.map(post =>
            post.id === postId ? { ...post, likedByMe: !post.likedByMe, likes: post.likedByMe ? post.likes - 1 : post.likes + 1 } : post
        ));
    };

    return (
        <div className="profile-page-wrapper">
            <Navbar />
            <div className="profile-page-container">
                <div className="profile-header">
                    <img src={userInfo.profilePic} alt="Profile" className="profile-pic" />
                    <div className="user-info">
                        <h2 className="username">{userInfo.username}</h2>
                    </div>
                </div>

                <h3 className="section-title">Your Posts</h3>
                <div className="user-posts">
                    {loading ? <div className="spinner"></div> : posts.map(post => (
                        <Post key={post.id} post={post} handleLike={handleLike} />
                    ))}
                </div>
            </div>
        </div>
    );
}

export default ProfilePage;
