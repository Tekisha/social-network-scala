import React, {useState} from 'react';
import Post from '../post/post.jsx';

function PostFeed({ posts }) {
    const [postList, setPostList] = useState(posts);

    const handleDelete = (postId) => {
        console.log(postId)
        setPostList(postList.filter(post => post.id !== postId));
    };

    if (!postList) {  
        return <div>Loading...</div>;
    }

    console.log(postList)

    return (
        <div className="feed-container">
            {postList.map(post => (
                <Post key={post.id} post={post} onDelete={handleDelete} />
            ))}
        </div>
    );
}

export default PostFeed;
