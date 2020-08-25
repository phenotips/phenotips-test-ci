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
import org.phenotips.data.PatientRepository;
import org.phenotips.data.permissions.AccessLevel;
import org.phenotips.data.permissions.EntityPermissionsManager;
import org.phenotips.security.authorization.AuthorizationModule;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.security.authorization.Right;
import org.xwiki.users.User;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.ObjectUtils;

/**
 * Implementation that allows all document access to the owner of a patient.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("owner-access")
@Singleton
public class OwnerAccessAuthorizationModule implements AuthorizationModule
{
    /** Checks to see if a document is a patient. */
    @Inject
    private PatientRepository patientRepository;

    /** Checks to see if the user is the owner. */
    @Inject
    private EntityPermissionsManager manager;

    /** Minimal access level for granting access. */
    @Inject
    @Named("owner")
    private AccessLevel ownerAccess;

    @Override
    public int getPriority()
    {
        return 400;
    }

    @Override
    public Boolean hasAccess(User user, Right access, EntityReference entity)
    {
        if (!ObjectUtils.allNotNull(access, entity) || access.getTargetedEntityType() == null
            || !access.getTargetedEntityType().contains(EntityType.DOCUMENT)) {
            return null;
        }

        // This converts the document to a patient.
        Patient patient = this.patientRepository.get(entity.toString());
        if (patient == null) {
            return null;
        }

        AccessLevel grantedAccess =
            this.manager.getEntityAccess(patient).getAccessLevel(user != null ? user.getProfileDocument() : null);

        if (this.ownerAccess.compareTo(grantedAccess) <= 0) {
            return true;
        }

        return null;
    }
}
