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
package org.phenotips.security.audit.rest.internal;

import org.phenotips.Constants;
import org.phenotips.security.audit.AuditEvent;
import org.phenotips.security.audit.AuditStore;
import org.phenotips.security.audit.rest.AuditResource;
import org.phenotips.security.authorization.AuthorizationService;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rest.XWikiResource;
import org.xwiki.security.authorization.Right;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Default implementation for {@link AuditResource} using XWiki's support for REST resources.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("org.phenotips.security.audit.rest.internal.DefaultAuditResource")
@Singleton
public class DefaultAuditResource extends XWikiResource implements AuditResource
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);

    @Inject
    private AuditStore auditStore;

    @Inject
    private UserManager users;

    @Inject
    private AuthorizationService auth;

    @Inject
    @Named("currentmixed")
    private DocumentReferenceResolver<EntityReference> resolver;

    /** Resolves unprefixed document names to the current wiki. */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> resolverd;

    @Override
    @SuppressWarnings("checkstyle:ParameterNumber")
    public Response listEvents(int start, int number, String action, String userId, String ip, String entityId,
        String fromTime, String toTime)
    {
        if (!this.auth.hasAccess(this.users.getCurrentUser(), Right.ADMIN,
            this.resolver.resolve(Constants.XWIKI_SPACE_REFERENCE))) {
            this.slf4Jlogger.debug("Activity logs access denied to user [{}]", this.users.getCurrentUser());
            return Response.status(Status.FORBIDDEN).build();
        }

        JSONArray eventsList = new JSONArray();
        AuditEvent eventTemplate = setTemplate(action, userId, ip, entityId);
        List<AuditEvent> results = getResults(eventTemplate, fromTime, toTime, start, number);
        for (AuditEvent event : results) {
            eventsList.put(event.toJSON());
        }

        JSONObject response = new JSONObject();
        response.put("data", eventsList);

        return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
    }

    private AuditEvent setTemplate(String action, String userId, String ip, String entityId)
    {
        DocumentReference entity = StringUtils.isNotBlank(entityId) ? this.resolverd.resolve(entityId) : null;
        User user = StringUtils.isNotBlank(userId) ? this.users.getUser(userId) : null;
        String actionId = StringUtils.isNotBlank(action) ? action : null;
        String ipValue = StringUtils.isNotBlank(ip) ? ip : null;

        AuditEvent eventTemplate = new AuditEvent(user, ipValue, actionId, null, entity, null);

        return eventTemplate;
    }

    private List<AuditEvent> getResults(AuditEvent eventTemplate, String fromTime, String toTime, int start, int number)
    {
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

        List<AuditEvent> results = this.auditStore.getEvents(eventTemplate, from, to, start, number);
        return results;
    }
}
