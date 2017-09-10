package com.damaru.morphmusic;

public class MorpherException extends Exception {

    private static final long serialVersionUID = 1L;

    public MorpherException(String message, Throwable cause) {
        super(message, cause);
    }

    public MorpherException(String message) {
        super(message);
    }

    public MorpherException(Throwable cause) {
        super(cause);
    }

    
}
