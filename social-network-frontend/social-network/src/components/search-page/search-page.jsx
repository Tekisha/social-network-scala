import React, { useState, useEffect } from 'react';
import Navbar from '../navbar/navbar.jsx';
import SearchInput from '../search-input/search-input.jsx';
import UsersList from '../user-list/user-list.jsx';
import './search-page.css';

function SearchPage() {
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredUsers, setFilteredUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const token = localStorage.getItem("token");

    useEffect(() => {
        if (searchTerm === '') {
            setFilteredUsers([]);
        } else {
            const fetchUsers = async () => {
                setLoading(true);
                setError(null);
                try {
                    const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/users/search?username=${searchTerm}&page=1&pageSize=10`, {
                        method: 'GET',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${token}`,
                        },
                    });

                    if (!response.ok) {
                        throw new Error('Failed to fetch users');
                    }

                    const data = await response.json();
                    setFilteredUsers(data);
                } catch (err) {
                    setError(err.message);
                } finally {
                    setLoading(false);
                }
            };

            fetchUsers();
        }
    }, [searchTerm, token]);

    return (
        <div className="search-page-wrapper">
            <Navbar />

            <div className="search-page-container">
                <h1 className="search-page-title">Search Users</h1>
                <SearchInput
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    placeholder="Search for users..."
                />

                {loading && <div>Loading...</div>}
                {error && <div className="error-message">{error}</div>}

                {!loading && !error && (
                    <UsersList title="Search Results" users={filteredUsers} />
                )}
            </div>
        </div>
    );
}

export default SearchPage;
