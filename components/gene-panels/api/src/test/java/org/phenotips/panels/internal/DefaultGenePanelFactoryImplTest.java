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
package org.phenotips.panels.internal;

import org.phenotips.data.Patient;
import org.phenotips.panels.GenePanel;
import org.phenotips.panels.GenePanelFactory;
import org.phenotips.panels.MatchCount;
import org.phenotips.panels.TermsForGene;
import org.phenotips.vocabulary.Vocabulary;
import org.phenotips.vocabulary.VocabularyManager;
import org.phenotips.vocabulary.VocabularyTerm;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultGenePanelFactoryImpl}.
 *
 * @version $Id$
 * @since 1.3
 */
public class DefaultGenePanelFactoryImplTest
{
    private static final String HPO_LABEL = "hpo";

    private static final String HGNC_LABEL = "hgnc";

    private static final String SIZE_LABEL = "returnedrows";

    private static final String ID_LABEL = "id";

    private static final String NAME_LABEL = "label";

    private static final String TOTAL_SIZE_LABEL = "totalrows";

    private static final String GENE_ROWS_LABEL = "rows";

    private static final String MATCH_COUNT_LABEL = "matchCount";

    private static final String COUNT_LABEL = "count";

    private static final String HPO_TERM1 = "HP:001";

    private static final String HPO_TERM2 = "HP:002";

    private static final String GENE1 = "gene1";

    private static final String GENE2 = "gene2";

    private static final String ASSOCIATED_GENES = "associated_genes";

