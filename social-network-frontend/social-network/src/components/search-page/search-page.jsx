import React, { useState, useEffect } from 'react';
import Navbar from '../navbar/navbar.jsx';
import SearchInput from '../search-input/search-input.jsx';
import UsersList from '../user-list/user-list.jsx';
import './search-page.css';

const mockUsers = [
    { id: 1, username: 'Friend1', profilePic: '/src/assets/user-icon.png' },
    { id: 2, username: 'Friend2', profilePic: '/src/assets/user-icon.png' },
    { id: 3, username: 'Friend3', profilePic: '/src/assets/user-icon.png' },
    { id: 4, username: 'JohnDoe', profilePic: '/src/assets/user-icon.png' },
    { id: 5, username: 'JaneSmith', profilePic: '/src/assets/user-icon.png' }
];

function SearchPage() {
    const [searchTerm, setSearchTerm] = useState('');
    const [filteredUsers, setFilteredUsers] = useState(mockUsers);

    useEffect(() => {
        if (searchTerm === '') {
            setFilteredUsers(mockUsers); // Reset when the input is cleared
        } else {
            const lowercasedFilter = searchTerm.toLowerCase();
            const filtered = mockUsers.filter(user =>
                user.username.toLowerCase().includes(lowercasedFilter)
            );
            setFilteredUsers(filtered);
        }
    }, [searchTerm]);

    return (
        <div className="search-page-wrapper">
            {/* Add Navbar */}
            <Navbar />

            <div className="search-page-container">
                <h1 className="search-page-title">Search Users</h1>
                <SearchInput
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    placeholder="Search for users..."
                />
                <UsersList title="Search Results" users={filteredUsers} />
            </div>
        </div>
    );
}

export default SearchPage;
