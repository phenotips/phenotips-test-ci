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
package org.phenotips.data.rest;

import org.phenotips.rest.PATCH;
import org.phenotips.rest.ParentResource;
import org.phenotips.rest.RequiredAccess;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Resource for working with patient records where patients records are identified and manipulated using an arbitrary
 * label and a corresponding identifier value, fields of the {@code LabeledIdentifierClass}.
 *
 * @version $Id$
 */
@Path("/patients/labeled-eid/{label}/{id}")
@ParentResource(PatientsResource.class)
public interface PatientByLabeledExternalIdentifierResource
{
    /**
     * Retrieve a patient record, identified by an arbitrary label and its corresponding identifier value, in its JSON
     * representation. If the indicated label doesn't exist, if the indicated patient record doesn't exist, or if the
     * user sending the request doesn't have the right to view the target patient record, an error is returned. If
     * multiple records exist with  with the same given identifier for the given label, a list of links to each such
     * record is returned.
     *
     * @param label an arbitrary label, either exists in all patients, or exists in at least one patient
     * @param id the patient's given external identifier for the given label
     * @return the JSON representation of the requested patient, or a status message in case of error
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequiredAccess("view")
    Response getPatient(@PathParam("label") String label, @PathParam("id") String id);

    /**
     * Update a patient record, identified by an arbitrary label and its corresponding identifier value, from its JSON
     * representation. If the user sending the request doesn't have the right to edit the target patient record, no
     * change is performed and an error is returned. If the indicated patient record doesn't exist, however a valid
     * JSON is provided with the query parameter "create" set to true, then a new patient record is created with the
     * provided data; otherwise no change is performed and an error is returned. If multiple records exist with the
     * same given identifier for the given label, no change is performed, and a list of links to each such record is
     * returned. If a field is set in the patient record, but missing in the JSON, then that field is not changed.
     *
     * @param json the JSON representation of the new patient to add
     * @param label an arbitrary label, either exists in all patients, or exists in at least one patient
     * @param id the patient's given external identifier for the given label
     * @param policy the policy according to which patient data should be written
     * @param create whether a patient record should be created given none exist for the label and id provided
     * @return a status message
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiredAccess("edit")
    Response updatePatient(String json, @PathParam("label") String label, @PathParam("id") String id,
        @QueryParam("policy") @DefaultValue("update") String policy,
        @QueryParam("create") @DefaultValue("false") boolean create);

    /**
     * Update a patient record, identified by an arbitrary label and its corresponding identifier value, from its JSON
     * representation. Existing patient data is merged with the provided JSON representation, if possible. If the user
     * sending the request doesn't have the right to edit the target patient record, no change is performed and an
     * error is returned. If the indicated patient record doesn't exist, however a valid JSON is provided with the
     * query parameter "create" set to true, then a new patient record is created with the provided data; otherwise no
     * change is performed and an error is returned. If multiple records exist with the same given identifier for the
     * given label, no change is performed, and a list of links to each such record is returned. If a field is set in
     * the patient record, but missing in the JSON, then that field is not changed.
     *
     * @param json the JSON representation of the new patient to add
     * @param label an arbitrary label, either exists in all patients, or exists in at least one patient
     * @param id the patient's given external identifier for the given label
     * @param create whether a patient record should be created given none exist for the label and id provided
     * @return a status message
     */
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @RequiredAccess("edit")
    Response patchPatient(String json, @PathParam("label") String label, @PathParam("id") String id,
        @QueryParam("create") @DefaultValue("false") boolean create);

    /**
     * Delete a patient record, identified by an arbitrary label and its corresponding identifier value. If the
     * indicated patient record doesn't exist, or if the user sending the request doesn't have the right to delete the
     * target patient record, no change is performed and an error is returned. If multiple records exist with the same
     * given identifier for the given label, no change is performed, and a list of links to each such record is
     * returned.
     *
     * @param label an arbitrary label, either exists in all patients, or exists in at least one patient
     * @param id the patient's given external identifier for the given label
     * @return a status message
     */
    @DELETE
    @RequiredAccess("edit")
    Response deletePatient(@PathParam("label") String label, @PathParam("id") String id);
}
