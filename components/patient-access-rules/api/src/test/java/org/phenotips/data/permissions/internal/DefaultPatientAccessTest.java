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
import org.phenotips.data.permissions.Collaborator;
import org.phenotips.data.permissions.Owner;
import org.phenotips.data.permissions.PatientAccess;
import org.phenotips.data.permissions.Visibility;
import org.phenotips.data.permissions.internal.access.EditAccessLevel;
import org.phenotips.data.permissions.internal.access.ManageAccessLevel;
import org.phenotips.data.permissions.internal.access.NoAccessLevel;
import org.phenotips.data.permissions.internal.access.OwnerAccessLevel;
import org.phenotips.data.permissions.internal.access.ViewAccessLevel;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the default {@link PatientAccess} implementation, {@link DefaultPatientAccess}.
 *
 * @version $Id$
 */
public class DefaultPatientAccessTest
{
    /** The user used as the owner of the patient. */
    private static final DocumentReference OWNER = new DocumentReference("xwiki", "XWiki", "padams");

    /** The owner of the patient, when owned by OWNER. */
    private static final Owner OWNER_OBJECT = new DefaultOwner(OWNER, mock(EntityAccessHelper.class));

    /** The owner of the patient, when owned by guest. */
    private static final Owner GUEST_OWNER_OBJECT = new DefaultOwner(null, mock(EntityAccessHelper.class));

    /** The user used as a collaborator. */
    private static final DocumentReference COLLABORATOR = new DocumentReference("xwiki", "XWiki", "hmccoy");

    /** The user used as a non-collaborator. */
    private static final DocumentReference OTHER_USER = new DocumentReference("xwiki", "XWiki", "cxavier");

