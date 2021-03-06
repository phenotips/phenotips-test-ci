/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.security.audit.script;

import org.phenotips.Constants;
import org.phenotips.security.audit.AuditEvent;
import org.phenotips.security.audit.AuditStore;
import org.phenotips.security.authorization.AuthorizationService;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.script.service.ScriptService;
import org.xwiki.security.authorization.Right;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

/**
 * Provides access to {@link AuditEvent audit events}.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("audit")
@Singleton
public class AuditScriptService implements ScriptService
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);

    @Inject
    private AuditStore store;

    @Inject
    private UserManager users;

    @Inject
    private AuthorizationService auth;

    @Inject
    @Named("currentmixed")
    private DocumentReferenceResolver<EntityReference> resolver;

    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> resolverd;

    /**
     * Retrieves all the events affecting a specific entity. Requires edit rights on the target entity.
     *
     * @param entity a reference to the target entity
     * @return a list of audited events, may be empty
     */
    @Nonnull
    public List<AuditEvent> getEventsForEntity(DocumentReference entity)
    {
        if (this.auth.hasAccess(this.users.getCurrentUser(), Right.EDIT, entity)) {
            return this.store.getEventsForEntity(entity);
        }
        return Collections.emptyList();
    }

    /**
     * Retrieves all the events generated by a specific user. Requires admin rights.
     *
     * @param userId the user whose events to retrieve, may be {@code null}
     * @return a list of audited events, may be empty
     */
    @Nonnull
    public List<AuditEvent> getEventsForUser(String userId)
    {
        if (this.auth.hasAccess(this.users.getCurrentUser(), Right.ADMIN,
            this.resolver.resolve(Constants.XWIKI_SPACE_REFERENCE))) {
            return this.store.getEventsForUser(this.users.getUser(userId));
        }
        return Collections.emptyList();
    }

    /**
     * Retrieves all the events generated by a specific user, coming from a specific IP. Requires admin rights.
     *
     * @param userId the user whose events to retrieve, may be {@code null}
     * @param ip the ip where the request came from
     * @return a list of audited events, may be empty
     */
    @Nonnull
    public List<AuditEvent> getEventsForUser(String userId, String ip)
    {
        if (this.auth.hasAccess(this.users.getCurrentUser(), Right.ADMIN,
            this.resolver.resolve(Constants.XWIKI_SPACE_REFERENCE))) {
            return this.store.getEventsForUser(this.users.getUser(userId), ip);
        }
        return Collections.emptyList();
    }

    /**
     * Retrieves audit events for filter parameters. Parameters {@code fromTime} and {@code toTime} define an interval
     * for the time stamp. Events matching all non-null fields from the template exactly will be returned. Requires
     * admin rights.
     *
     * @param start for large result set paging, the index of the first event to display in the returned page
     * @param number for large result set paging, how many events to display in the returned page
     * @param action the event type, for example {@code view}, {@code edit}, {@code export}, empty (meaning all)
     * @param userId the user whose events to retrieve, may be {@code null}, if empty events for all users returned
     * @param ip the ip where the request came from, if empty events for all ips returned
     * @param entityId a reference to the target entity
     * @param fromTime start of the interval for the time stamp filter. If parameter fromTime is {@code null}, matching
     *            events from the beginning will be retrieved.
     * @param toTime end of the interval for the time stamp filter. If parameter toTime is {@code null}, matching events
     *            until the present moment will be retrieved.
     * @return a list of audited events, may be empty
     */
    @Nonnull
    @SuppressWarnings("checkstyle:ParameterNumber")
    public List<AuditEvent> getEvents(int start, int number, String action, String userId, String ip, String entityId,
        String fromTime, String toTime)
    {
        if (this.auth.hasAccess(this.users.getCurrentUser(), Right.ADMIN,
            this.resolver.resolve(Constants.XWIKI_SPACE_REFERENCE))) {
            Calendar from = null;
            try {
                Date d = DATE_FORMAT.parse(fromTime);
                from = Calendar.getInstance();
                from.setTime(d);
            } catch (Exception e) {
                // Nothing to do for bad input, leave it as null
            }

            Calendar to = null;
            try {
                Date d = DATE_FORMAT.parse(toTime);
                to = Calendar.getInstance();
                to.setTime(d);
            } catch (Exception e) {
                // Nothing to do for bad input, leave it as null
            }

            DocumentReference entity = entityId != null ? this.resolverd.resolve(entityId) : null;
            User user = userId != null ? this.users.getUser(userId) : null;
            String actionId = StringUtils.isNotBlank(action) ? action : null;
            String ipValue = StringUtils.isNotBlank(ip) ? ip : null;

            AuditEvent eventTemplate = new AuditEvent(user, ipValue, actionId, null, entity, null);
            return this.store.getEvents(eventTemplate, from, to, start, number);
        }
        return Collections.emptyList();
    }

    /**
     * Counts all the events for filter parameters. Parameters fromTime and toTime define an interval for the time
     * stamp. Requires admin rights.
     *
     * @param action the event type, for example {@code view}, {@code edit}, {@code export}, empty (meaning all)
     * @param userId the user whose events to retrieve, may be {@code null}, if empty events for all users returned
     * @param ip the ip where the request came from, if empty events for all ips returned
     * @param entityId a reference to the target entity
     * @param fromTime start of the interval for the time stamp filter. If parameter fromTime is {@code null}, matching
     *            events from the beginning will be retrieved.
     * @param toTime end of the interval for the time stamp filter. If parameter toTime is {@code null}, matching events
     *            until the present moment will be retrieved.
     * @return total number of events satisfying template and time criteria, or {@code -1} for insufficient rights or
     *         internal errors
     */
    public long countEvents(String action, String userId, String ip, String entityId, String fromTime, String toTime)
    {
        if (this.auth.hasAccess(this.users.getCurrentUser(), Right.ADMIN,
            this.resolver.resolve(Constants.XWIKI_SPACE_REFERENCE))) {
            Calendar from = null;
            try {
                Date d = DATE_FORMAT.parse(fromTime);
                from = Calendar.getInstance();
                from.setTime(d);
            } catch (Exception e) {
                // Nothing to do for bad input, leave it as null
            }

            Calendar to = null;
            try {
                Date d = DATE_FORMAT.parse(toTime);
                to = Calendar.getInstance();
                to.setTime(d);
            } catch (Exception e) {
                // Nothing to do for bad input, leave it as null
            }

            DocumentReference entity = StringUtils.isNotBlank(entityId) ? this.resolverd.resolve(entityId) : null;
            User user = StringUtils.isNotBlank(userId) ? this.users.getUser(userId) : null;
            String actionId = StringUtils.isNotBlank(action) ? action : null;
            String ipValue = StringUtils.isNotBlank(ip) ? ip : null;

            AuditEvent eventTemplate = new AuditEvent(user, ipValue, actionId, null, entity, null);
            return this.store.countEvents(eventTemplate, from, to);
        }
        return -1;
    }
}
