package pe.edu.utp.Paway.config;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import pe.edu.utp.Paway.service.AccountService;
import pe.edu.utp.Paway.service.AccountService.SignedIn;

/**
 * The HTTP session only stores the account id and its role. Names and the customer id are
 * read from the database on each request, so a deleted account stops working immediately.
 */
public final class AccountSession {
    public static final String ACCOUNT_ID = "accountId";
    public static final String ROLE = "accountRole";
    private static final String CACHE = AccountSession.class.getName() + ".current";

    private AccountSession() { }

    /** Stores the account in a fresh session id (prevents session fixation); other attributes survive. */
    public static void signIn(HttpServletRequest request, SignedIn account) {
        HttpSession session = request.getSession();
        request.changeSessionId();
        session.setAttribute(ACCOUNT_ID, account.accountId());
        session.setAttribute(ROLE, account.role().name());
        request.setAttribute(CACHE, Optional.of(account));
    }

    /** The signed-in account for this request, looked up once and cached on the request. */
    @SuppressWarnings("unchecked")
    public static Optional<SignedIn> current(HttpServletRequest request, AccountService accounts) {
        Object cached = request.getAttribute(CACHE);
        if (cached instanceof Optional<?> optional) return (Optional<SignedIn>) optional;
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute(ACCOUNT_ID);
        Object role = session == null ? null : session.getAttribute(ROLE);
        Optional<SignedIn> account = id instanceof Long accountId
                ? accounts.find(accountId).filter(a -> a.role().name().equals(role))
                : Optional.empty();
        request.setAttribute(CACHE, account);
        return account;
    }
}