    /** Basic tests for {@link PatientAccess#getPatient()}. */
    @Test
    public void getPatient()
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Assert.assertSame(p, pa.getPatient());
    }

    /** Basic tests for {@link PatientAccess#getOwner()}. */
    @Test
    public void getOwner()
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Assert.assertSame(OWNER_OBJECT, pa.getOwner());
        Assert.assertSame(OWNER, pa.getOwner().getUser());
    }

    @Test
    public void getOwnerWithGuestOwner() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        when(am.getOwner(p)).thenReturn(GUEST_OWNER_OBJECT);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Assert.assertSame(GUEST_OWNER_OBJECT, pa.getOwner());
        Assert.assertSame(null, pa.getOwner().getUser());
    }

    /** Basic tests for {@link PatientAccess#isOwner()}. */
    @Test
    public void isOwner() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        when(h.getCurrentUser()).thenReturn(OWNER);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Assert.assertTrue(pa.isOwner());

        when(h.getCurrentUser()).thenReturn(OTHER_USER);
        Assert.assertFalse(pa.isOwner());
    }

    /** {@link PatientAccess#isOwner()} with guest as the current user always returns false. */
    @Test
    public void isOwnerForGuests() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        when(h.getCurrentUser()).thenReturn(null);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Assert.assertFalse(pa.isOwner());

        // False even if the owner cannot be computed
        when(am.getOwner(p)).thenReturn(null);
        Assert.assertFalse(pa.isOwner());
    }

    /** {@link PatientAccess#isOwner()} with guest as the current user returns true if the owner is also guest. */
    @Test
    public void isOwnerWithGuestOwnerForGuests() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        when(am.getOwner(p)).thenReturn(GUEST_OWNER_OBJECT);
        when(h.getCurrentUser()).thenReturn(null);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Assert.assertTrue(pa.isOwner());
    }

    /** {@link PatientAccess#isOwner()} for guest owners and a non-guest user returns false. */
    @Test
    public void isOwnerWithGuestOwnerForOtherUsers() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        when(am.getOwner(p)).thenReturn(GUEST_OWNER_OBJECT);
        when(h.getCurrentUser()).thenReturn(OTHER_USER);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Assert.assertFalse(pa.isOwner());
    }

    /** Basic tests for {@link PatientAccess#isOwner(org.xwiki.model.reference.EntityReference)}. */
    @Test
    public void isOwnerWithSpecificUser() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Assert.assertTrue(pa.isOwner(OWNER));
        Assert.assertFalse(pa.isOwner(OTHER_USER));
        Assert.assertFalse(pa.isOwner(null));
    }

    /** Basic tests for {@link PatientAccess#setOwner(org.xwiki.model.reference.EntityReference)}. */
    @Test
    public void setOwner() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        when(am.setOwner(p, OTHER_USER)).thenReturn(true);
        Assert.assertTrue(pa.setOwner(OTHER_USER));
    }

    /** Basic tests for {@link PatientAccess#getVisibility()}. */
    @Test
    public void getVisibility() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        Visibility v = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(v);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Assert.assertSame(v, pa.getVisibility());
    }

    /** Basic tests for {@link PatientAccess#setVisibility(Visibility)}. */
    @Test
    public void setVisibility() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Visibility v = mock(Visibility.class);
        when(vm.setVisibility(p, v)).thenReturn(true);
        Assert.assertTrue(pa.setVisibility(v));
    }

    /** Basic tests for {@link PatientAccess#getCollaborators()}. */
    @Test
    public void getCollaborators() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Collection<Collaborator> collaborators = new HashSet<>();
        when(am.getCollaborators(p)).thenReturn(collaborators);
        Assert.assertSame(collaborators, pa.getCollaborators());
    }

    /** Basic tests for {@link PatientAccess#updateCollaborators(Collection)}. */
    @Test
    public void updateCollaborators() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        Collection<Collaborator> collaborators = new HashSet<>();
        when(am.setCollaborators(p, collaborators)).thenReturn(true);
        Assert.assertTrue(pa.updateCollaborators(collaborators));
    }

    /**
     * Basic tests for {@link PatientAccess#addCollaborator(org.xwiki.model.reference.EntityReference, AccessLevel)}.
     */
    @Test
    public void addCollaborator() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper h = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, h, am, vm);
        when(am.addCollaborator(Matchers.same(p), Matchers.any(Collaborator.class))).thenReturn(true);
        Assert.assertTrue(pa.addCollaborator(COLLABORATOR, mock(AccessLevel.class)));
    }

    /** Basic tests for {@link PatientAccess#removeCollaborator(Collaborator)}. */
    @Test
    public void removeCollaborator() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);

        when(am.removeCollaborator(Matchers.same(p), Matchers.any(Collaborator.class))).thenReturn(true);
        Assert.assertTrue(pa.removeCollaborator(COLLABORATOR));

        Collaborator collaborator = mock(Collaborator.class);
        when(am.removeCollaborator(p, collaborator)).thenReturn(true);
        Assert.assertTrue(pa.removeCollaborator(collaborator));
    }

    /** {@link PatientAccess#getAccessLevel()} returns no access for guest users. */
    @Test
    public void getAccessLevelWithGuestUser() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        when(helper.getCurrentUser()).thenReturn(null);
        Visibility publicV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(publicV);
        AccessLevel view = new ViewAccessLevel();
        when(publicV.getDefaultAccessLevel()).thenReturn(view);
        AccessLevel none = new NoAccessLevel();
        when(am.resolveAccessLevel("none")).thenReturn(none);
        Assert.assertSame(none, pa.getAccessLevel());
    }

    /** {@link PatientAccess#getAccessLevel()} returns the default visibility access for guest users. */
    @Test
    public void getAccessLevelWithGuestUserAndPrivateVisibility() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        when(helper.getCurrentUser()).thenReturn(null);
        Visibility privateV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(privateV);
        AccessLevel none = new NoAccessLevel();
        when(privateV.getDefaultAccessLevel()).thenReturn(none);
        when(am.resolveAccessLevel("none")).thenReturn(none);
        Assert.assertSame(none, pa.getAccessLevel());
    }

    /** {@link PatientAccess#getAccessLevel()} returns owner access for guest users and guest owner. */
    @Test
    public void getAccessLevelWithGuestUserAndGuestOwner() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(GUEST_OWNER_OBJECT);
        when(helper.getCurrentUser()).thenReturn(null);
        Visibility privateV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(privateV);
        AccessLevel none = new NoAccessLevel();
        when(privateV.getDefaultAccessLevel()).thenReturn(none);
        AccessLevel owner = new OwnerAccessLevel();
        when(am.resolveAccessLevel("owner")).thenReturn(owner);
        Assert.assertSame(owner, pa.getAccessLevel());
    }

    /** {@link PatientAccess#getAccessLevel()} returns Owner access for the owner. */
    @Test
    public void getAccessLevelWithOwner() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        when(helper.getCurrentUser()).thenReturn(OWNER);
        AccessLevel owner = new OwnerAccessLevel();
        when(am.resolveAccessLevel("owner")).thenReturn(owner);
        Assert.assertSame(owner, pa.getAccessLevel());
    }

    /** {@link PatientAccess#getAccessLevel()} returns Owner access for site administrators. */
    @Test
    public void getAccessLevelWithAdministrator() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        when(helper.getCurrentUser()).thenReturn(OTHER_USER);
        when(am.isAdministrator(p, OTHER_USER)).thenReturn(true);
        AccessLevel owner = new OwnerAccessLevel();
        when(am.resolveAccessLevel("owner")).thenReturn(owner);
        Assert.assertSame(owner, pa.getAccessLevel());
    }

    /** {@link PatientAccess#getAccessLevel()} returns the specified collaborator access for a collaborator. */
    @Test
    public void getAccessLevelWithCollaborator() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        AccessLevel edit = new EditAccessLevel();
        when(am.isAdministrator(p, COLLABORATOR)).thenReturn(false);
        when(helper.getCurrentUser()).thenReturn(COLLABORATOR);
        when(am.getAccessLevel(p, COLLABORATOR)).thenReturn(edit);
        Visibility publicV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(publicV);
        AccessLevel view = new ViewAccessLevel();
        when(publicV.getDefaultAccessLevel()).thenReturn(view);
        Assert.assertSame(edit, pa.getAccessLevel());
    }

    /**
     * {@link PatientAccess#getAccessLevel(EntityReference)} returns the specified collaborator access for a
     * collaborator.
     */
    @Test
    public void getAccessLevelForCollaboratorAsAdministrator() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        AccessLevel edit = new EditAccessLevel();
        when(am.isAdministrator(p, COLLABORATOR)).thenReturn(false);
        when(helper.getCurrentUser()).thenReturn(OTHER_USER);
        when(am.getAccessLevel(p, COLLABORATOR)).thenReturn(edit);
        Visibility publicV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(publicV);
        AccessLevel view = new ViewAccessLevel();
        when(publicV.getDefaultAccessLevel()).thenReturn(view);
        Assert.assertSame(edit, pa.getAccessLevel(COLLABORATOR));
    }

    /**
     * {@link PatientAccess#getAccessLevel(EntityReference)} returns owner access for an administrator.
     */
    @Test
    public void getAccessLevelForAdministratorAsCollaborator() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        AccessLevel edit = new EditAccessLevel();
        when(am.isAdministrator(p, OTHER_USER)).thenReturn(true);
        when(helper.getCurrentUser()).thenReturn(COLLABORATOR);
        when(am.getAccessLevel(p, OTHER_USER)).thenReturn(edit);
        Visibility publicV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(publicV);
        AccessLevel owner = new OwnerAccessLevel();
        when(am.resolveAccessLevel("owner")).thenReturn(owner);
        Assert.assertSame(owner, pa.getAccessLevel(OTHER_USER));
    }

    /** {@link PatientAccess#getAccessLevel()} returns the default visibility access for non-collaborators. */
    @Test
    public void getAccessLevelWithOtherUser() throws ComponentLookupException
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        AccessLevel none = new NoAccessLevel();
        when(am.isAdministrator(p, OTHER_USER)).thenReturn(false);
        when(helper.getCurrentUser()).thenReturn(OTHER_USER);
        when(am.getAccessLevel(p, OTHER_USER)).thenReturn(none);
        Visibility publicV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(publicV);
        AccessLevel view = new ViewAccessLevel();
        when(publicV.getDefaultAccessLevel()).thenReturn(view);
        Assert.assertSame(view, pa.getAccessLevel());
    }

    /**
     * {@link PatientAccess#hasAccessLevel(AccessLevel)} returns {@code true} for lower or same access levels than the
     * current user's access, {@code false} for higher levels.
     */
    @Test
    public void hasAccessLevel()
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        when(helper.getCurrentUser()).thenReturn(COLLABORATOR);
        AccessLevel edit = new EditAccessLevel();
        when(am.getAccessLevel(p, COLLABORATOR)).thenReturn(edit);
        Visibility publicV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(publicV);
        AccessLevel view = new ViewAccessLevel();
        when(publicV.getDefaultAccessLevel()).thenReturn(view);
        Assert.assertTrue(pa.hasAccessLevel(view));
        Assert.assertTrue(pa.hasAccessLevel(edit));
        Assert.assertFalse(pa.hasAccessLevel(new ManageAccessLevel()));
        Assert.assertFalse(pa.hasAccessLevel(new OwnerAccessLevel()));
    }

    /**
     * {@link PatientAccess#hasAccessLevel(AccessLevel)} returns {@code true} for lower or same access levels than the
     * specified user's access, {@code false} for higher levels.
     */
    @Test
    public void hasAccessLevelForOtherUser()
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        AccessLevel edit = new EditAccessLevel();
        when(am.getAccessLevel(p, COLLABORATOR)).thenReturn(edit);
        Visibility publicV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(publicV);
        AccessLevel view = new ViewAccessLevel();
        when(publicV.getDefaultAccessLevel()).thenReturn(view);
        Assert.assertTrue(pa.hasAccessLevel(COLLABORATOR, view));
        Assert.assertTrue(pa.hasAccessLevel(COLLABORATOR, edit));
        Assert.assertFalse(pa.hasAccessLevel(COLLABORATOR, new ManageAccessLevel()));
        Assert.assertFalse(pa.hasAccessLevel(COLLABORATOR, new OwnerAccessLevel()));
    }

    @Test
    public void hasAccessLevelForGuestUsers()
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(OWNER_OBJECT);
        when(helper.getCurrentUser()).thenReturn(null);
        AccessLevel edit = new EditAccessLevel();
        when(am.getAccessLevel(p, COLLABORATOR)).thenReturn(edit);
        Visibility publicV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(publicV);
        AccessLevel view = new ViewAccessLevel();
        when(publicV.getDefaultAccessLevel()).thenReturn(view);
        when(am.resolveAccessLevel("none")).thenReturn(new NoAccessLevel());
        Assert.assertFalse(pa.hasAccessLevel(view));
        Assert.assertFalse(pa.hasAccessLevel(edit));
        Assert.assertFalse(pa.hasAccessLevel(new ManageAccessLevel()));
        Assert.assertFalse(pa.hasAccessLevel(new OwnerAccessLevel()));
    }

    @Test
    public void hasAccessLevelForGuestUsersAsOwners()
    {
        Patient p = mock(Patient.class);
        EntityAccessHelper helper = mock(EntityAccessHelper.class);
        EntityAccessManager am = mock(EntityAccessManager.class);
        EntityVisibilityManager vm = mock(EntityVisibilityManager.class);
        PatientAccess pa = new DefaultPatientAccess(p, helper, am, vm);
        when(am.getOwner(p)).thenReturn(GUEST_OWNER_OBJECT);
        when(helper.getCurrentUser()).thenReturn(null);
        AccessLevel edit = new EditAccessLevel();
        Visibility publicV = mock(Visibility.class);
        when(vm.getVisibility(p)).thenReturn(publicV);
        AccessLevel view = new ViewAccessLevel();
        when(publicV.getDefaultAccessLevel()).thenReturn(view);
        when(am.resolveAccessLevel("none")).thenReturn(new NoAccessLevel());
        when(am.resolveAccessLevel("owner")).thenReturn(new OwnerAccessLevel());
        Assert.assertTrue(pa.hasAccessLevel(view));
        Assert.assertTrue(pa.hasAccessLevel(edit));
        Assert.assertTrue(pa.hasAccessLevel(new ManageAccessLevel()));
        Assert.assertTrue(pa.hasAccessLevel(new OwnerAccessLevel()));
    }

    /** {@link PatientAccess#toString()} is customized. */
    @Test
    public void toStringTest()
    {
        Patient p = mock(Patient.class);
        PatientAccess pa = new DefaultPatientAccess(p, null, null, null);
        when(p.getDocumentReference()).thenReturn(new DocumentReference("xwiki", "data", "P123"));
        Assert.assertEquals("Access rules for xwiki:data.P123", pa.toString());
    }

    /** {@link PatientAccess#toString()} is customized. */
    @Test
    public void toStringWithNullPatient()
    {
        PatientAccess pa = new DefaultPatientAccess(null, null, null, null);
        Assert.assertEquals("Access rules for <unknown patient>", pa.toString());
    }
}
