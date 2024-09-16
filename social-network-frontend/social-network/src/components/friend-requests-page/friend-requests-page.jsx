import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../navbar/navbar.jsx';
import './friend-requests-page.css';

function FriendRequestsPage() {
    const [friendRequests, setFriendRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(1);
    const [pageSize] = useState(10);
    const [loadingMore, setLoadingMore] = useState(false);
    const [hasMoreRequests, setHasMoreRequests] = useState(true);
    const hasFetchedRequests = useRef(false);
    const navigate = useNavigate();

    const token = localStorage.getItem("token");

    const fetchFriendRequests = async (currentPage) => {
        if (hasFetchedRequests.current && currentPage === 1) {
            return;
        }

        if (currentPage === 1) {
            hasFetchedRequests.current = true;
        }

        setLoadingMore(true);

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/friendRequests/receivedPending?page=${currentPage}&pageSize=${pageSize}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                throw new Error('Failed to fetch friend requests');
            }

            const data = await response.json();
            const pendingRequests = data.filter(request => request.status === 'pending');

            setFriendRequests((prevRequests) => [...prevRequests, ...pendingRequests]);

            if (pendingRequests.length < pageSize) {
                setHasMoreRequests(false);
            }
        } catch (err) {
            console.error(err.message);
        } finally {
            setLoading(false);
            setLoadingMore(false);
        }
    };

    useEffect(() => {
        fetchFriendRequests(page);
    }, [page]);

    const handleScroll = () => {
        const scrollPosition = window.innerHeight + document.documentElement.scrollTop;
        const documentHeight = document.documentElement.offsetHeight;

        if (scrollPosition >= documentHeight - 100 && !loadingMore && hasMoreRequests) {
            setLoadingMore(true);
            setPage((prevPage) => prevPage + 1);
        }
    };

    useEffect(() => {
        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll);
    }, [loadingMore, hasMoreRequests]);

    const handleRespondToRequest = async (requestId, status) => {
        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/friendRequests/${requestId}/respond`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ status }),
            });

            if (!response.ok) {
                throw new Error(`Failed to ${status} the friend request`);
            }

            alert(`Successfully ${status} the friend request`);
            setFriendRequests(friendRequests.filter(request => request.id !== requestId));
        } catch (err) {
            console.error(err.message);
        }
    };

    const handleAccept = (requestId) => {
        handleRespondToRequest(requestId, 'accepted');
    };

    const handleReject = (requestId) => {
        handleRespondToRequest(requestId, 'rejected');
    };

    const handleNavigateToProfile = (requesterId) => {
        navigate(`/profile/${requesterId}`);
    };

    return (
        <div className="friend-requests-page-wrapper">
            <Navbar />
            <div className="friend-requests-page-container">
                <h1 className="friend-requests-title">Friend Requests</h1>

                {loading && <div>Loading...</div>}

                {friendRequests.length > 0 ? (
                    <ul className="friend-requests-list">
                        {friendRequests.map(request => (
                            <li key={request.id} className="friend-request-item">
                                <div className="friend-request-info" onClick={() => handleNavigateToProfile(request.requesterId)}>
                                    <img
                                        src={`${import.meta.env.VITE_BACKEND_URL}${request.requesterProfilePhoto || "/assets/images/default.png"}`}
                                        alt={request.username}
                                        className="friend-request-pic"
                                    />
                                    <span>{request.requesterUsername}</span>
                                </div>
                                <div className="friend-request-actions">
                                    <button className="accept-button" data-button-type="accept" onClick={() => handleAccept(request.id)}>Accept</button>
                                    <button className="reject-button" data-button-type="reject" onClick={() => handleReject(request.id)}>Reject</button>
                                </div>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p>No pending friend requests.</p>
                )}

                {loadingMore && <div className="spinner">Loading more requests...</div>}
            </div>
        </div>
    );
}

export default FriendRequestsPage;
