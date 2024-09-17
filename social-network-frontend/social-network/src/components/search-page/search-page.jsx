import React, { useState, useEffect, useRef } from 'react';
import Navbar from '../navbar/navbar.jsx';
import SearchInput from '../search-input/search-input.jsx';
import UsersList from '../user-list/user-list.jsx';
import './search-page.css';

function SearchPage() {
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredUsers, setFilteredUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [loadingMore, setLoadingMore] = useState(false);
    const [error, setError] = useState(null);
    const [page, setPage] = useState(1);
    const [hasMoreUsers, setHasMoreUsers] = useState(true);
    const pageSize = 10;
    const hasFetchedInitialResults = useRef(false);

    const token = localStorage.getItem("token");

    const fetchUsers = async (currentPage) => {
        if (hasFetchedInitialResults.current && currentPage === 1) {
            return;
        }

        if (currentPage === 1) {
            hasFetchedInitialResults.current = true;
        }

        setLoadingMore(true);

        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/users/search?username=${searchTerm}&page=${currentPage}&pageSize=${pageSize}`, {
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

            if (currentPage === 1) {
                setFilteredUsers(data);
            } else {
                setFilteredUsers((prevUsers) => [...prevUsers, ...data]);
            }

            if (data.length < pageSize) {
                setHasMoreUsers(false);
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
            setLoadingMore(false);
        }
    };

    useEffect(() => {
        if (searchTerm === '') {
            setFilteredUsers([]);
            setHasMoreUsers(false);
        } else {
            setPage(1);
            setHasMoreUsers(true);
            hasFetchedInitialResults.current = false;
            fetchUsers(1);
        }
    }, [searchTerm, token]);

    const handleScroll = () => {
        const scrollPosition = window.innerHeight + document.documentElement.scrollTop;
        const documentHeight = document.documentElement.offsetHeight;

        if (scrollPosition >= documentHeight - 100 && !loadingMore && hasMoreUsers) {
            setLoadingMore(true);
            setPage((prevPage) => prevPage + 1);
        }
    };

    useEffect(() => {
        if (page > 1) {
            fetchUsers(page);
        }
    }, [page]);

    useEffect(() => {
        window.addEventListener("scroll", handleScroll);
        return () => window.removeEventListener("scroll", handleScroll);
    }, [loadingMore, hasMoreUsers]);

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

                {loadingMore && <div className="spinner">Loading more users...</div>}
            </div>
        </div>
    );
}

export default SearchPage;
