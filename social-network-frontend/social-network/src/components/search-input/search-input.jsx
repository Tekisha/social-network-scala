import React from 'react';
import './search-input.css';

function SearchInput({ value, onChange, placeholder }) {
    return (
        <input
            type="text"
            className="search-input"
            value={value}
            onChange={onChange}
            placeholder={placeholder}
        />
    );
}

export default SearchInput;
