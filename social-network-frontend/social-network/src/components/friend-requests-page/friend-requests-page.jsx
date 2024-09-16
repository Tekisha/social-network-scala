import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom'; 
import Navbar from '../navbar/navbar.jsx';
import './friend-requests-page.css';

function FriendRequestsPage() {
    const [friendRequests, setFriendRequests] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const token = localStorage.getItem("token");

    useEffect(() => {
        const fetchFriendRequests = async () => {
            setLoading(true);
            setError(null);

            try {
                const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/friendRequests/receivedPending?page=1&pageSize=10`, {
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
                setFriendRequests(pendingRequests);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchFriendRequests();
    }, [token]);

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
            setError(err.message);
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
                {error && <div className="error-message">{error}</div>}

                {!loading && !error && friendRequests.length > 0 ? (
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
            </div>
        </div>
    );
}

export default FriendRequestsPage;
