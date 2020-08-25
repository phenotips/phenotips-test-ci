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
package org.phenotips.studies.family.script;

import org.phenotips.studies.family.Family;
import org.phenotips.studies.family.FamilyTools;
import org.phenotips.studies.family.Pedigree;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Script service for working with families. All methods assume actions are performed by current user and do
 * corresponding permission checks.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Singleton
@Named("families")
public class FamilyScriptService implements ScriptService
{
    @Inject
    @Named("secure")
    private FamilyTools familyTools;

    /**
     * Creates an empty family.
     *
     * @return Family object corresponding to the newly created family.
     */
    public Family create()
    {
        return this.familyTools.createFamily();
    }

    /**
     * Creates an empty family.
     *
     * @return Family object corresponding to the newly created family.
     */
    public Family createFamily()
    {
        return create();
    }

    /**
     * Returns family object, or null if doesn't exist or current user has no rights.
     *
     * @param id a PhenotTips family ID
     * @return Family object of the family with the given id, or null if familyId is not valid or current user does not
     *         have permissions to view the family.
     */
    public Family get(String id)
    {
        return this.familyTools.getFamilyById(id);
    }

    /**
     * Returns family object, or null if doesn't exist or current user has no rights.
     *
     * @param familyId a PhenotTips family ID
     * @return Family object of the family with the given id, or null if familyId is not valid or current user does not
     *         have permissions to view the family.
     */
    public Family getFamilyById(String familyId)
    {
        return get(familyId);
    }

    /**
     * Returns family pedigree. Essentially this is a shortcut for getFamilyById().getPedigree() with a check that
     * family is not null.
     *
     * @param familyId must be a valid family id
     * @return pedigree object or null if no such family, no pedigree or no view rights for the family.
     */
    public Pedigree getPedigreeForFamily(String familyId)
    {
        return this.familyTools.getPedigreeForFamily(familyId);
    }

    /**
     * Returns a family ID the patient belongs to.
     *
     * @param patientId id of the patient
     * @return Id of the, or null if patient does not belong to the family or current user has no view rights for the
     *         patient.
     */
    public Family getFamilyForPatient(String patientId)
    {
        return this.familyTools.getFamilyForPatient(patientId);
    }

    /**
     * Returns patient's pedigree, which is the pedigree of a family that patient belongs to.
     *
     * @param patientId id of the patient
     * @return Id of the, or null if patient does not belong to the family or current user has no view rights for the
     *         patient.
     */
    public Pedigree getPedigreeForPatient(String patientId)
    {
        return this.familyTools.getPedigreeForPatient(patientId);
    }

    /**
     * Removes a patient from the family, modifying the both the family and patient records to reflect the change.
     *
     * @param patientId of the patient to delete
     * @return true if patient was removed. false if not, for example, if the patient is not associated with a family,
     *         or if current user has no delete rights
     */
    public boolean removeMember(String patientId)
    {
        return this.familyTools.removeMember(patientId);
    }

    /**
     * Adds a patient to the family, modifying both the family and patient records to reflect the change.
     *
     * @param patientId of the patient to add
     * @param familyId of the family to add patient to
     * @return true if patient was added. false if not, for example, if the patient is already in another family,
     *         or if current user has no edit rights
     */
    public boolean addMember(String patientId, String familyId)
    {
        return this.familyTools.addMember(patientId, familyId);
    }

    /**
     * Deletes a family record, modifying all member patient records to reflect the change. No patient records are
     * deleted.
     *
     * @param family the family record to delete
     * @return {@code true} if successful; {@code false} if the user does not have the right to delete the family record
     *         or the deletion fails
     */
    public boolean delete(Family family)
    {
        return deleteFamily(family.getId(), false);
    }

    /**
     * Delete family, modifying the both the family and patient records to reflect the change.
     *
     * @param familyId of the family to delete
     * @param deleteAllMembers indicator whether to delete all family member documents as well
     * @return true if successful; false if deletion failed or current user has not enough rights
     */
    public boolean deleteFamily(String familyId, boolean deleteAllMembers)
    {
        return this.familyTools.deleteFamily(familyId, deleteAllMembers);
    }

    /**
     * Checks if the current user can delete the family (or the family and all the members).
     *
     * @param familyId of the family to delete
     * @param deleteAllMembers indicator whether to check delete permissions on all family member documents as well
     * @return true if successful
     */
    public boolean canDeleteFamily(String familyId, boolean deleteAllMembers)
    {
        return this.familyTools.currentUserCanDeleteFamily(familyId, deleteAllMembers);
    }
}
