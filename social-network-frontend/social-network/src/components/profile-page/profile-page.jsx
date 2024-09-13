import React, { useState, useEffect } from "react";
import Navbar from '../navbar/navbar.jsx';
import UsersList from '../user-list/user-list.jsx';
import EditProfileModal from '../forms/edit-profile/edit-profile-modal.jsx';
import './profile-page.css';
import { useParams } from "react-router-dom";
import PostFeed from "../post-feed/post-feed.jsx";
import { decodeJWT } from '../../utils/jwtUtils';

function ProfilePage() {
    const { userId } = useParams();
    const [userInfo, setUserInfo] = useState({
        username: "UserNotFound",
        profilePic: "/src/assets/user-icon.png",
        isCurrentUser: false,
        isFriend: false,
    });
    const [posts, setPosts] = useState([]);
    const [friends, setFriends] = useState([]);
    const [showFriendsModal, setShowFriendsModal] = useState(false);
    const [showEditProfileModal, setShowEditProfileModal] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const token = localStorage.getItem("token");
    const decodedToken = decodeJWT(token);
    const loggedInUserId = decodedToken.userId;

    const fetchUser = async () => {
        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/users/${userId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error('Failed to fetch user data');
            }

            const data = await response.json();
            setUserInfo({
                username: data.username,
                profilePic: `${import.meta.env.VITE_BACKEND_URL}${data.profilePhoto}`,
                isFriend: data.isFriend,
                isCurrentUser: Number(userId) === loggedInUserId
            });
        } catch (error) {
            setError(error.message);
            console.error("Error fetching user:", error);
        }
    };

    const fetchUserPosts = async () => {
        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts/user/${userId}?page=1&pageSize=10`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error('Failed to fetch user posts');
            }

            const data = await response.json();
            console.log(response)
            const transformedPosts = data.map(postData => ({
                id: postData.post.id,
                username: postData.username,
                content: postData.post.content,
                likes: postData.likeCount,
                likedByMe: postData.likedByMe,
                timestamp: postData.post.createdAt,
                comments: postData.commentCount,
                userId: postData.post.userId,
                profilePhoto: postData.profilePhoto,
            }));

            setPosts(transformedPosts);
        } catch (error) {
            setError(error.message);
            console.error("Error fetching user posts:", error);
        }
    };

    useEffect(() => {
        setLoading(true);
        fetchUser();
        fetchUserPosts();
        setLoading(false);
    }, [userId]);

    const handleAddFriend = () => {
        console.log("Friend request sent");
        setUserInfo({ ...userInfo, isFriend: true });
    };

    const handleRemoveFriend = () => {
        console.log("Friend removed");
        setUserInfo({ ...userInfo, isFriend: false });
    };

    const toggleFriendsModal = () => {
        setShowFriendsModal(!showFriendsModal);
    };

    const toggleEditProfileModal = () => {
        setShowEditProfileModal(!showEditProfileModal);
    };

    const handleSaveProfile = (updatedInfo) => {
        console.log("Updated Info:", updatedInfo);
        setUserInfo({ ...userInfo, username: updatedInfo.username });
    };

    return (
        <div className="profile-page-wrapper">
            <Navbar loggedInUserId={loggedInUserId} />
            <div className="profile-page-container">
                <div className="profile-header">
                    <img src={userInfo.profilePic} alt="Profile" className="profile-pic" />
                    <div className="user-info">
                        <h2 className="username">{userInfo.username}</h2>
                        {userInfo.isCurrentUser && (
                            <button className="view-friends-button" onClick={toggleFriendsModal}>
                                View Friends
                            </button>
                        )}
                        {userInfo.isCurrentUser ? (
                            <button className="edit-button" onClick={toggleEditProfileModal}>
                                <i className="fas fa-edit"></i> Edit Profile
                            </button>
                        ) : userInfo.isFriend ? (
                            <button className="friend-button" onClick={handleRemoveFriend}>
                                <i className="fas fa-user-minus"></i> Remove Friend
                            </button>
                        ) : (
                            <button className="friend-button" onClick={handleAddFriend}>
                                <i className="fas fa-user-plus"></i> Add Friend
                            </button>
                        )}
                    </div>
                </div>

                {showFriendsModal && (
                    <div className="friends-modal-overlay" onClick={toggleFriendsModal}>
                        <div className="friends-modal" onClick={(e) => e.stopPropagation()}>
                            <button className="close-modal-button" onClick={toggleFriendsModal}>
                                <i className="fas fa-times"></i>
                            </button>
                            <UsersList title="Friends" users={friends} closeModal={toggleFriendsModal} />
                        </div>
                    </div>
                )}

                {showEditProfileModal && (
                    <EditProfileModal
                        userInfo={userInfo}
                        onClose={toggleEditProfileModal}
                        onSave={handleSaveProfile}
                    />
                )}

                <h3 className="section-title">Posts</h3>
                <div className="user-posts">
                    {loading ? (
                        <div className="spinner"></div>
                    ) : (
                        userInfo.isFriend || userInfo.isCurrentUser ? (
                            <PostFeed posts={posts} />
                        ) : (
                            <p key="become-friends">Become friends to see posts!</p>
                        )
                    )}
                </div>
            </div>
        </div>
    );
}

export default ProfilePage;
