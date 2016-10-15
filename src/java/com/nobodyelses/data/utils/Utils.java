package com.nobodyelses.data.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;
import org.mozilla.javascript.debug.DebuggableScript;
import org.restlet.Request;
import org.restlet.engine.util.Base64;
import org.restlet.ext.crypto.internal.CryptoUtils;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.apphosting.api.DatastorePb;
import com.google.apphosting.api.DatastorePb.CompiledCursor;
import com.google.apphosting.api.DatastorePb.CompiledCursor.Position;
import com.google.apphosting.api.DatastorePb.CompiledCursor.PositionIndexValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.storage.onestore.v3.OnestoreEntity;
import com.google.storage.onestore.v3.OnestoreEntity.Path;
import com.google.storage.onestore.v3.OnestoreEntity.Path.Element;
import com.google.storage.onestore.v3.OnestoreEntity.PropertyValue;
import com.google.storage.onestore.v3.OnestoreEntity.Reference;
import com.maintainer.data.controller.Resource;
import com.maintainer.data.model.EntityImpl;
import com.maintainer.data.model.MyField;
import com.maintainer.data.provider.DataProvider;
import com.maintainer.data.provider.DataProviderFactory;
import com.maintainer.data.provider.Key;
import com.maintainer.data.provider.Query;
import com.maintainer.data.provider.datastore.MyMemcacheServiceFactory;
import com.nobodyelses.data.model.User;
import com.nobodyelses.data.router.MyResourceRouter;

public class Utils {
    public static final String SYSTEM_USERNAME = "administrator";
    private static final String SYSTEM_USER = "SystemUser";
    private static final String SYSTEM_USER_KEY = "SystemUserKey";

    public static final int UNAUTHORIZED = 401;

    private static final Logger log = Logger.getLogger(Utils.class.getName());

    private static final String _0_1 = "{0}: {1}";
    private static final String WEIGHT_INVALID_0 = "Weight invalid: {0}";

    public static final String AMERICA_NEW_YORK = "America/New_York";
    public static final String TRADING_TIME_ZONE = AMERICA_NEW_YORK;
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDD_HHMMSS = "yyyyMMdd HH:mm:ss";
    public static final String MMDDYY_HHMMZ = "MM.dd.yy HH:mm zzz";
    public static final DateTimeFormatter DATE_FORMATTER_NEW_YORK_WITHOUT_TIME = DateTimeFormat.forPattern(YYYYMMDD).withZone(DateTimeZone.forID(TRADING_TIME_ZONE));
    public static final DateTimeFormatter DATE_FORMATTER_NEW_YORK_WITHOUT_TIME2 = DateTimeFormat.forPattern(YYYY_MM_DD).withZone(DateTimeZone.forID(TRADING_TIME_ZONE));
    public static final DateTimeFormatter DATE_FORMATTER_NEW_YORK_WITH_TIME = DateTimeFormat.forPattern(YYYYMMDD_HHMMSS).withZone(DateTimeZone.forID(TRADING_TIME_ZONE));
    public static final SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yy HH:mm z");
    public static final NumberFormat nf = new DecimalFormat("#########.##");

    private static final String IT_S_A_WEEKEND_SETTING_TRADING_DATE_TO = "It's a weekend; setting trading date to {0}.";

    private static final int DAY_MILLISECONDS = 86400000;

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private static DateTime useDate;
    private static User systemUser;
    private static Key systemUserKey;

    private static final SimpleDateFormat expiresSimpleDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

    @SuppressWarnings("resource")
    public static String getFileAsString(final String path) {
        final byte[] buffer = new byte[(int) new File(path).length()];
        FileInputStream f;
        try {
            f = new FileInputStream(path);
            f.read(buffer);
        } catch (final Exception e) {
            log.warning(e.getMessage());
        }
        return new String(buffer);
    }

