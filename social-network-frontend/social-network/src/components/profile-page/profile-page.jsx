import React, {useState, useEffect, useRef} from "react";
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
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [hasMorePosts, setHasMorePosts] = useState(true);
    const [page, setPage] = useState(1);
    const pageSize = 4;
    const [hasMoreFriends, setHasMoreFriends] = useState(true);
    const [friendPage, setFriendPage] = useState(1);
    const friendPageSize = 50;
    const hasFetchedFriends = useRef(false);

    const token = localStorage.getItem("token");
    const decodedToken = decodeJWT(token);
    const loggedInUserId = decodedToken.userId;

    const hasFetchedPosts = useRef(false);

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

        } catch (error) {
            setError(error.message);
            console.error("Error fetching user:", error);
        }
    };

    const fetchUserPosts = async (pageToFetch = 1) => {
        if (hasFetchedPosts.current && pageToFetch === 1) {
            return;
        }

        if (pageToFetch === 1) {
            hasFetchedPosts.current = true;
        }

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/posts/user/${userId}?page=${pageToFetch}&pageSize=${pageSize}`, {
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

            if (transformedPosts.length < pageSize) {
                setHasMorePosts(false);
            }

            setPosts(prevPosts => [...prevPosts, ...transformedPosts]);
        } catch (error) {
            setError(error.message);
            console.error("Error fetching user posts:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleScroll = () => {
        if (window.innerHeight + document.documentElement.scrollTop !== document.documentElement.offsetHeight || loading) {
            return;
        }

        if (hasMorePosts) {
            setPage(prevPage => prevPage + 1);
        }
    };

    useEffect(() => {
        const initializeData = async () => {
            setLoading(true);
            await fetchUser();
            await fetchUserPosts(page);
        };

        initializeData();
    }, [userId]);

    useEffect(() => {
        if (page > 1) {
            fetchUserPosts(page);
        }
    }, [page]);

    useEffect(() => {
        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll);
    }, [loading, hasMorePosts]);


    const fetchFriends = async (currentPage = 1) => {
        if (hasFetchedFriends.current && currentPage === 1) {
            return;
        }

        if (currentPage === 1) {
            hasFetchedFriends.current = true;
        }

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/friendships?page=${currentPage}&pageSize=${friendPageSize}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error('Failed to fetch friends');
            }

            const data = await response.json();
            const transformedFriends = data.map(friend => ({
                ...friend,
                id: friend.friendId
            }));

            if (transformedFriends.length < friendPageSize) {
                setHasMoreFriends(false);
            }

            setFriends(prevFriends => [...prevFriends, ...transformedFriends]);
        } catch (error) {
            setError(error.message);
            console.error("Error fetching friends:", error);
        }
    };


    const handleFriendsScroll = () => {
        const scrollPosition = window.innerHeight + document.documentElement.scrollTop;
        const documentHeight = document.documentElement.offsetHeight;

        if (scrollPosition >= documentHeight - 100 && hasMoreFriends) {
            setFriendPage(prevPage => prevPage + 1);
        }
    };

    useEffect(() => {
        if (friendPage > 1) {
            fetchFriends(friendPage);
        }
    }, [friendPage]);

    useEffect(() => {
        window.addEventListener("scroll", handleFriendsScroll);
        return () => window.removeEventListener("scroll", handleFriendsScroll);
    }, [hasMoreFriends]);

    useEffect(() => {
        if (showFriendsModal) {
            fetchFriends(1);
        }
    }, [showFriendsModal]);

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

    const handleDeletePost = (postId) => {
        setPosts((prevPosts) => prevPosts.filter(post => post.id !== postId));
    };

    const toggleFriendsModal = () => setShowFriendsModal(!showFriendsModal);
    const toggleEditProfileModal = () => setShowEditProfileModal(!showEditProfileModal);

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
                            <UsersList
                                title="Friends"
                                users={friends}
                                closeModal={toggleFriendsModal}
                                loading={loading}
                            />
                        </div>
                    </div>
                )}

                {showEditProfileModal && (
                    <EditProfileModal
                        userInfo={userInfo}
                        onClose={toggleEditProfileModal}
                        refreshUser={fetchUser}
                    />
                )}

                <h3 className="section-title">Posts</h3>
                <div className="user-posts">
                    {posts.length > 0 ? <PostFeed posts={posts} onDeletePost={handleDeletePost} /> : <p>No posts available.</p>}
                    {loading && <div className="spinner">Loading...</div>}
                </div>
            </div>
        </div>
    );
}

export default ProfilePage;
