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
package org.phenotips.panels;

import org.phenotips.data.Patient;
import org.phenotips.vocabulary.VocabularyTerm;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * A factory for objects of {@link GenePanel} type.
 *
 * @version $Id$
 * @since 1.3
 */
@Role
@Unstable("New API introduced in 1.3")
public interface GenePanelFactory
{
    /**
     * Specifies whether the created panel should keep track of the number of genes for each present term. This is
     * false by default.
     *
     * @param generate the created panel will keep track of the number of genes for each present term iff true
     * @return the current {@link GenePanelFactory} object
     * @since 1.4
     */
    GenePanelFactory withMatchCount(boolean generate);

    /**
     * Creates an object of {@link GenePanel} class, given a collection of present and absent {@link VocabularyTerm}
     * objects.
     *
     * @param presentTerms present {@link VocabularyTerm} objects
     * @param absentTerms absent {@link VocabularyTerm} objects
     * @return a new {@link GenePanel} object for the collection of present and absent {@link VocabularyTerm} objects
     */
    GenePanel build(
        @Nonnull Collection<VocabularyTerm> presentTerms,
        @Nonnull Collection<VocabularyTerm> absentTerms);

    /**
     * Creates an object of {@link GenePanel} class, given a collection of present and absent {@link VocabularyTerm}
     * objects, and a collection of {@code rejectedGenes rejected genes}.
     *
     * @param presentTerms present {@link VocabularyTerm} objects
     * @param absentTerms absent {@link VocabularyTerm} objects
     * @param rejectedGenes a collection of {@link VocabularyTerm} genes that should be excluded from panel data
     * @return a new {@link GenePanel} object for the collection of present and absent {@link VocabularyTerm} objects
     * @since 1.4
     */
    GenePanel build(
        @Nonnull Collection<VocabularyTerm> presentTerms,
        @Nonnull Collection<VocabularyTerm> absentTerms,
        @Nonnull Collection<VocabularyTerm> rejectedGenes);

    /**
     * Create an object of {@link GenePanel} class for a given {@link Patient} object. Matching genes marked as rejected
     * candidate or tested negative in the {@code patient} will be excluded from the panel.
     *
     * @param patient the {@link Patient} of interest
     * @return a new {@link GenePanel} object for the patient
     */
    GenePanel build(@Nonnull Patient patient);

    /**
     * Create an object of {@link GenePanel} class for a given {@link Patient} object. The {@link GenePanel} will not
     * include genes that were marked as rejected candidate or tested negative in the patient iff
     * {@code excludeRejectedGenes} is set to true.
     *
     * @param patient the {@link Patient} of interest
     * @param excludeRejectedGenes true iff rejected genes for patient should be excluded from generated gene panel data
     * @return a new {@link GenePanel} object for the patient
     * @since 1.4
     */
    GenePanel build(@Nonnull Patient patient, boolean excludeRejectedGenes);
}
