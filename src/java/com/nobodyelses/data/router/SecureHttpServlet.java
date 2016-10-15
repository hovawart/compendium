package com.nobodyelses.data.router;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasypt.util.text.StrongTextEncryptor;
import org.restlet.engine.util.Base64;
import org.restlet.ext.crypto.internal.CryptoUtils;

import com.google.appengine.api.NamespaceManager;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.Key;
import com.nobodyelses.data.model.User;
import com.nobodyelses.data.utils.Utils;

@SuppressWarnings("serial")
public class SecureHttpServlet extends MyHttpServlet {
    private static final Logger log = Logger.getLogger(SecureHttpServlet.class.getName());

    public static final String NOT_FOUND_STRING = "Not Found";
    public static final int NOT_FOUND = 404;
    public static final int SUCCESS_OK = 200;
    public static final int INTERNAL_SERVER_ERROR = 500;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        //String current = NamespaceManager.get();
        boolean verified;
        try {
            verified = verify(req);

            if (!verified) {
                Utils.sendUnauthorized(resp, getCredentialsCookieName());
                return;
            }

            final User user = getUser();
            if (user != null) {
                log.fine(MessageFormat.format("username: {0}", user.getUsername()));
                NamespaceManager.set(user.getKey().getId().toString());
                // temporarily put the user id in the cookie
                //setUserIdCookie(resp, user);
            }

            super.service(req, resp);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            //NamespaceManager.set(current);
        }

    }

    protected void setUserIdCookie(final HttpServletResponse resp, final User user) {
        final String userid = user.getKey().toString();
        final String cookie = Utils.getUserIdCookie(userid);
        resp.addHeader("Set-Cookie", cookie);
    }

    protected String getCookieIdentifier(final HttpServletRequest req) {
        return getCookieIdentifier(req, getCredentialsCookieName());
    }

    protected String getCookieIdentifier(final HttpServletRequest req, String cookieName) {
        String identifier = null;

        final Map<String, Cookie> mappedCookies = new HashMap<String, Cookie>();
        final Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                mappedCookies.put(cookie.getName(), cookie);
            }

            final Cookie cookie = mappedCookies.get(cookieName);
            if (cookie != null) {
                try {
                    final String decrypt = CryptoUtils.decrypt("AES", "MyExtraSecretKey".getBytes(), Base64.decode(cookie.getValue()));
                    final String[] split = decrypt.split("\\/");
                    final Date issued = new Date(Long.parseLong(split[0]));
                    identifier = split[1];
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return identifier;
    }
    /**
     * This is for backward compatibility with Restlet until it is pulled out.
     *
     * @param req
     * @return
     */
    private boolean verifyRestlet(final HttpServletRequest req) {
        final Map<String, Cookie> mappedCookies = new HashMap<String, Cookie>();
        final Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (final Cookie cookie : cookies) {
                mappedCookies.put(cookie.getName(), cookie);
            }

            final Cookie cookie = mappedCookies.get(getCredentialsCookieName());
            if (cookie != null) {

                try {
                    final String decrypt = CryptoUtils.decrypt("AES", "MyExtraSecretKey".getBytes(), Base64.decode(cookie.getValue()));
                    final String[] split = decrypt.split("\\/");
                    final Date issued = new Date(Long.parseLong(split[0]));
                    final String identifier = split[1];
                    final String secret = split[2];
                    final String indexes = split[3];

                    String userId = null;
                    //                    final Cookie userIdCookie = mappedCookies.get("userid");
                    //                    if (userIdCookie != null) {
                    //                        userId = userIdCookie.getValue();
                    //                    }
                    return verify(issued, identifier, secret, userId);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    protected String getCredentialsCookieName() {
        return "Credentials";
    }

    public static String getSessionCookie(final String identifier, final String secret, final String path) {
        final Date date = Utils.getDate();
        final String time = String.valueOf(date.getTime());
        final int index1 = time.length() + 1;
        final int index2 = index1 + identifier.length() + 1;

        final String credentials = MessageFormat.format("{0}/{1}/{2}/{3},{4}", time, identifier, secret, index1, index2);

        final StrongTextEncryptor strongTextEncryptor = new StrongTextEncryptor();
        strongTextEncryptor.setPassword(getCookieTextEncryptorPassword());
        final String encrypt = strongTextEncryptor.encrypt(credentials);

        final String cookie = MessageFormat.format("{0}={1}; path={2}; HttpOnly", getSessionCookieName(), encrypt, path);

        return cookie;
    }

    private String getPersistentCookie(final String identifier, final String secret, final String path, final Date expiration) {
        final Date date = Utils.getDate();
        final String time = String.valueOf(date.getTime());
        final int index1 = time.length();
        final int index2 = index1 + identifier.length();

        final String credentials = MessageFormat.format("{0}{1}{2}/{3},{4}", time, identifier, secret, index1, index2);

        final StrongTextEncryptor strongTextEncryptor = new StrongTextEncryptor();
        strongTextEncryptor.setPassword(getCookieTextEncryptorPassword());
        final String encrypt = strongTextEncryptor.encrypt(credentials);

        final String expires = getExpires(expiration);

        final String cookie = MessageFormat.format("{0}={1}; path={2}; Expires={3}; HttpOnly", getSessionCookieName(), encrypt, path, expires);

        return cookie;
    }

    protected String getExpires(final Date expiration) {
        final String expires = sdf.format(expiration);
        return expires;
    }

    protected boolean verify(final HttpServletRequest req) throws Exception {
        if (isRestletMode()) {
            return verifyRestlet(req);
        }

        final Map<String, Cookie> mappedCookies = new HashMap<String, Cookie>();
        final Cookie[] cookies = req.getCookies();
        for (final Cookie cookie : cookies) {
            mappedCookies.put(cookie.getName(), cookie);
        }

        final Cookie cookie = mappedCookies.get(getSessionCookieName());
        if (cookie != null) {
            final StrongTextEncryptor strongTextEncryptor = new StrongTextEncryptor();
            strongTextEncryptor.setPassword(getCookieTextEncryptorPassword());
            final String decrypt = strongTextEncryptor.decrypt(cookie.getValue());
            final String[] split = decrypt.split("\\/");
            final String attributes = split[0];
            final String[] indexes = split[1].split(",");
            final int index0 = Integer.parseInt(indexes[0]);
            final int index1 = Integer.parseInt(indexes[1]);

            final String issuedString = attributes.substring(0, index0);
            final Long issuedLong = Long.parseLong(issuedString);

            final Date issued = new Date(issuedLong);
            final String identifier = attributes.substring(index0, index1);
            final String secret = attributes.substring(index1);

            return verify(issued, identifier, secret, null);
        }

        return false;
    }

    protected void updateSessionCookie(final HttpServletResponse resp, final String identifier, final String secret, final String path) {
        final String cookie = getSessionCookie(identifier, secret, path);
        resp.addHeader("Set-Cookie", cookie);
    }

    protected boolean isRestletMode() {
        return true;
    }

    protected boolean verify(final Date issued, final String identifier, final String secret, final String userId) throws Exception {
        User user = getUserByIdentifier(identifier);

        setUser(user);

        return user != null;
    }

    protected User getUserByIdentifier(final String identifier) throws Exception {
        User user = null;
        Key key = null;

        try {
            key = getKeyFromIdentifier(identifier);
        } catch (Exception e) {}

        if (key != null) {
            user = getUserByKey(key);
        } else {
            user = Utils.getUserByIdentifier(identifier);
        }

        return user;
    }

    @SuppressWarnings("unchecked")
    protected User getUserByKey(Key key) throws Exception {
        if (User.class.getName().equals(key.getKind().getName())) {
            throw new Exception("A user login is required.");
        }
        final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
        User user = users.get(key);
        return user;
    }

    protected Key getKeyFromIdentifier(final String identifier) throws Exception {
        Key key = null;

        try {
            key = Key.fromString(identifier);
        } catch (Exception e) {}

        return key;
    }

    protected static void setUser(final User user) {
        ThreadLocalInfo.setInfo(user);
    }

    protected static User getUser() {
        return ThreadLocalInfo.getInfo().getUser();
    }

    protected User getUserByUserId(final String userId) throws Exception {
        final Key key = Key.fromString(userId);
        final User user = (User) DataProviderFactory.instance().getDataProvider(User.class).get(key);
        return user;
    }

    protected static String getSessionCookieName() {
        return "sessionid";
    }

    protected static String getCookieTextEncryptorPassword() {
        return "Very extra super secret password";
    }
}
