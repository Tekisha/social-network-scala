package exceptions

class UsernameAlreadyExistsException(message: String = "Username already exists") extends Exception(message)