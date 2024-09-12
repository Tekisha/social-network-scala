import React, { useState, useEffect } from "react";
import Navbar from '../navbar/navbar.jsx';
import UsersList from '../user-list/user-list.jsx';
import EditProfileModal from '../forms/edit-profile/edit-profile-modal.jsx';
import './profile-page.css';
import { useParams } from "react-router-dom";
import PostFeed from "../post-feed/post-feed.jsx";

// Mocked jwtUtils to decode token
const mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

// Mock decode function
const decodeJWT = (token) => {
    return {
        sub: "1234567890",  // Mocked logged-in user ID
        name: "JohnDoe",     // Mocked username
    };
};

function ProfilePage() {
    const { viewedUserId } = useParams();
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

    const token = mockToken;
    const decodedToken = decodeJWT(token);
    const loggedInUserId = decodedToken.sub;

    useEffect(() => {
        setLoading(true);

        setTimeout(() => {
            const dummyUserInfo = {
                userId: viewedUserId,
                username: viewedUserId === loggedInUserId ? "CurrentUser" : "OtherUser",
                profilePic: "/src/assets/user-icon.png",
            };

            const isCurrentUser = viewedUserId === loggedInUserId;
            const isFriend = !isCurrentUser && dummyUserInfo.username === "OtherUser";

            setUserInfo({
                ...dummyUserInfo,
                isCurrentUser,
                isFriend
            });

            if (isCurrentUser) {
                const dummyFriends = [
                    { id: 1, username: "Friend1", profilePic: "/src/assets/user-icon.png" },
                    { id: 2, username: "Friend2", profilePic: "/src/assets/user-icon.png" },
                    { id: 3, username: "Friend3", profilePic: "/src/assets/user-icon.png" }
                ];
                setFriends(dummyFriends);
            }

            setLoading(false);
        }, 1000);

        setTimeout(() => {
            const userPosts = [
                {
                    id: 1,
                    user: "OtherUser",
                    content: "This is another user's post.",
                    likes: 12,
                    likedByMe: false,
                    timestamp: new Date().toISOString(),
                    comments: 2
                },
                {
                    id: 2,
                    user: "OtherUser",
                    content: "Second post from another user!",
                    likes: 5,
                    likedByMe: false,
                    timestamp: new Date().toISOString(),
                    comments: 1
                },
            ];
            setPosts(userPosts);
        }, 1500);
    }, [viewedUserId, loggedInUserId]);

    const handleLike = (postId) => {
        setPosts(posts.map(post =>
            post.id === postId ? { ...post, likedByMe: !post.likedByMe, likes: post.likedByMe ? post.likes - 1 : post.likes + 1 } : post
        ));
    };

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
                    <img src={userInfo.profilePic} alt="Profile" className="profile-pic"/>
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
                            <PostFeed posts={posts} handleLike={handleLike} />
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
