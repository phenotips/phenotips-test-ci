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
import org.phenotips.security.authorization.AuthorizationService;
import org.phenotips.vocabularies.rest.CategoryResource;
import org.phenotips.vocabularies.rest.DomainObjectFactory;
import org.phenotips.vocabularies.rest.model.Category;
import org.phenotips.vocabulary.Vocabulary;
import org.phenotips.vocabulary.VocabularyManager;

import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link DefaultCategoryResource} class.
 */
public class DefaultCategoryResourceTest
{
    private static final String CATEGORY_A = "A";

    @Rule
    public MockitoComponentMockingRule<CategoryResource> mocker =
        new MockitoComponentMockingRule<>(DefaultCategoryResource.class);

    private Logger logger;

    private VocabularyManager vm;

    private CategoryResource component;

    @Mock
    private Vocabulary vocabA1;

    @Mock
    private Vocabulary vocabA2;

    @Mock
    private Category categoryA;

    private Autolinker autolinker;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private User user;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        final Execution execution = mock(Execution.class);
        final ExecutionContext executionContext = mock(ExecutionContext.class);
        final ComponentManager componentManager = this.mocker.getInstance(ComponentManager.class, "context");
        when(componentManager.getInstance(Execution.class)).thenReturn(execution);
        when(execution.getContext()).thenReturn(executionContext);
        when(executionContext.getProperty("xwikicontext")).thenReturn(mock(XWikiContext.class));

        this.component = this.mocker.getComponentUnderTest();

        this.vm = this.mocker.getInstance(VocabularyManager.class);
        final Set<Vocabulary> vocabs = new HashSet<>();
        vocabs.add(this.vocabA1);
        vocabs.add(this.vocabA2);
        when(this.vm.getVocabularies(CATEGORY_A)).thenReturn(vocabs);

        ReflectionUtils.setFieldValue(this.component, "uriInfo", this.uriInfo);

        final DomainObjectFactory objectFactory = this.mocker.getInstance(DomainObjectFactory.class);
        when(objectFactory.createLinkedCategoryRepresentation(eq(CATEGORY_A), any(Autolinker.class),
            any(Function.class))).thenReturn(this.categoryA);

        this.logger = this.mocker.getMockedLogger();
        final UserManager users = this.mocker.getInstance(UserManager.class);
        final AuthorizationService authorizationService = this.mocker.getInstance(AuthorizationService.class);
        when(users.getCurrentUser()).thenReturn(this.user);
        when(authorizationService.hasAccess(eq(this.user), eq(Right.ADMIN), any(EntityReference.class)))
            .thenReturn(true);

        this.autolinker = this.mocker.getInstance(Autolinker.class);
        when(this.autolinker.forResource(any(Class.class), eq(this.uriInfo))).thenReturn(this.autolinker);
        when(this.autolinker.withGrantedRight(any(Right.class))).thenReturn(this.autolinker);
        when(this.autolinker.build()).thenReturn(Collections.emptyList());
    }

    @Test(expected = WebApplicationException.class)
    public void getCategoryThrowsExceptionWhenCategoryNameIsBlank()
    {
        this.component.getCategory(" ");
        verify(this.logger).error("The category should not be blank.");
        Assert.fail("An exception should have been thrown for blank category.");
    }

    @Test(expected = WebApplicationException.class)
    public void getCategoryThrowsExceptionWhenCategoryIsNotValid()
    {
        when(this.vm.hasCategory(CATEGORY_A)).thenReturn(false);
        this.component.getCategory(CATEGORY_A);
        verify(this.logger).error("Could not find specified category: {}", CATEGORY_A);
        Assert.fail("An exception should have been thrown for invalid category.");
    }

    @Test
    public void getCategoryBehavesAsExpectedWithValidData()
    {
        when(this.vm.hasCategory(CATEGORY_A)).thenReturn(true);
        final Category category = this.component.getCategory(CATEGORY_A);
        Assert.assertEquals(this.categoryA, category);
    }
}
