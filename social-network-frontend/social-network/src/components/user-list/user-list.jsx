import React from 'react';
import { Link } from 'react-router-dom';
import './user-list.css';

function UsersList({ title, users, closeModal }) {
    return (
        <div className="users-list-container">
            <h3 className="users-list-title">{title}</h3>
            {users.length > 0 ? (
                <ul className="users-list">
                    {users.map(user => (
                        <li key={user.id} className="user-item">
                            <Link
                                to={`/profile/${user.id}`}
                                className="user-link"
                                onClick={closeModal ? closeModal : undefined}
                            >
                                <img src={user.profilePic || '/src/assets/user-icon.png'} alt={user.username} className="user-pic" />
                                <span>{user.username}</span>
                            </Link>
                        </li>
                    ))}
                </ul>
            ) : (
                <p>No users found.</p>
            )}
        </div>
    );
}

export default UsersList;
