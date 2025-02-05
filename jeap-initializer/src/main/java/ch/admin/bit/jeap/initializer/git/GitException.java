package ch.admin.bit.jeap.initializer.git;

import org.eclipse.jgit.api.errors.GitAPIException;

public class GitException extends RuntimeException {
    private GitException(String message, Exception cause) {
        super(message, cause);
    }

    private GitException(String message) {
        super(message);
    }

    public static GitException cloneFailed(Exception cause) {
        return new GitException("git clone failed", cause);
    }

    public static GitException fileRetrievalFailed(String message) {
        return new GitException(message);
    }

    public static GitException initFailed(GitAPIException cause) {
        return new GitException("git init failed", cause);
    }
}
