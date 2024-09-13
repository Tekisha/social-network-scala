import React from 'react';
import Post from '../post/post.jsx';

function PostFeed({ posts }) {
    if (!posts) {
        return <div>Loading...</div>;
    }

    return (
        <div className="feed-container">
            {posts.map(post => (
                <Post key={post.id} post={post} />
            ))}
        </div>
    );
}

export default PostFeed;
