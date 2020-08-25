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
package org.phenotips.panels.rest.internal;

import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * A data class that contains term identifier data, such as present and absent terms, as well as rejected genes, if any.
 * This class is used as key for the {@link GenePanelLoader}.
 *
 * @version $Id$
 * @since 1.4
 */
class PanelData
{
    /** A set of present term identifiers. */
    private final Set<String> presentTerms;

    /** A set of absent term identifiers. */
    private final Set<String> absentTerms;

    /** A set of rejected gene identifiers. */
    private final Set<String> rejectedGenes;

    /** A marker specifying if the number of genes for each present term should be counted. */
    private final boolean withMatchCount;

    /**
     * The default constructor for the class. Parameters are non-null sets of {@code presentTerms present terms},
     * {@code absentTerms absent terms}, and {@code rejectedGenes rejected genes}.
     *
     * @param presentTerms present term identifiers
     * @param absentTerms absent term identifiers
     * @param rejectedGenes rejected gene identifiers
     */
    PanelData(
        @Nonnull final Set<String> presentTerms,
        @Nonnull final Set<String> absentTerms,
        @Nonnull final Set<String> rejectedGenes)
    {
        this(presentTerms, absentTerms, rejectedGenes, false);
    }

    /**
     * The default constructor for the class. Parameters are non-null sets of {@code presentTerms present terms},
     * {@code absentTerms absent terms}, and {@code rejectedGenes rejected genes}.
     *
     * @param presentTerms present term identifiers
     * @param absentTerms absent term identifiers
     * @param rejectedGenes rejected gene identifiers
     * @param withMatchCount set to true iff the number of genes available for term should be counted
     */
    PanelData(
        @Nonnull final Set<String> presentTerms,
        @Nonnull final Set<String> absentTerms,
        @Nonnull final Set<String> rejectedGenes,
        final boolean withMatchCount)
    {
        this.presentTerms = presentTerms;
        this.absentTerms = absentTerms;
        this.rejectedGenes = rejectedGenes;
        this.withMatchCount = withMatchCount;
    }

    /**
     * Gets the present terms for the panel.
     *
     * @return a set of present terms for the panel
     */
    Set<String> getPresentTerms()
    {
        return this.presentTerms;
    }

    /**
     * Gets the absent terms for the panel.
     *
     * @return a set of absent terms for the panel
     */
    Set<String> getAbsentTerms()
    {
        return this.absentTerms;
    }

    /**
     * Gets the specified rejected genes for the panel.
     *
     * @return a set of genes that should be excluded from the gene panel
     */
    Set<String> getRejectedGenes()
    {
        return this.rejectedGenes;
    }

    /**
     * Gets the withMatchCount value for the panel.
     *
     * @return true iff the number of genes for each present term should be counted
     */
    boolean isWithMatchCount()
    {
        return this.withMatchCount;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PanelData panelData = (PanelData) o;

        return new EqualsBuilder()
            .append(this.withMatchCount, panelData.withMatchCount)
            .append(this.presentTerms, panelData.presentTerms)
            .append(this.absentTerms, panelData.absentTerms)
            .append(this.rejectedGenes, panelData.rejectedGenes)
            .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(this.presentTerms)
            .append(this.absentTerms)
            .append(this.rejectedGenes)
            .append(this.withMatchCount)
            .toHashCode();
    }
}
