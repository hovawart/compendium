package com.nobodyelses.data.router;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.Query;
import com.nobodyelses.data.model.User;
import com.nobodyelses.data.utils.Sendgrid;
import com.nobodyelses.data.utils.Sendgrid.WarningListener;
import com.nobodyelses.data.utils.Utils;

public class ResetPasswordServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(ResetPasswordServlet.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            final String username = req.getParameter("username");
            if (Utils.isEmpty(username)) throw new Exception("A username must be provided.");

            final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
            final Query q = new Query(User.class);
            q.filter("username", username);
            final List<User> find = users.find(q);
            if (find.isEmpty()) throw new Exception("User does not exists.");

            final User user = find.get(0);

            resp.setStatus(200);
            resp.getWriter().write("{\"message\":\"" + username + "\"}");
        } catch (final Exception e) {
            SecureHttpServlet.sendError(resp, e);
        }

    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        try {
            String message = null;

            final String hash = req.getParameter("ref");
            if (!Utils.isEmpty(hash)) {
                message = resetPassword(hash, req, resp);
            } else {
                message = sendPasswordReset(req, resp);
            }
            resp.setStatus(SecureHttpServlet.SUCCESS_OK);
            resp.setContentType(SecureHttpServlet.APPLICATION_JSON);
            resp.getWriter().write("{\"welcome\":\"" + message + "\"}");
        } catch (final Exception e) {
            SecureHttpServlet.sendError(resp, e);
        }
    }

    private String resetPassword(final String hash, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
        final Query q = new Query(User.class);
        q.filter("hash", hash);
        final List<User> find = users.find(q);
        if (find.isEmpty()) throw new Exception("User does not exists.");

        final User user = find.get(0);

        final String password = req.getParameter("password");
        if (Utils.isEmpty(password)) throw new Exception("A password must be provided.");

        final String confirmPassword = req.getParameter("confirmPassword");
        if (!password.equals(confirmPassword)) throw new Exception("Passwords must match.");

        final String encrypt = com.maintainer.util.Utils.encrypt(password);
        user.setPassword(encrypt);
        user.setHash(null);

        users.put(user);

        final String username = user.getUsername();

        final String credentials = Utils.formatRestletCredentials(username, password.toCharArray());
        resp.addHeader("Set-Cookie", "Credentials=" + credentials);

        final String userIdCookie = Utils.getUserIdCookie(user.getKey().toString());
        resp.addHeader("Set-Cookie", userIdCookie);

        return username;
    }

    @SuppressWarnings("unchecked")
    private String sendPasswordReset(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final String username = req.getParameter("username");
        if (Utils.isEmpty(username)) throw new Exception("An email address must be provided.");
        if (!Utils.isValidEmailAddress(username)) throw new Exception("Email address must be a valid.");

        final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
        final Query q = new Query(User.class);
        q.filter("username", username);
        final List<User> find = users.find(q);
        if (find.isEmpty()) throw new Exception("User does not exists.");

        final User user = find.get(0);

        final UUID uuid = java.util.UUID.randomUUID();
        final String hash = uuid.toString();
        user.setHash(hash);
        users.put(user);

        Sendgrid mail = new Sendgrid("nobodyelses","sendgrid1026");

        String msgBody = "<p>Hello {0}!<p>To complete the password reset process, you must click the link below to change your password:<p>http://data.nobodyelses.com/#resetpassword?ref={1}<p>If you did not request a password reset, please disregard this email.<p>Your password will not change until you access the link above and create a new one.";
        msgBody = MessageFormat.format(msgBody, username, hash);

        log.fine(msgBody);

        log.finer("sending reset email...");
        mail.setTo(username)
        .setFrom("noreply@nobodyelses.com")
        .setFromName("Nobodyelses Data")
        .setSubject("Nobodyelses Data Password Reset")
        .setText(msgBody)
        .setHtml(msgBody);

        mail.send(new WarningListener() {
            @Override
            public void warning(String w, Throwable t) {
                log.fine(w);
            }
        });

        log.finer("reset email sent.");

        return username;
    }

    @SuppressWarnings("unchecked")
    private String sendPasswordReset_old(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        final String username = req.getParameter("username");
        if (Utils.isEmpty(username)) throw new Exception("An email address must be provided.");
        if (!Utils.isValidEmailAddress(username)) throw new Exception("Email address must be a valid.");

        final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
        final Query q = new Query(User.class);
        q.filter("username", username);
        final List<User> find = users.find(q);
        if (find.isEmpty()) throw new Exception("User does not exists.");

        final User user = find.get(0);

        final UUID uuid = java.util.UUID.randomUUID();
        final String hash = uuid.toString();
        user.setHash(hash);
        users.put(user);

        final Properties props = new Properties();
        final Session session = Session.getDefaultInstance(props, null);

        String msgBody = "Hello {0}!\n\nTo complete the password reset process, you must click the link below to change your password:\n\nhttps://www.animalland.com/#resetpassword?ref={1}\n\nIf you did not request a password reset, please disregard this email.\n\nYour password will not change until you access the link above and create a new one.";
        msgBody = MessageFormat.format(msgBody, username, hash);

        log.fine(msgBody);

        try {
            log.finer("sending reset email...");
            final Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("support@animalland.com"));
            msg.setReplyTo(new InternetAddress[]{new InternetAddress("noreply@animalland.com")});
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(username));
            msg.setSubject("Animalland password reset requested");
            msg.setText(msgBody);
            Transport.send(msg);
            log.finer("reset email sent.");

            return username;
        } catch (final AddressException e) {
            throw (e);
        } catch (final MessagingException e) {
            throw (e);
        }
    }
}
