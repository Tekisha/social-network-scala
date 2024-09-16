import React from 'react';
import Post from '../post/post.jsx';

function PostFeed({ posts, onDeletePost }) {
    if (!posts || posts.length === 0) {
        return <div>No posts available.</div>;
    }

    return (
        <div className="feed-container">
            {posts.map((post, index) => (
                <Post key={`${post.id}-${index}`} post={post} onDelete={onDeletePost} />
            ))}
        </div>
    );
}

export default PostFeed;