    @SuppressWarnings("resource")
    public static byte[] getFile(final String path) {
        final byte[] buffer = new byte[(int) new File(path).length()];
        FileInputStream f;
        try {
            f = new FileInputStream(path);
            f.read(buffer);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static Date toNewYorkTimeStartOfDay(final Date date) {
        return stripTime(new DateTime(date)).toDateTime(getTradingTimeZone()).plusHours(9).toDate();
    }

    public static Date xtoNewYorkTimeEndOfDay(final Date date) {
        return stripTime(new DateTime(date)).toDateTime(getTradingTimeZone()).plusHours(17).toDate();
    }

    public static DateTime getNewYorkTime() {
        final DateTime now = DateTime.now(getTradingTimeZone());
        return now;
    }

    public static DateTimeZone getTradingTimeZone() {
        return DateTimeZone.forID(TRADING_TIME_ZONE);
    }

    public static boolean isMarketOpen() {
        final DateTime date = getNewYorkTime();
        return isMarketOpen(date);
    }

    public static boolean isMarketOpen(final Date date) {
        return isMarketOpen(toDateTime(date));
    }

    private static boolean isMarketOpen(final DateTime date) {
        final boolean isTradingDay = isTradingDay(date);
        if (!isTradingDay) {
            return false;
        }

        return isTradingHour(date);
    }

    public static boolean isTradingHour(final Date date) {
        return isTradingHour(toDateTime(date));
    }

    private static boolean isTradingHour(final DateTime date) {
        final int hour = date.toDateTime(getTradingTimeZone()).getHourOfDay();
        if (hour < 8 || hour > 17) {
            return false;
        }
        return true;
    }

    public static boolean isTradingDay(final Date date) {
        return isTradingDay(toDateTime(date));
    }

    private static boolean isTradingDay(final DateTime date) {
        final int day = date.toDateTime(getTradingTimeZone()).getDayOfWeek();
        switch (day) {
        case DateTimeConstants.SATURDAY:
        case DateTimeConstants.SUNDAY:
            return false;
        }
        return true;
    }

    public static boolean isDateAfter(final Date date1, final Date date2) {
        final DateTime dateTime1 = toDateTime(date1);
        final DateTime dateTime2 = toDateTime(date2);

        final DateMidnight midnight1 = dateTime1.toDateMidnight();
        final DateMidnight midnight2 = dateTime2.toDateMidnight();

        final boolean after = midnight1.isAfter(midnight2);
        return after;
    }

    public static Date yesterday() {
        return yesterday(getDate());
    }

    public static Date yesterday(final Date date) {
        return new DateTime(date, DateTimeZone.forID(Utils.TRADING_TIME_ZONE)).toDateMidnight().minusDays(1).toDate();

    }

    private static DateTime yesterday(final DateTime date) {
        return date.toDateTime(getTradingTimeZone()).toDateMidnight().toDateTime();
    }

    public static Date lastTradingDay() {
        return lastTradingDay(toDateTime()).toDate();
    }

    public static Date lastTradingDay(final Date date) {
        return lastTradingDay(toDateTime(date)).toDate();
    }

    private static DateTime lastTradingDay(DateTime date) {
        final DateTime now = toDateTime();
        if (date.isAfter(now)) {
            date = now;
        }

        date = date.minusDays(1);
        date = stripTime(date);

        for (int i = 0; i < 30; i++) {
            if (isTradingDay(date)) {
                break;
            }
            date = date.minusDays(1);
        }

        return date;
    }

    public static boolean isYesterday(final Date date) {
        return isYesterday(toDateTime(date));
    }

    private static boolean isYesterday(DateTime date) {
        final DateTime yesterday = yesterday(toDateTime());
        date = date.toDateMidnight().toDateTime();

        final boolean equals = yesterday.equals(date);
        return equals;
    }

    public static boolean isWeekend() {
        return isWeekend(toDateTime());
    }

    public static boolean isWeekend(final Date date) {
        return isWeekend(new DateTime(date, getTradingTimeZone()));
    }

    private static boolean isWeekend(final DateTime date) {
        final int day = date.getDayOfWeek();

        switch (day) {
        case DateTimeConstants.SATURDAY:
        case DateTimeConstants.SUNDAY:
            return true;
        }

        return false;
    }

    public static int days(final Date start, final Date end) {
        return days(start, end, true);
    }

    public static int days(final Date start, final Date end, final boolean inclusive) {
        final long startTime = start.getTime();
        final long endTime = end.getTime();
        final long diff = endTime - startTime;

        final Long days = diff / DAY_MILLISECONDS;
        int intValue = days.intValue();
        if (inclusive) {
            if (diff >= 0) {
                intValue++;
            } else {
                intValue--;
            }
        }
        return intValue;
    }

    public static boolean isBefore(DateTime date) {
        final DateTime now = toDateTime();
        date = stripTime(date);
        return date.isBefore(now);
    }

    private static DateTime toDateTime(final Date date) {
        return new DateTime(date);
    }

    public static Date stripTime(final Date date) {
        return new DateTime(date, getTradingTimeZone()).toDateMidnight().toDate();
    }

    private static DateTime stripTime(final DateTime date) {
        return date.toDateTime(getTradingTimeZone()).toDateMidnight().toDateTime();
    }

    public static Object[] toArray(final Object... objs) {
        return objs;
    }

    public static int getDiffYears(final Date first, final Date last) {
        return getDiffYears(toDateTime(first), toDateTime(last));
    }

    public static int getDiffYears(DateTime first, DateTime last) {
        first = stripTime(first);
        last = stripTime(last);

        final int years = last.getYear() - first.getYear();
        return years;
    }

    public static int getDiffQuarters(final Date first, final Date last) {
        return getDiffQuarters(toDateTime(first), toDateTime(last));
    }

    public static int getDiffQuarters(DateTime first, DateTime last) {
        first = stripTime(first);
        last = stripTime(last);

        final PeriodType monthDay = PeriodType.yearMonthDay().withYearsRemoved();
        final Period difference = new Period(first, last, monthDay);
        final int months = difference.getMonths();

        return (int) Math.ceil(months / 3.0);
    }

    public static int toInt(final String value) {
        if (value == null) {
            return 0;
        }

        int i = 0;
        try {
            i = Integer.parseInt(value);
        } catch (final Exception e) {
        }

        return i;
    }

    public static Date getDateAdd(final Date date, final int years) {
        return getDateAdd(toDateTime(date), years).toDate();
    }

    public static DateTime getDateAdd(DateTime date, final int years) {
        date = stripTime(date);
        return date.plusYears(years);
    }

    public static DateTime getDateAddMonths(DateTime date, final int months) {
        date = stripTime(date);
        return date.plusMonths(months);
    }

    public static Date getDateAddMonths(final Date date, final int days) {
        return getDateAddMonths(toDateTime(date), days).toDate();
    }

    public static Date getDateAddDays(final Date date, final int days) {
        return getDateAddDays(toDateTime(date), days).toDate();
    }

    private static DateTime getDateAddDays(DateTime date, final int days) {
        date = stripTime(date);
        return date.plusDays(days);
    }

    public static String getFormattedDate() {
        return DATE_FORMATTER_NEW_YORK_WITHOUT_TIME2.print(getDate().getTime());
    }

    public static String getFormattedDate(final Date date) {
        return DATE_FORMATTER_NEW_YORK_WITHOUT_TIME2.print(date.getTime());
    }

    public static Date getDate() {
        return toDate().toDate();
    }

    public static DateTime toDateTime() {
        return getNewYorkTime();
    }

    public static DateTime toDate() {
        if (useDate != null) {
            return useDate;
        }
        return getNewYorkTime();
    }

    public static Date toDate(final String source, final String format) {
        return DateTime.parse(source, DateTimeFormat.forPattern(format)).toDate();
    }

    public static Date toDate(final String source, final int days) throws Exception {
        DateTime dateTime = toDateTime(source);
        if (days > 0) {
            dateTime = dateTime.plus(days);
        } else if (days < 0) {
            dateTime = dateTime.minus(days);
        }
        return dateTime.toDate();
    }

    public static Date toDate(final Date date, final int days) throws Exception {
        DateTime dateTime = toDateTime(date);
        if (days > 0) {
            dateTime = dateTime.plusDays(Math.abs(days));
        } else if (days < 0) {
            dateTime = dateTime.minusDays(Math.abs(days));
        }
        return dateTime.toDate();
    }

    public static Date toDate(final String source) throws Exception {
        return toDateTime(source).toDate();
    }

    public static DateTime toDateTime(final String source) throws Exception {
        DateTime parsed = null;
        if (source.length() == 8) {
            parsed = DateTime.parse(source, DATE_FORMATTER_NEW_YORK_WITHOUT_TIME);
        } else if (source.length() == 10) {
            parsed = DateTime.parse(source, DATE_FORMATTER_NEW_YORK_WITHOUT_TIME2);
        } else if (source.length() == 17) {
            parsed = DateTime.parse(source, DATE_FORMATTER_NEW_YORK_WITH_TIME);
        } else if (source.length() == 18) {
            parsed = toDateTime(sdf.parse(source));
        } else {
            throw new Exception("Date format '" + source + "' not supported.");
        }
        return parsed;
    }

    public static boolean isToday(DateTime date) {
        DateTime now = toDateTime();
        now = stripTime(now);
        date = stripTime(date);
        return date.equals(now);
    }

    public static boolean isLess(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.getTime() < date2.getTime();
    }

    public static boolean isLessOrEqual(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.getTime() <= date2.getTime();
    }

    public static boolean isGreater(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.getTime() > date2.getTime();
    }

    public static boolean isGreaterOrEqual(final Date date1, final Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.getTime() >= date2.getTime();
    }

    public static boolean isBetween(final Date date0, final Date date1, final Date date2) {
        if (date0 == null || date1 == null || date2 == null) {
            return false;
        }

        final long time0 = date0.getTime();
        final long time1 = date1.getTime();
        final long time2 = date2.getTime();

        return time1 <= time0 && time0 <= time2;
    }

    public static void setDate(final Date date) throws Exception {
        if (date == null) {
            useDate = null;
        } else {
            useDate = toDateTime(date);
        }
    }

    public static void setDate(final String source) throws Exception {
        if (source == null) {
            useDate = null;
        } else {
            setDate(toDateTime(source).toDate());
        }
    }

    public static boolean isToday(final Date date) {
        return isToday(toDateTime(date));
    }

    public static Gson getGson() {
        final GsonBuilder builder = com.maintainer.util.Utils.getGsonBuilder();

        final JsonSerializer<User> userSerializer = new JsonSerializer<User>() {
            @Override
            public JsonElement serialize(final User user, final Type typeOfSrc, final JsonSerializationContext context) {
                if (user == null) return null;

                final JsonObject object = new JsonObject();
                if (user.getKey() != null) {
                    object.add("id", new JsonPrimitive(user.getKey().toString()));
                }

                if (user.getUsername() != null) {
                    object.add("username", new JsonPrimitive(user.getUsername()));
                }

                if (user.getAccountNumber() != null) {
                    object.add("accountNumber", new JsonPrimitive(user.getAccountNumber()));
                }

                return object;
            }
        };

        builder.registerTypeAdapter(User.class, userSerializer);

        return builder.create();
    }

    public static Cursor getCursor(final String property, final String value, final String app, final String kind) throws Exception {
        final CompiledCursor compiledCursor = new DatastorePb.CompiledCursor();
        final Position position = new DatastorePb.CompiledCursor.Position();
        final PositionIndexValue positionIndexValue = new PositionIndexValue();
        positionIndexValue.setProperty(property);
        final PropertyValue propertyValue = new PropertyValue();
        propertyValue.setStringValue(value);
        positionIndexValue.setValue(propertyValue);
        position.addIndexValue(positionIndexValue);
        compiledCursor.setPosition(position);

        final Reference reference = new OnestoreEntity.Reference();
        reference.setApp(app);

        final Path path = new OnestoreEntity.Path();
        final Element element = new Path.Element();
        element.setType(kind);
        element.setName(value);
        path.addElement(element);
        reference.setPath(path);

        position.setKey(reference);
        position.setStartInclusive(false);

        final Constructor<Cursor> constructor = Cursor.class.getDeclaredConstructor(DatastorePb.CompiledCursor.class);
        constructor.setAccessible(true);

        final Cursor cursor = constructor.newInstance(new Object[] { compiledCursor });
        return cursor;
    }

    public static String getStackTrace(final Exception e) {
        final StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        final String stacktrace = sw.toString();
        return stacktrace;
    }

    public static boolean isEmpty(final String string) {
        return string == null || string.trim().length() == 0;
    }

    public static String createTask(final MyResourceRouter router, final Request request, final String resourceName, final byte[] payload, final String mimeType) throws Exception {
        Method taskMethod = null;

        final org.restlet.data.Method method = request.getMethod();
        if (method.equals(org.restlet.data.Method.POST)) {
            taskMethod = Method.POST;
        } else if (method.equals(org.restlet.data.Method.PUT)) {
            taskMethod = Method.PUT;
        } else if (method.equals(org.restlet.data.Method.GET)) {
            taskMethod = Method.GET;
        }

        return createTask(router, request, taskMethod, resourceName, payload, mimeType);
    }

    public static String createTask(final MyResourceRouter router, final Request request, final Method taskMethod, final String resourceName, final byte[] payload, final String mimeType) throws Exception {
        final String credentialsCookieName = router.getCrentialsCookieName();
        final String cookie = request.getCookies().getFirstValue(credentialsCookieName);

        final String cookieHeader = credentialsCookieName + "=" + cookie;
        final String url = "/data/" + resourceName;

        return createTask(url, taskMethod, cookieHeader, payload, mimeType);
    }

    private static String createTask(final String url, final Method taskMethod, final String cookieHeader, final byte[] payload, final String mimeType) {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", cookieHeader);

        final TaskOptions taskOptions = createTaskOptions(url, taskMethod, headers, payload, mimeType);
        return createTask(taskOptions);
    }

    public static String createTask(final TaskOptions taskOptions) {
        final Queue queue = QueueFactory.getDefaultQueue();
        final TaskHandle handle = queue.add(taskOptions);
        return handle.toString();
    }

    public static TaskOptions createTaskOptions(final String url, final Method taskMethod, final Map<String, String> headers, final byte[] payload, final String mimeType) {
        final TaskOptions taskOptions = TaskOptions.Builder.withUrl(url);
        taskOptions.method(taskMethod);

        taskOptions.retryOptions(RetryOptions.Builder.withTaskRetryLimit(0));

        for (final Entry<String, String> e : headers.entrySet()) {
            final String key = e.getKey();
            final String value = e.getValue();
            taskOptions.header(key, value);
        }

        if (payload != null) {
            taskOptions.payload(payload, mimeType);
        }
        return taskOptions;
    }

    public static boolean isValid(final Object object) {
        return (object != null && !Undefined.class.isAssignableFrom(object.getClass()) && !object.equals(Double.NaN) && !object.equals(Double.POSITIVE_INFINITY) && !object.equals(Double.NEGATIVE_INFINITY)
                && !object.equals(UniqueTag.NOT_FOUND) && !object.equals(UniqueTag.NULL_VALUE));
    }

    public static void severe(final Logger log, final Exception e) {
        log.severe(e.getMessage());
        log.severe(getStackTrace(e));
    }

    public static void severe(final Logger log, final String message) {
        log.severe(message);
    }

    @SuppressWarnings("unchecked")
    public static User getUser(final Key key) throws Exception {
        if (key == null) return null;
        final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
        final User user = users.get(key);
        return user;
    }

    public static List<String> getVariables(final Script script) throws Exception {
        final Class<? extends Script> scriptClass = script.getClass();
        Field field = scriptClass.getDeclaredField("idata");
        field.setAccessible(true);

        final DebuggableScript idata = (DebuggableScript) field.get(script);
        final Class<? extends DebuggableScript> idataClass = idata.getClass();
        field = idataClass.getDeclaredField("itsStringTable");
        field.setAccessible(true);

        String[] list = (String[]) field.get(idata);
        if (list != null) {
            final List<String> strings = new LinkedList<String>(Arrays.asList(list));

            field = idataClass.getDeclaredField("argNames");
            field.setAccessible(true);

            list = (String[]) field.get(idata);
            final List<String> args = new LinkedList<String>(Arrays.asList(list));

            strings.removeAll(args);
            return strings;
        }
        return Collections.emptyList();
    }

    public static String getResourceName(final Request request) {
        final ArrayList<Resource> resources = com.maintainer.util.Utils.getResources(request);
        final String resourceName = resources.get(0).getResource();
        return resourceName;
    }

    @SuppressWarnings("unchecked")
    public static Key getSystemUserKey() throws Exception {
        if (systemUserKey != null) return systemUserKey;

        final MemcacheService cache = MyMemcacheServiceFactory.getMemcacheService();
        Key key = (Key) cache.get(SYSTEM_USER_KEY);

        if (key == null) {
            final User system = getSystemUser();
            if (system != null) {
                key = system.getKey();
                cache.put(SYSTEM_USER_KEY, key);
            }
        }

        systemUserKey = key;

        return systemUserKey;
    }

    @SuppressWarnings("unchecked")
    public static User getSystemUser() throws Exception {
        if (systemUser != null) return systemUser;

        final MemcacheService cache = MyMemcacheServiceFactory.getMemcacheService();
        User user = (User) cache.get(SYSTEM_USER);

        if (user == null) {
            final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(User.class);
            final Query query = new Query(User.class);
            query.filter("username", SYSTEM_USERNAME);
            final List<User> list = users.find(query);
            if (!list.isEmpty()) {
                user = list.get(0);
                cache.put(SYSTEM_USER, user);
            }
        }

        systemUser = user;

        return systemUser;
    }

    private static Object convertToDoubleIfPossible(Object value) {
        try {
            value = com.maintainer.util.Utils.convert(value, Double.class);
        } catch (final Exception e) {
            // ignore this as it could be a formatted string
        }
        return value;
    }

    private static boolean hasChanged(final String s1, final String s2) {
        if (Utils.isEmpty(s1) && Utils.isEmpty(s2)) {
            return false;
        } else if (Utils.isEmpty(s1) && !Utils.isEmpty(s2)) {
            return true;
        } else if (!Utils.isEmpty(s1) && Utils.isEmpty(s2)) {
            return true;
        }

        return !s1.equals(s2);
    }

    public static String formatRestletCredentials(final String identifier, final char[] secret) throws GeneralSecurityException {
        // Data buffer
        final StringBuffer sb = new StringBuffer();

        // Indexes buffer
        final StringBuffer isb = new StringBuffer();
        final String timeIssued = Long.toString(System.currentTimeMillis());
        int i = timeIssued.length();
        sb.append(timeIssued);

        isb.append(i);

        sb.append('/');
        sb.append(identifier);

        i += identifier.length() + 1;
        isb.append(',').append(i);

        sb.append('/');
        sb.append(secret);

        // Store indexes at the end of the string
        sb.append('/');
        sb.append(isb);

        return Base64.encode(CryptoUtils.encrypt("AES", "MyExtraSecretKey".getBytes(), sb.toString()), false);
    }

    public static String getUserIdCookie(final String userid) {
        final String cookie = MessageFormat.format("{0}={1}; path={2}; HttpOnly", "userid", userid, "/");
        return cookie;
    }

    // From:
    // http://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private static Matcher matcher;

    /**
     * Validate hex with regular expression
     *
     * @param hex
     *            hex for validation
     * @return true valid hex, false invalid hex
     */
    public static boolean isValidEmailAddress(final String hex) {
        matcher = pattern.matcher(hex);
        return matcher.matches();
    }

    private static boolean initGlobalContextCalled = false;

    private static void initGlobalContext() {
        if (initGlobalContextCalled)
            return;

        initGlobalContextCalled = true;
        ContextFactory.initGlobal(new SandboxContextFactory(new SandboxShutter() {

            @Override
            public boolean allowClassAccess(final Class<?> type) {
                if (
                        type.getName().startsWith("java.lang")
                        ) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean allowFieldAccess(final Class<?> type, final Object instance, final String fieldName) {
                return false;
            }

            @Override
            public boolean allowMethodAccess(final Class<?> type, final Object instance, final String methodName) {
                return true;
            }

            @Override
            public boolean allowStaticFieldAccess(final Class<?> type, final String fieldName) {
                return false;
            }

            @Override
            public boolean allowStaticMethodAccess(final Class<?> type, final String methodName) {
                return false;
            }
        }));
    };

    public static Context getContext() {
        initGlobalContext();

        final Context cx = Context.enter();
        cx.setOptimizationLevel(-1);
        return cx;
    }

    public static void log(final String message, final Object...params) {
        //        final Entity log = new Entity("Log");
        //        log.setUnindexedProperty("activity", MessageFormat.format(message, params));
        //        log.setUnindexedProperty("time", new Date().toString());
        //        DatastoreServiceFactory.getAsyncDatastoreService().put(log);
    }

    public static Object executeScript(final Script script, final TimerContext cx, final Scriptable scope, final long expires) {
        cx.startTimer(expires);
        return script.exec(cx, scope);
    }

    public static String getUniqueId() {
        final UUID uuid = java.util.UUID.randomUUID();
        final String hash = uuid.toString();
        return hash;
    }

    public static Object[] jsToJava(final NativeArray arr) {
        final Object [] array = new Object[(int) arr.getLength()];
        for (final Object o : arr.getIds()) {
            final int index = (Integer) o;
            final Object object2 = arr.get(index, null);
            if (Utils.isValid(object2)) {
                array[index] = object2;
            }
        }
        return array;
    }

    @SuppressWarnings("rawtypes")
    public static void stripIds(Object o1) throws Exception {
        if (o1 == null) return;

        Class<? extends Object> clazz = o1.getClass();
        if (Map.class.isAssignableFrom(clazz)) {
            o1 = ((Map) o1).values();
            clazz = o1.getClass();
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            final Iterator iterator = ((Collection)o1).iterator();
            while (iterator.hasNext()) {
                final Object o2 = iterator.next();
                stripIds(o2);
            }
        } else if (EntityImpl.class.isAssignableFrom(clazz)){
            final MyField field = com.maintainer.util.Utils.getField(o1, "id");
            field.setAccessible(true);
            if (field != null) {
                field.set(o1, null);
            }

            final Field[] fields = clazz.getDeclaredFields();
            for (final Field f : fields) {
                f.setAccessible(true);
                final Object o2 = f.get(o1);
                stripIds(o2);
            }
        }
    }

    public static void setUser(final Object target, final EntityImpl value) throws Exception {
        setProperty(target, value, "user");
    }

    public static void setParent(final Object target, final EntityImpl value) throws Exception {
        setProperty(target, value, "parent");
    }

    @SuppressWarnings("rawtypes")
    public static void setProperty(Object target, final EntityImpl value, final String fieldName) throws Exception {
        if (target == null) return;

        Class<? extends Object> clazz = target.getClass();
        if (Map.class.isAssignableFrom(clazz)) {
            target = ((Map) target).values();
            clazz = target.getClass();
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            final Iterator iterator = ((Collection)target).iterator();
            while (iterator.hasNext()) {
                final Object o2 = iterator.next();
                if (o2 != null && EntityImpl.class.isAssignableFrom(o2.getClass())) {
                    setProperty(o2, value, fieldName);
                }
            }
        } else if (EntityImpl.class.isAssignableFrom(clazz)){
            final MyField field = com.maintainer.util.Utils.getField(target, fieldName);
            field.setAccessible(true);
            if (field != null) {
                final Object existing = field.get(target);
                if (existing == null) {
                    if (field.getType().isAssignableFrom(value.getClass())) {
                        field.set(target, value);
                    }
                }
            }

            final Field[] fields = clazz.getDeclaredFields();
            for (final Field f : fields) {
                f.setAccessible(true);
                final Object o2 = f.get(target);
                setProperty(o2, value, fieldName);
            }
        }
    }

    public static String getExpires(final Date expiration) {
        final String expires = expiresSimpleDateFormat.format(expiration);
        return expires;
    }

    @SuppressWarnings("unchecked")
    public static User getUserByIdentifier(final String identifier) throws Exception {
        final MemcacheService cache = MyMemcacheServiceFactory.getMemcacheService();
        User user = (User) cache.get(identifier);

        if (user == null) {
            final DataProvider<User> users = (DataProvider<User>) DataProviderFactory.instance().getDataProvider(com.nobodyelses.data.model.User.class);
            final Query q = new Query(User.class);
            q.filter("username", identifier);
            final List<User> list = users.find(q);
            if (!list.isEmpty()) {
                user = list.get(0);
                cache.put(identifier, user);
            }
        }
        return user;
    }

    public static void sendUnauthorized(final HttpServletResponse resp) throws Exception {
        sendUnauthorized(resp, "Credentials");
    }

    public static void sendUnauthorized(final HttpServletResponse resp, final String credentialsCookieName) throws Exception {
        clearCredentialsCookie(resp, credentialsCookieName);
        //resp.addHeader("Set-Cookie", "userid=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT");
        resp.setStatus(UNAUTHORIZED);
        resp.getOutputStream().write("Unauthorized".getBytes());
    }

    public static void clearCredentialsCookie(final HttpServletResponse resp, final String credentialsCookieName) {
        resp.addHeader("Set-Cookie", credentialsCookieName + "=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }

    public static String getBody(final HttpServletRequest req) throws IOException {
        String body = null;
        final ServletInputStream inputStream = req.getInputStream();
        final java.util.Scanner scanner = new java.util.Scanner(inputStream);
        try {
            final Scanner useDelimiter = scanner.useDelimiter("\\A");
            if (useDelimiter.hasNext()) {
                body = useDelimiter.next();
            }
        } finally {
            scanner.close();
        }
        return body;
    }
}
