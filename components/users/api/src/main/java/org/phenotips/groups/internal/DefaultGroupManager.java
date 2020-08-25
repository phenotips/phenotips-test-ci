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
package org.phenotips.groups.internal;

import org.phenotips.groups.Group;
import org.phenotips.groups.GroupManager;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.stability.Unstable;
import org.xwiki.users.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiGroupService;

/**
 * Default implementation for {@link GroupManager}, using XDocuments as the place where groups are defined.
 *
 * @version $Id$
 * @since 1.0M9
 */
@Unstable
@Component
@Singleton
public class DefaultGroupManager implements GroupManager
{
    /** The space where groups are stored. */
    private static final EntityReference GROUP_SPACE = new EntityReference("Groups", EntityType.SPACE);

    private static final EntityReference USER_CLASS = new EntityReference("XWikiUsers", EntityType.DOCUMENT,
        new EntityReference(XWiki.SYSTEM_SPACE, EntityType.SPACE));

    /** Logging helper. */
    @Inject
    private Logger logger;

    /** Used for searching for groups. */
    @Inject
    private QueryManager qm;

    @Inject
    private Execution execution;

    /** Solves partial group references in the current wiki. */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> resolver;

    @Inject
    @Named("userOrGroup")
    private DocumentReferenceResolver<String> userOrGroupResolver;

    @Inject
    private DocumentAccessBridge bridge;

    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> compactSerializer;

    @Override
    public Set<Group> getGroupsForUser(User user)
    {
        if (user == null || user.getProfileDocument() == null) {
            return Collections.emptySet();
        }

        DocumentReference profile = user.getProfileDocument();

        Set<Group> result = new LinkedHashSet<>();
        try {
            Query q =
                this.qm.createQuery("from doc.object(XWiki.XWikiGroups) grp where grp.member in (:u, :su)", Query.XWQL);
            q.bindValue("u", profile.toString());
            q.bindValue("su", this.compactSerializer.serialize(profile));
            List<Object> groups = q.execute();
            List<Object> nestedGroups = new ArrayList<>(groups);
            while (!nestedGroups.isEmpty()) {
                StringBuilder qs = new StringBuilder("from doc.object(XWiki.XWikiGroups) grp where grp.member in (");
                for (int i = 0; i < nestedGroups.size(); ++i) {
                    if (i > 0) {
                        qs.append(',');
                    }
                    qs.append('?').append(i * 2 + 1).append(",?").append(i * 2 + 2);
                }
                qs.append(')');
                q = this.qm.createQuery(qs.toString(), Query.XWQL);
                for (int i = 0; i < nestedGroups.size(); ++i) {
                    String formalGroupName =
                        this.resolver.resolve(String.valueOf(nestedGroups.get(i)), GROUP_SPACE).toString();
                    String shortGroupName = this.compactSerializer
                        .serialize(this.resolver.resolve(String.valueOf(nestedGroups.get(i)), GROUP_SPACE));
                    q.bindValue(i * 2 + 1, formalGroupName);
                    q.bindValue(i * 2 + 2, shortGroupName);
                }
                nestedGroups = q.execute();
                nestedGroups.removeAll(groups);
                groups.addAll(nestedGroups);
            }
            q = this.qm.createQuery(
                "from doc.object(XWiki.XWikiGroups) grp, doc.object(PhenoTips.PhenoTipsGroupClass) phgrp", Query.XWQL);
            groups.retainAll(q.execute());
            for (Object groupName : groups) {
                result.add(getGroup(String.valueOf(groupName)));
            }
        } catch (QueryException ex) {
            this.logger.warn("Failed to search for user's groups: {}", ex.getMessage());
        }

        return Collections.unmodifiableSet(result);
    }

    @Override
    public Group getGroup(String name)
    {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        DocumentReference groupReference = this.resolver.resolve(name, GROUP_SPACE);
        return new DefaultGroup(groupReference);
    }

    @Override
    public Group getGroup(DocumentReference groupReference)
    {
        if (groupReference == null) {
            return null;
        }
        return new DefaultGroup(groupReference);
    }

    @Override
    public Set<Document> getAllMembersForGroup(String name)
    {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        Set<Document> result = new LinkedHashSet<>();

        try {
            XWikiContext context = getXWikiContext();
            XWikiGroupService groupService = context.getWiki().getGroupService(context);

            List<String> nestedGroups = new ArrayList<>();
            nestedGroups.add(name);

            while (!nestedGroups.isEmpty()) {
                String groupName = nestedGroups.get(0);
                nestedGroups.remove(0);
                Collection<String> members = groupService.getAllMembersNamesForGroup(groupName, 0, 0, context);
                for (String memberName : members) {
                    EntityReference userOrGroup = this.userOrGroupResolver.resolve(memberName);
                    XWikiDocument doc = (XWikiDocument) this.bridge.getDocument((DocumentReference) userOrGroup);
                    if (doc.getXObject(Group.CLASS_REFERENCE) != null || doc.getXObject(USER_CLASS) != null) {
                        if (doc.getXObject(Group.CLASS_REFERENCE) != null) {
                            nestedGroups.add(memberName);
                        }
                        result.add(doc.newDocument(context));
                    }
                }
            }
        } catch (Exception e) {
            //
        }

        return Collections.unmodifiableSet(result);
    }

    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }
}
