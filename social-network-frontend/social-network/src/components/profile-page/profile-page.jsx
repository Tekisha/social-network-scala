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
        pendingRequest: false,
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
                pendingRequest: data.pendingRequest,
                isCurrentUser: Number(userId) === loggedInUserId,
            });
            console.log(userInfo)
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

    const handleAddFriend = async () => {
        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/friendRequests`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body: JSON.stringify({
                    receiverId: Number(userId)
                })
            });

            if (response.status === 400) {
                const data = await response.json();
                if (data.message === 'A pending friend request already exists between these users') {
                    alert("There's a pending friend request. Please check your requests to respond.");
                    return;
                }
            }

            if (!response.ok) {
                throw new Error('Failed to send friend request');
            }

            const data = await response.json();
            console.log("Friend request sent", data);

            setUserInfo({ ...userInfo, pendingRequest: true });
        } catch (error) {
            console.error("Error sending friend request:", error);
            setError(error.message);
        }
    };

    const handleRemoveRequest = async () => {
        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/friendRequests/user/${userId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error('Failed to remove friend request');
            }

            console.log("Friend request removed");
            setUserInfo({ ...userInfo, pendingRequest: false });
        } catch (error) {
            console.error("Error removing friend request:", error);
            setError(error.message);
        }
    };

    const handleRemoveFriend = async () => {
        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/friendships/${userId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                }
            });

            if (!response.ok) {
                throw new Error('Failed to remove friend');
            }

            console.log("Friend removed successfully");
            setUserInfo({ ...userInfo, isFriend: false });
        } catch (error) {
            console.error("Error removing friend:", error);
            setError(error.message);
        }
    };

    const toggleFriendsModal = () => {
        setShowFriendsModal(!showFriendsModal);
    };

    const toggleEditProfileModal = () => {
        setShowEditProfileModal(!showEditProfileModal);
    };

    const handleSaveProfile = (updatedInfo) => {
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
                        ) : userInfo.pendingRequest ? (
                            <button className="friend-button" onClick={handleRemoveRequest}>
                                <i className="fas fa-times"></i> Remove Request
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
