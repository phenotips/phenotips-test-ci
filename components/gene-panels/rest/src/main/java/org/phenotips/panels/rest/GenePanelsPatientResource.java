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
package org.phenotips.panels.rest;

import org.phenotips.data.rest.PatientResource;
import org.phenotips.rest.ParentResource;
import org.phenotips.rest.Relation;
import org.phenotips.rest.RequiredAccess;

import org.xwiki.stability.Unstable;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Patient resource for working with gene panels data.
 *
 * @version $Id$
 * @since 1.3
 */
@Unstable("New API introduced in 1.3")
@Path("/patients/{patient-id}/suggested-gene-panels")
@Relation("https://phenotips.org/rel/genePanelsPatient")
@ParentResource(PatientResource.class)
public interface GenePanelsPatientResource
{
    /**
     * Retrieves a JSON representation of genes associated with the stored HPO terms for a patient that has the given
     * {@code patientId internal ID}, as well as the count of the number of phenotypes each gene may be associated with.
     *
     * @param patientId the internal ID of the patient of interest
     * @param excludeRejectedGenes iff true, rejected genes will be excluded from results
     * @param withMatchCount set to true iff the number of genes available for term should be counted
     * @return a JSON representation of genes and their counts data if successful, an error code otherwise
     * @since 1.4 (modified)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RequiredAccess("view")
    Response getPatientGeneCounts(
        @PathParam("patient-id") String patientId,
        @QueryParam("exclude-rejected-genes") @DefaultValue("false") boolean excludeRejectedGenes,
        @QueryParam("with-match-count") @DefaultValue("false") boolean withMatchCount);
}
