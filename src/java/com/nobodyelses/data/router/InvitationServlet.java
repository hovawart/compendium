package com.nobodyelses.data.router;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
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

@SuppressWarnings("serial")
public class InvitationServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(InvitationServlet.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            final String hash = req.getParameter("hash");
            if (Utils.isEmpty(hash)) throw new Exception("A hash must be provided.");

            final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
            final Query q = new Query(User.class);
            q.filter("hash", hash);
            final List<User> find = users.find(q);
            if (find.isEmpty()) throw new Exception("Invitation does not exists.");

            final User user = find.get(0);

            resp.setStatus(200);
            resp.getWriter().write("{\"email\":\"" + user.getUsername() + "\"}");
        } catch (final Exception e) {
            SecureHttpServlet.sendError(resp, e);
        }
    }

    @SuppressWarnings({ "unchecked", "unused" })
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        try {
            final String body = SecureHttpServlet.getBody(req);
            final Type type = com.maintainer.util.Utils.getItemType();
            final Map<String, Object> map = Utils.getGson().fromJson(body, type);

            final String username = (String) map.get("email");
            if (Utils.isEmpty(username)) throw new Exception("An email address must be provided.");
            if (!Utils.isValidEmailAddress(username)) throw new Exception("Email address must be a valid.");

            final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
            final Query q = new Query(User.class);
            q.filter("username", username);
            final List<User> find = users.find(q);
            if (!find.isEmpty()) throw new Exception("Invitation already requested.");

            final User user = new User(username, null);

            final UUID uuid = java.util.UUID.randomUUID();
            final String hash = uuid.toString();
            user.setHash(hash);

            final User post = users.post(user);

            // this is for automatic invitations
            sendInvitation(user);

            resp.setStatus(SecureHttpServlet.SUCCESS_OK);
            resp.setContentType(SecureHttpServlet.APPLICATION_JSON);
            resp.getWriter().write("{\"welcome\":\"" + username + "\"}");
        } catch (final Exception e) {
            SecureHttpServlet.sendError(resp, e);
        }
    }

    public static String sendInvitation(final User user) throws Exception {
        Sendgrid mail = new Sendgrid("nobodyelses","sendgrid1026");

        final String username = user.getUsername();
        final String hash = user.getHash();

        String msgBody = "<p>Hello {0}!<p>To complete the invitation process, you must click the link below to create your password:<p>http://data.nobodyelses.com/#signup?ref={1}<p>If you did not request this invitation, please disregard this email.";
        msgBody = MessageFormat.format(msgBody, username, hash);

        log.fine(msgBody);

        log.finer("sending invitation email...");

        mail.setTo(username)
        .setFrom("noreply@nobodyelses.com")
        .setFromName("Nobodyelses Data")
        .setSubject("Nobodyelses Data Invitation")
        .setText(msgBody)
        .setHtml(msgBody);

        mail.send(new WarningListener() {
            @Override
            public void warning(String w, Throwable t) {
                log.fine(w);
            }
        });

        log.finer("invitation email sent.");

        return username;
    }

    public static String sendInvitation_old(final User user) throws Exception {
        final Properties props = new Properties();
        final Session session = Session.getDefaultInstance(props, null);

        final String username = user.getUsername();
        final String hash = user.getHash();

        String msgBody = "Hello {0}!\n\nTo complete the invitation process, you must click the link below to create your password:\n\nhttps://nobodyelses-basic-application.appspot.com/#signup?ref={1}\n\nIf you did not request this invitation, please disregard this email.";
        msgBody = MessageFormat.format(msgBody, username, hash);

        log.fine(msgBody);

        try {
            log.finer("sending invitation email...");
            final Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("noreply@nobodyelses.com", "Nobodyelses Basic Application"));
            msg.setReplyTo(new InternetAddress[]{new InternetAddress("noreply@nobodyelses.com")});
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(username));
            msg.setSubject("Nobodyelses Basic Application Invitation");
            msg.setText(msgBody);
            Transport.send(msg);
            log.finer("invitation email sent.");

            return username;
        } catch (final AddressException e) {
            throw (e);
        } catch (final MessagingException e) {
            throw (e);
        }
    }
}
