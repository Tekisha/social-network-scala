import React from 'react';
import Post from '../post/post.jsx';

function PostFeed({ posts, handleLike }) {
    return (
        <div className="feed-container">
            {posts.map(post => (
                <Post key={post.id} post={post} handleLike={handleLike} />
            ))}
        </div>
    );
}

export default PostFeed;
