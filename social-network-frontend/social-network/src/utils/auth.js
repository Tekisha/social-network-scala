import {decodeJWT} from './jwtUtils.js';

export const isAuthenticated = () => {
    const token = localStorage.getItem('token');
    if (!token) return false;

    try {
        const decoded = decodeJWT(token);
        const currentTime = Date.now() / 1000;
        if (decoded.expiration < currentTime) {
            localStorage.removeItem('token');
            return false;
        }
        return true;
    } catch (e) {
        return false;
    }
};