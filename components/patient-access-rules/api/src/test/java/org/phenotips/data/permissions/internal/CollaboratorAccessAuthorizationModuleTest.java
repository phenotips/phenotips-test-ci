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

import org.phenotips.data.Patient;
import org.phenotips.data.permissions.AccessLevel;
import org.phenotips.data.permissions.EntityAccess;
import org.phenotips.entities.PrimaryEntityResolver;
import org.phenotips.security.authorization.AuthorizationModule;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.security.authorization.ManageRight;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.users.User;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

/**
 * Tests for the {@link CollaboratorAccessAuthorizationModule collaborator granted access} {@link AuthorizationModule}
 * component.
 *
 * @version $Id$
 */
public class CollaboratorAccessAuthorizationModuleTest
{
    @Rule
    public final MockitoComponentMockingRule<AuthorizationModule> mocker =
        new MockitoComponentMockingRule<>(CollaboratorAccessAuthorizationModule.class);

    @Mock
    private User user;

    @Mock
    private Patient patient;

    @Mock
    private EntityAccess pAccess;

    private DocumentReference doc = new DocumentReference("xwiki", "data", "P01");

    @Mock
    private DocumentReference userProfile;

    @Mock
    private AccessLevel noAccess;

    @Mock
    private AccessLevel viewAccess;

    @Mock
    private AccessLevel editAccess;

    @Mock
    private AccessLevel manageAccess;

    private EntityAccessManager helper;

    private PrimaryEntityResolver resolver;

    @Before
    public void setupMocks() throws ComponentLookupException
    {
        MockitoAnnotations.initMocks(this);
        this.helper = this.mocker.getInstance(EntityAccessManager.class);

        when(this.noAccess.getGrantedRight()).thenReturn(Right.ILLEGAL);
        when(this.viewAccess.getGrantedRight()).thenReturn(Right.VIEW);
        when(this.editAccess.getGrantedRight()).thenReturn(Right.EDIT);
        when(this.manageAccess.getGrantedRight()).thenReturn(ManageRight.MANAGE);

        this.resolver = this.mocker.getInstance(PrimaryEntityResolver.class);
        when(this.resolver.resolveEntity("xwiki:data.P01")).thenReturn(this.patient);

        when(this.user.getProfileDocument()).thenReturn(this.userProfile);
    }

    @Test
    public void manageCollaboratorGrantedViewEditCommentDeleteAndManage() throws ComponentLookupException
    {
        when(this.helper.getAccessLevel(this.patient, this.userProfile)).thenReturn(this.manageAccess);
        Assert.assertTrue(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.VIEW, this.doc));
        Assert.assertTrue(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.EDIT, this.doc));
        Assert.assertTrue(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.COMMENT, this.doc));
        Assert.assertTrue(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.DELETE, this.doc));
        Assert.assertTrue(this.mocker.getComponentUnderTest().hasAccess(this.user, ManageRight.MANAGE, this.doc));
    }

    @Test
    public void editCollaboratorGrantedViewAndEdit() throws ComponentLookupException
    {
        when(this.helper.getAccessLevel(this.patient, this.userProfile)).thenReturn(this.editAccess);
        Assert.assertTrue(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.VIEW, this.doc));
        Assert.assertTrue(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.EDIT, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.COMMENT, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.DELETE, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, ManageRight.MANAGE, this.doc));
    }

    @Test
    public void viewCollaboratorGrantedView() throws ComponentLookupException
    {
        when(this.helper.getAccessLevel(this.patient, this.userProfile)).thenReturn(this.viewAccess);
        Assert.assertTrue(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.VIEW, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.EDIT, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.COMMENT, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.DELETE, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, ManageRight.MANAGE, this.doc));
    }

    @Test
    public void noActionForNonCollaborator() throws ComponentLookupException
    {
        when(this.helper.getAccessLevel(this.patient, this.userProfile)).thenReturn(this.noAccess);
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.VIEW, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.EDIT, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.COMMENT, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.DELETE, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, ManageRight.MANAGE, this.doc));
    }

    @Test
    public void noActionWithNonPatient() throws ComponentLookupException
    {
        when(this.resolver.resolveEntity("xwiki:data.P01")).thenReturn(null);
        when(this.helper.getAccessLevel(this.patient, this.userProfile)).thenReturn(this.editAccess);
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.VIEW, this.doc));
    }

    @Test
    public void noActionForGuestUser() throws ComponentLookupException
    {
        when(this.helper.getAccessLevel(this.patient, null)).thenReturn(this.editAccess);
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(null, Right.VIEW, this.doc));
    }

    @Test
    public void noActionForNullRight() throws ComponentLookupException
    {
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, null, this.doc));
    }

    @Test
    public void noActionForNullDocument() throws ComponentLookupException
    {
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.VIEW, null));
    }

    @Test
    public void noActionWithNonDocumentRight() throws ComponentLookupException
    {
        when(this.helper.getAccessLevel(this.patient, this.userProfile)).thenReturn(this.manageAccess);
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.REGISTER, this.doc));
        Assert.assertNull(this.mocker.getComponentUnderTest().hasAccess(this.user, Right.PROGRAM, this.doc));
    }

    @Test
    public void expectedPriority() throws ComponentLookupException
    {
        Assert.assertEquals(300, this.mocker.getComponentUnderTest().getPriority());
    }
}
