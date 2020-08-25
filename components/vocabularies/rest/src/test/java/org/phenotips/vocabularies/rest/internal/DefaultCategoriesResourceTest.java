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

import org.phenotips.rest.Autolinker;
import org.phenotips.rest.model.Link;
import org.phenotips.security.authorization.AuthorizationService;
import org.phenotips.vocabularies.rest.CategoriesResource;
import org.phenotips.vocabularies.rest.DomainObjectFactory;
import org.phenotips.vocabularies.rest.model.Categories;
import org.phenotips.vocabularies.rest.model.Category;
import org.phenotips.vocabulary.VocabularyManager;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.inject.Provider;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.xpn.xwiki.XWikiContext;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link DefaultCategoriesResource} class.
 */
public class DefaultCategoriesResourceTest
{
    private static final String CATEGORY_A_LABEL = "A";

    private static final String CATEGORY_B_LABEL = "B";

    private static final String CATEGORY_C_LABEL = "C";

    @Rule
    public MockitoComponentMockingRule<CategoriesResource> mocker =
        new MockitoComponentMockingRule<>(DefaultCategoriesResource.class);

    @Mock
    private Provider<Autolinker> autolinkerProvider;

    @Mock
    private Category categoryA;

    @Mock
    private Category categoryB;

    @Mock
    private Category categoryC;

    @Before
    public void setUp() throws ComponentLookupException
    {
        MockitoAnnotations.initMocks(this);

        final Execution execution = mock(Execution.class);
        final ExecutionContext executionContext = mock(ExecutionContext.class);
        final ComponentManager componentManager = this.mocker.getInstance(ComponentManager.class, "context");
        when(componentManager.getInstance(Execution.class)).thenReturn(execution);
        when(execution.getContext()).thenReturn(executionContext);
        when(executionContext.getProperty("xwikicontext")).thenReturn(mock(XWikiContext.class));

        final VocabularyManager vocabularyManager = this.mocker.getInstance(VocabularyManager.class);
        final DomainObjectFactory objectFactory = this.mocker.getInstance(DomainObjectFactory.class);
        final UserManager users = this.mocker.getInstance(UserManager.class);
        final AuthorizationService authorizationService = this.mocker.getInstance(AuthorizationService.class);

        final List<String> categoriesList = Arrays.asList(CATEGORY_A_LABEL, CATEGORY_B_LABEL, CATEGORY_C_LABEL);
        when(vocabularyManager.getAvailableCategories()).thenReturn(categoriesList);

        when(objectFactory.createCategoriesRepresentation(eq(categoriesList), any(Autolinker.class),
            any(Function.class))).thenReturn(Arrays.asList(this.categoryA, this.categoryB, this.categoryC));

        final User user = mock(User.class);
        when(users.getCurrentUser()).thenReturn(user);
        when(authorizationService.hasAccess(eq(user), eq(Right.ADMIN), any(DocumentReference.class)))
            .thenReturn(true);

        final Autolinker autolinker = this.mocker.getInstance(Autolinker.class);
        when(autolinker.forSecondaryResource(any(Class.class), any(UriInfo.class))).thenReturn(autolinker);
        when(autolinker.forResource(any(Class.class), any(UriInfo.class))).thenReturn(autolinker);
        when(autolinker.withActionableResources(any(Class.class))).thenReturn(autolinker);
        when(autolinker.withExtraParameters(anyString(), anyString())).thenReturn(autolinker);
        when(autolinker.withGrantedRight(any(Right.class))).thenReturn(autolinker);
        when(autolinker.build()).thenReturn(Collections.singletonList(mock(Link.class)));
    }

    @Test
    public void getAllCategories() throws Exception
    {
        final Categories categories = this.mocker.getComponentUnderTest().getAllCategories();
        final Collection<Category> categoryCollection = categories.getCategories();
        Assert.assertEquals(3, categoryCollection.size());
        Assert.assertEquals(Arrays.asList(this.categoryA, this.categoryB, this.categoryC), categoryCollection);
    }
}
