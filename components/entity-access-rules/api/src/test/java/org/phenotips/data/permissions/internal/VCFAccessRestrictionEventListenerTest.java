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
package org.phenotips.data.permissions.internal;

import org.phenotips.data.permissions.AccessLevel;
import org.phenotips.data.permissions.EntityAccess;
import org.phenotips.data.permissions.EntityPermissionsManager;
import org.phenotips.entities.PrimaryEntity;
import org.phenotips.entities.PrimaryEntityResolver;

import org.xwiki.bridge.event.ActionExecutingEvent;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.XWikiServletRequest;

import static org.mockito.Mockito.when;

/**
 * Tests for the {@link VCFAccessRestrictionEventListener}.
 *
 * @version $Id$
 */
public class VCFAccessRestrictionEventListenerTest
{
    @Rule
    public final MockitoComponentMockingRule<EventListener> mocker =
        new MockitoComponentMockingRule<>(VCFAccessRestrictionEventListener.class);

    private ActionExecutingEvent event = new ActionExecutingEvent("download");

    @Mock
    private XWikiDocument doc;

    private DocumentReference docRef = new DocumentReference("xwiki", "data", "P0000001");

    @Mock
    private XWikiContext context;

    @Mock
    private XWikiServletRequest request;

    private PrimaryEntityResolver resolver;

    @Mock
    private PrimaryEntity primaryEntity;

    private EntityPermissionsManager permissions;

    private AccessLevel edit;

    @Mock
    private EntityAccess access;

    @Before
    public void setUp() throws ComponentLookupException
    {
        MockitoAnnotations.initMocks(this);
        this.resolver = this.mocker.getInstance(PrimaryEntityResolver.class);
        when(this.resolver.resolveEntity("xwiki:data.P0000001")).thenReturn(this.primaryEntity);
        this.permissions = this.mocker.getInstance(EntityPermissionsManager.class);
        when(this.permissions.getEntityAccess(this.primaryEntity)).thenReturn(this.access);
        this.edit = this.mocker.getInstance(AccessLevel.class, "edit");

        when(this.context.getRequest()).thenReturn(this.request);
        when(this.context.getDoc()).thenReturn(this.doc);
        when(this.doc.getDocumentReference()).thenReturn(this.docRef);
    }

    @Test
    public void listensForDownloadActions() throws ComponentLookupException
    {
        List<Event> events = this.mocker.getComponentUnderTest().getEvents();
        Assert.assertTrue(events.contains(new ActionExecutingEvent("download")));
        Assert.assertTrue(events.contains(new ActionExecutingEvent("downloadrev")));
    }

    @Test
    public void hasName() throws ComponentLookupException
    {
        String name = this.mocker.getComponentUnderTest().getName();
        Assert.assertTrue(StringUtils.isNotBlank(name));
        Assert.assertFalse("default".equals(name));
    }

    @Test
    public void forbidsDownloadingVcfAttachmentsIfAccessIsLowerThanEdit() throws ComponentLookupException
    {
        when(this.request.getRequestURI()).thenReturn("/bin/download/data/P0000001/file.vcf");
        when(this.access.hasAccessLevel(this.edit)).thenReturn(false);
        this.mocker.getComponentUnderTest().onEvent(this.event, this.doc, this.context);
        Assert.assertTrue(this.event.isCanceled());
    }

    @Test
    public void allowsDownloadingVcfAttachmentsWhenHasEditAccess() throws ComponentLookupException
    {
        when(this.request.getRequestURI()).thenReturn("/bin/download/data/P0000001/file.vcf");
        when(this.access.hasAccessLevel(this.edit)).thenReturn(true);
        this.mocker.getComponentUnderTest().onEvent(this.event, this.doc, this.context);
        Assert.assertFalse(this.event.isCanceled());
    }

    @Test
    public void alwaysAllowsDownloadingNonVcfAttachments() throws ComponentLookupException
    {
        when(this.request.getRequestURI()).thenReturn("/bin/download/data/P0000001/file.png");
        when(this.access.hasAccessLevel(this.edit)).thenReturn(false);
        this.mocker.getComponentUnderTest().onEvent(this.event, this.doc, this.context);
        Assert.assertFalse(this.event.isCanceled());
    }

    @Test
    public void ignoresExtraFileparts() throws ComponentLookupException
    {
        when(this.request.getRequestURI()).thenReturn("/bin/download/data/P0000001/file.vcf/hacked");
        when(this.access.hasAccessLevel(this.edit)).thenReturn(false);
        this.mocker.getComponentUnderTest().onEvent(this.event, this.doc, this.context);
        Assert.assertTrue(this.event.isCanceled());
    }
}
