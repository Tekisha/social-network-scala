import React, { useEffect, useState } from 'react';
import Post from '../post/post.jsx';

const mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

const decodeJWT = (token) => {
    return {
        sub: "1234567890",
        name: "JohnDoe",
    };
};

function PostFeed({ posts, handleLike }) {
    const [user, setUser] = useState(null);

    useEffect(() => {
        const decodedUser = decodeJWT(mockToken);
        setUser(decodedUser);
    }, []);

    if (!user) {
        return <div>Loading...</div>;
    }

    return (
        <div className="feed-container">
            {posts.map(post => (
                <Post key={post.id} post={post} handleLike={handleLike} loggedInUserId={user.sub} />
            ))}
        </div>
    );
}

export default PostFeed;
