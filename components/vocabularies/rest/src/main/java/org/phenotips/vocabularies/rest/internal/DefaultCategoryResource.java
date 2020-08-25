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
package org.phenotips.vocabularies.rest.internal;

import org.phenotips.Constants;
import org.phenotips.rest.Autolinker;
import org.phenotips.security.authorization.AuthorizationService;
import org.phenotips.vocabularies.rest.CategoryResource;
import org.phenotips.vocabularies.rest.DomainObjectFactory;
import org.phenotips.vocabularies.rest.VocabularyResource;
import org.phenotips.vocabularies.rest.VocabularyTermSuggestionsResource;
import org.phenotips.vocabularies.rest.model.Category;
import org.phenotips.vocabulary.Vocabulary;
import org.phenotips.vocabulary.VocabularyManager;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rest.XWikiResource;
import org.xwiki.security.authorization.Right;
import org.xwiki.stability.Unstable;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * Default implementation of {@link CategoryResource} using XWiki's support for REST resources.
 *
 * @version $Id$
 * @since 1.4
 */
@Component
@Named("org.phenotips.vocabularies.rest.internal.DefaultCategoryResource")
@Singleton
@Unstable
public class DefaultCategoryResource extends XWikiResource implements CategoryResource
{
    @Inject
    private Logger logger;

    @Inject
    private VocabularyManager vm;

    @Inject
    private DomainObjectFactory objectFactory;

    @Inject
    private Provider<Autolinker> autolinker;

    @Inject
    private UserManager users;

    @Inject
    private AuthorizationService authorizationService;

    @Inject
    @Named("default")
    private DocumentReferenceResolver<EntityReference> resolver;

    @Override
    public Category getCategory(@Nonnull final String categoryName)
    {
        if (StringUtils.isBlank(categoryName)) {
            this.logger.error("The category should not be blank.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        if (!this.vm.hasCategory(categoryName)) {
            this.logger.error("Could not find specified category: {}", categoryName);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return this.objectFactory.createLinkedCategoryRepresentation(categoryName, getCategoryLinks(),
            this::getVocabulariesForCategory);
    }

    /**
     * Returns a list of {@link org.phenotips.vocabularies.rest.model.Vocabulary} for a category with provided
     * {@code categoryId}.
     *
     * @param categoryId an identifier for a vocabulary category
     * @return a list of {@link org.phenotips.vocabularies.rest.model.Vocabulary} associated with {@code categoryId}
     */
    private List<org.phenotips.vocabularies.rest.model.Vocabulary> getVocabulariesForCategory(final String categoryId)
    {
        final Set<Vocabulary> vocabularies = this.vm.getVocabularies(categoryId);
        return this.objectFactory.createVocabulariesRepresentation(vocabularies, getVocabularyLinks(), null);
    }

    /**
     * Returns the autolinker with all resources common to all vocabularies set.
     *
     * @return an {@link Autolinker}
     */
    private Autolinker getVocabularyLinks()
    {
        return this.autolinker.get()
            .forSecondaryResource(VocabularyResource.class, this.uriInfo)
            .withActionableResources(VocabularyTermSuggestionsResource.class)
            .withGrantedRight(userIsAdmin() ? Right.ADMIN : Right.VIEW);
    }

    /**
     * Returns the autolinker with all resources common to all categories set.
     *
     * @return an {@link Autolinker}
     */
    private Autolinker getCategoryLinks()
    {
        return this.autolinker.get()
            .forResource(getClass(), this.uriInfo)
            .withGrantedRight(userIsAdmin() ? Right.ADMIN : Right.VIEW);
    }

    /**
     * Returns true if the user has admin rights.
     *
     * @return true iff the user has admin rights, false otherwise
     */
    private boolean userIsAdmin()
    {
        final User user = this.users.getCurrentUser();
        return this.authorizationService.hasAccess(user, Right.ADMIN,
            this.resolver.resolve(Constants.XWIKI_SPACE_REFERENCE));
    }
}