    @Rule
    public MockitoComponentMockingRule<GenePanelFactory> mocker =
        new MockitoComponentMockingRule<>(DefaultGenePanelFactoryImpl.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private GenePanelFactory genePanelFactory;

    @Mock
    private Vocabulary hpo;

    @Mock
    private Vocabulary hgnc;

    @Before
    public void setUp() throws ComponentLookupException
    {
        MockitoAnnotations.initMocks(this);
        this.genePanelFactory = this.mocker.getComponentUnderTest();
        final VocabularyManager vocabularyManager = this.mocker.getInstance(VocabularyManager.class);
        when(vocabularyManager.getVocabulary(HPO_LABEL)).thenReturn(this.hpo);
        when(vocabularyManager.getVocabulary(HGNC_LABEL)).thenReturn(this.hgnc);
    }

    // -----------------------------------Test build(Patient patient)-----------------------------------//

    @Test
    public void buildThrowsExceptionIfPatientIsNull()
    {
        final Patient patient = null;
        this.expectedException.expect(Exception.class);
        this.genePanelFactory.build(patient);
    }

    @Test
    public void buildThrowsExceptionIfPatientIsNullWithMatchCounts()
    {
        final Patient patient = null;
        this.expectedException.expect(Exception.class);
        this.genePanelFactory.withMatchCount(true).build(patient);
    }

    @Test
    public void buildWorksIfPatientEmpty()
    {
        final Patient patient = mock(Patient.class);
        Assert.assertEquals(Collections.emptySet(), patient.getFeatures());

        // Without match count.
        final GenePanel genePanel = this.genePanelFactory.build(patient);
        Assert.assertEquals(0, genePanel.size());
        Assert.assertTrue(genePanel.getPresentTerms().isEmpty());
        Assert.assertTrue(genePanel.getAbsentTerms().isEmpty());
        Assert.assertTrue(genePanel.getTermsForGeneList().isEmpty());
        Assert.assertNull(genePanel.getMatchCounts());

        final JSONObject expectedJson = new JSONObject().put(SIZE_LABEL, 0).put(TOTAL_SIZE_LABEL, 0)
            .put(GENE_ROWS_LABEL, new JSONArray());
        Assert.assertTrue(genePanel.toJSON().similar(expectedJson));
    }

    @Test
    public void buildWorksIfPatientEmptyWithMatchCounts()
    {
        final Patient patient = mock(Patient.class);
        Assert.assertEquals(Collections.emptySet(), patient.getFeatures());

        // With match count.
        final GenePanel genePanel = this.genePanelFactory.withMatchCount(true).build(patient);
        Assert.assertEquals(0, genePanel.size());
        Assert.assertTrue(genePanel.getPresentTerms().isEmpty());
        Assert.assertTrue(genePanel.getAbsentTerms().isEmpty());
        Assert.assertTrue(genePanel.getTermsForGeneList().isEmpty());
        Assert.assertTrue(genePanel.getMatchCounts().isEmpty());

        final JSONObject expectedJson = new JSONObject().put(SIZE_LABEL, 0).put(TOTAL_SIZE_LABEL, 0)
            .put(GENE_ROWS_LABEL, new JSONArray()).put(MATCH_COUNT_LABEL, new JSONArray());
        Assert.assertTrue(genePanel.toJSON().similar(expectedJson));
    }

    // ---Test build(Collection<VocabularyTerm> presentTerms, Collection<VocabularyTerm> absentTerms)---//

    @Test
    public void buildThrowsExceptionIfAnyVocabularyTermIsNull()
    {
        final List<VocabularyTerm> presentTerms = null;
        final List<VocabularyTerm> absentTerms = Collections.emptyList();

        // Without match count.
        this.expectedException.expect(Exception.class);
        this.genePanelFactory.build(presentTerms, absentTerms);
    }

    @Test
    public void buildThrowsExceptionIfAnyVocabularyTermIsNullWithMatchCounts()
    {
        final List<VocabularyTerm> presentTerms = null;
        final List<VocabularyTerm> absentTerms = Collections.emptyList();

        // With match count.
        this.expectedException.expect(Exception.class);
        this.genePanelFactory.withMatchCount(true).build(presentTerms, absentTerms);
    }

    @Test
    public void buildWorksIfVocabularyTermsAreEmpty()
    {
        final Set<VocabularyTerm> presentTerms = Collections.emptySet();
        final Set<VocabularyTerm> absentTerms = Collections.emptySet();

        // Without match count.
        final GenePanel genePanel = this.genePanelFactory.build(presentTerms, absentTerms);
        Assert.assertEquals(0, genePanel.size());
        Assert.assertTrue(genePanel.getPresentTerms().isEmpty());
        Assert.assertTrue(genePanel.getAbsentTerms().isEmpty());
        Assert.assertTrue(genePanel.getTermsForGeneList().isEmpty());
        Assert.assertNull(genePanel.getMatchCounts());

        final JSONObject expectedJson = new JSONObject().put(SIZE_LABEL, 0).put(TOTAL_SIZE_LABEL, 0)
            .put(GENE_ROWS_LABEL, new JSONArray());
        Assert.assertTrue(genePanel.toJSON().similar(expectedJson));
    }

    @Test
    public void buildWorksIfVocabularyTermsAreEmptyWithMatchCounts()
    {
        final Set<VocabularyTerm> presentTerms = Collections.emptySet();
        final Set<VocabularyTerm> absentTerms = Collections.emptySet();

        // With match count.
        final GenePanel genePanel = this.genePanelFactory.withMatchCount(true)
            .build(presentTerms, absentTerms);
        Assert.assertEquals(0, genePanel.size());
        Assert.assertTrue(genePanel.getPresentTerms().isEmpty());
        Assert.assertTrue(genePanel.getAbsentTerms().isEmpty());
        Assert.assertTrue(genePanel.getTermsForGeneList().isEmpty());
        Assert.assertTrue(genePanel.getMatchCounts().isEmpty());

        final JSONObject expectedJson = new JSONObject().put(SIZE_LABEL, 0).put(TOTAL_SIZE_LABEL, 0)
            .put(GENE_ROWS_LABEL, new JSONArray()).put(MATCH_COUNT_LABEL, new JSONArray());
        Assert.assertTrue(genePanel.toJSON().similar(expectedJson));
    }

    @Test
    public void buildWorksIfVocabularyTermsAreNotEmpty()
    {
        final VocabularyTerm presentTerm = mock(VocabularyTerm.class);
        final VocabularyTerm absentTerm = mock(VocabularyTerm.class);

        final Set<VocabularyTerm> presentTerms = new HashSet<>();
        presentTerms.add(presentTerm);
        final Set<VocabularyTerm> absentTerms = new HashSet<>();
        absentTerms.add(absentTerm);

        final List<String> associatedGenes = new ArrayList<>();
        associatedGenes.add(GENE1);
        associatedGenes.add(GENE2);

        when(presentTerm.get(ASSOCIATED_GENES)).thenReturn(associatedGenes);
        when(presentTerm.getId()).thenReturn(HPO_TERM1);
        when(absentTerm.getId()).thenReturn(HPO_TERM2);
        when(presentTerm.getTranslatedName()).thenReturn(HPO_TERM1);
        when(absentTerm.getTranslatedName()).thenReturn(HPO_TERM2);
        when(presentTerm.getName()).thenReturn(HPO_TERM1);
        when(absentTerm.getName()).thenReturn(HPO_TERM2);
        when(this.hgnc.getTerm(anyString())).thenReturn(null);

        // Without match count.
        final GenePanel genePanel = this.genePanelFactory.build(presentTerms, absentTerms);

        // The gene panel should only represent two genes.
        Assert.assertEquals(2, genePanel.size());
        // There should only be one present term and one absent term.
        Assert.assertEquals(presentTerms, genePanel.getPresentTerms());
        Assert.assertEquals(absentTerms, genePanel.getAbsentTerms());

        final List<TermsForGene> termsForGene = genePanel.getTermsForGeneList();
        // There should only be two objects, one for each gene.
        Assert.assertEquals(2, termsForGene.size());

        Assert.assertEquals(GENE1, termsForGene.get(0).getGeneId());
        Assert.assertEquals(GENE1, termsForGene.get(0).getGeneSymbol());
        // The number of terms associated with "gene1" should be 1.
        Assert.assertEquals(1, termsForGene.get(0).getCount());
        // Check the term associated with "gene1" is the one that is expected.
        Assert.assertEquals(presentTerms, termsForGene.get(0).getTerms());

        Assert.assertEquals(GENE2, termsForGene.get(1).getGeneId());
        Assert.assertEquals(GENE2, termsForGene.get(1).getGeneSymbol());
        // The number of terms associated with "gene2" should be 1.
        Assert.assertEquals(1, termsForGene.get(1).getCount());
        // Check the term associated with "gene2" is the one that is expected.
        Assert.assertEquals(presentTerms, termsForGene.get(1).getTerms());
        // The panel was build without match counts, so this value should be null.
        Assert.assertNull(genePanel.getMatchCounts());
    }

    @Test
    public void buildWorksIfVocabularyTermsAreNotEmptyWithMatchCounts()
    {
        final VocabularyTerm presentTerm = mock(VocabularyTerm.class);
        final VocabularyTerm absentTerm = mock(VocabularyTerm.class);

        final Set<VocabularyTerm> presentTerms = new HashSet<>();
        presentTerms.add(presentTerm);
        final Set<VocabularyTerm> absentTerms = new HashSet<>();
        absentTerms.add(absentTerm);

        final List<String> associatedGenes = new ArrayList<>();
        associatedGenes.add(GENE1);
        associatedGenes.add(GENE2);

        when(presentTerm.get(ASSOCIATED_GENES)).thenReturn(associatedGenes);
        when(presentTerm.getId()).thenReturn(HPO_TERM1);
        when(absentTerm.getId()).thenReturn(HPO_TERM2);
        when(presentTerm.getTranslatedName()).thenReturn(HPO_TERM1);
        when(absentTerm.getTranslatedName()).thenReturn(HPO_TERM2);
        when(presentTerm.getName()).thenReturn(HPO_TERM1);
        when(absentTerm.getName()).thenReturn(HPO_TERM2);
        when(this.hgnc.getTerm(anyString())).thenReturn(null);

        // With match count.
        final GenePanel genePanel = this.genePanelFactory.withMatchCount(true).build(presentTerms, absentTerms);

        // The gene panel should only represent two genes.
        Assert.assertEquals(2, genePanel.size());
        // There should only be one present term and one absent term.
        Assert.assertEquals(presentTerms, genePanel.getPresentTerms());
        Assert.assertEquals(absentTerms, genePanel.getAbsentTerms());

        final List<TermsForGene> termsForGene = genePanel.getTermsForGeneList();
        // There should only be two objects, one for each gene.
        Assert.assertEquals(2, termsForGene.size());

        Assert.assertEquals(GENE1, termsForGene.get(0).getGeneId());
        Assert.assertEquals(GENE1, termsForGene.get(0).getGeneSymbol());
        // The number of terms associated with "gene1" should be 1.
        Assert.assertEquals(1, termsForGene.get(0).getCount());
        // Check the term associated with "gene1" is the one that is expected.
        Assert.assertEquals(presentTerms, termsForGene.get(0).getTerms());

        Assert.assertEquals(GENE2, termsForGene.get(1).getGeneId());
        Assert.assertEquals(GENE2, termsForGene.get(1).getGeneSymbol());
        // The number of terms associated with "gene2" should be 1.
        Assert.assertEquals(1, termsForGene.get(1).getCount());
        // Check the term associated with "gene2" is the one that is expected.
        Assert.assertEquals(presentTerms, termsForGene.get(1).getTerms());
        // The panel was build with match counts, so there should be one term with the count of two (genes).
        final List<MatchCount> matchCountsList = genePanel.getMatchCounts();
        Assert.assertEquals(1, matchCountsList.size());
        final MatchCount matchCountObj = matchCountsList.get(0);
        Assert.assertEquals(HPO_TERM1, matchCountObj.getId());
        Assert.assertEquals(HPO_TERM1, matchCountObj.getName());
        Assert.assertEquals(2, matchCountObj.getCount());
        Assert.assertTrue(new JSONObject().put(ID_LABEL, HPO_TERM1).put(NAME_LABEL, HPO_TERM1).put(COUNT_LABEL, 2)
            .similar(matchCountObj.toJSON()));
    }
}
