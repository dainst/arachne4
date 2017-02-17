package de.uni_koeln.arachne.controller;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// needed to use .andDo(print()) for debugging
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

import de.uni_koeln.arachne.response.search.SearchHit;
import de.uni_koeln.arachne.response.search.SearchResult;
import de.uni_koeln.arachne.response.search.SearchResultFacet;
import de.uni_koeln.arachne.response.search.SearchResultFacetValue;
import de.uni_koeln.arachne.response.search.SuggestResult;
import de.uni_koeln.arachne.service.SearchService;
import de.uni_koeln.arachne.service.UserRightsService;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.search.SearchParameters;

@RunWith(MockitoJUnitRunner.class)
public class TestSearchController {
	
	@Mock
	private SearchService searchService;
	
	@Mock
	private UserRightsService userRightsService;
	
	@InjectMocks
	private SearchController controller = new SearchController(1, 2);
	
	private MockMvc mockMvc;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		
		final TestData testData = new TestData();
		final SearchResult searchResult = testData.getDefaultSearchResult();
		// this is a neat trick to get a final, mutable boolean value which I can reference in the overriden answer
		// methods
		final AtomicBoolean isIndexSearch = new AtomicBoolean(false);
				
		when(searchService.buildDefaultSearchRequest(any(SearchParameters.class), any(Multimap.class)))
		.then(new Answer<SearchRequestBuilder>() {
			@Override
			public SearchRequestBuilder answer(InvocationOnMock invocation)	throws Throwable {
				isIndexSearch.set(false);
				final Object[] args = invocation.getArguments();
				final SearchParameters searchParams = (SearchParameters)args[0];
				
				List<SearchHit> entites = searchResult.getEntities();
				int resultSize = entites.size();
				int limit = searchParams.getLimit();
				int offset = searchParams.getOffset();
				
				if (resultSize > offset) {
					final int toIndex = (limit + offset) > resultSize ? resultSize : (limit + offset);
					searchResult.setEntities(entites.subList(offset, toIndex));
					entites = searchResult.getEntities();
				}
				searchResult.setOffset(searchParams.getOffset());
				
				searchResult.setLimit(limit);
				if (resultSize > limit) {
					searchResult.setEntities(entites.subList(0, limit));
				}
				
				final int facetLimit = searchParams.getFacetLimit();
				if (facetLimit > 0) {
					final List<SearchResultFacet> newFacets = new ArrayList<SearchResultFacet>();
					for (SearchResultFacet facet : searchResult.getFacets()) {
						List<SearchResultFacetValue> facetValues = facet.getValues();
						if (facetValues.size() > facetLimit) {
							facetValues = facetValues.subList(0, facetLimit);
							final SearchResultFacet newFacet = new SearchResultFacet(facet.getName());
							for (SearchResultFacetValue facetValue : facetValues) {
								newFacet.addValue(facetValue);
							}
							newFacets.add(newFacet);
						} else {
							// for testing sf parameter on facet_test1
							if (searchParams.getFacetsToSort().contains(facet.getName())) {
								Collections.sort(facetValues, new FacetValueComparator());
							}
							newFacets.add(facet);
						}
					}
					searchResult.setFacets(newFacets);
				}
								
				return null;
			}
		});
		
		when(searchService.buildContextSearchRequest(anyLong(), any(SearchParameters.class), any(Multimap.class)))
		.then(new Answer<SearchRequestBuilder>() {
			@Override
			public SearchRequestBuilder answer(InvocationOnMock invocation)	throws Throwable {
				isIndexSearch.set(false);
				final Object[] args = invocation.getArguments();
				final long entityId = (long)args[0];
				if (entityId != 1) {
					searchResult.setSize(0);
					searchResult.setLimit(1);
					searchResult.setOffset(0);
					searchResult.setEntities(null);
					searchResult.setFacets(null);
				} else {
					final SearchParameters searchParams = (SearchParameters)args[1];

					List<SearchHit> entities = searchResult.getEntities();
					int resultSize = entities.size();
					int limit = searchParams.getLimit();
					int offset = searchParams.getOffset();

					if (resultSize > offset) {
						final int toIndex = (limit + offset) > resultSize ? resultSize : (limit + offset);
						searchResult.setEntities(entities.subList(offset, toIndex));
						entities = searchResult.getEntities();
					}
					searchResult.setOffset(searchParams.getOffset());

					searchResult.setLimit(limit);
					if (resultSize > limit) {
						searchResult.setEntities(entities.subList(0, limit));
					}

					final int facetLimit = searchParams.getFacetLimit();
					if (facetLimit > 0) {
						final List<SearchResultFacet> newFacets = new ArrayList<SearchResultFacet>();
						for (SearchResultFacet facet : searchResult.getFacets()) {
							List<SearchResultFacetValue> facetValues = facet.getValues();
							if (facetValues.size() > facetLimit) {
								facetValues = facetValues.subList(0, facetLimit);
								final SearchResultFacet newFacet = new SearchResultFacet(facet.getName());
								for (SearchResultFacetValue facetValue : facetValues) {
									newFacet.addValue(facetValue);
								}
								newFacets.add(newFacet);
							} else {
								newFacets.add(facet);
							}
						}
						searchResult.setFacets(newFacets);
					}
				}		
				return null;
			}
		});
		
		when(searchService.buildIndexSearchRequest(anyString(), any(Multimap.class)))
		.then(new Answer<SearchResult>() {
			@Override
			public SearchResult answer(InvocationOnMock invocation) throws Throwable {
				final String facetName = invocation.getArgumentAt(0, String.class);
				ListIterator<SearchResultFacet> iterator = searchResult.getFacets().listIterator();
				while (iterator.hasNext()) {
					SearchResultFacet facet = (SearchResultFacet) iterator.next();
					if (!facetName.equals(facet.getName())) {
						iterator.remove();
					}
				}
				return null;
			}
		});
		
		when(searchService.executeSearchRequest(eq((SearchRequestBuilder)null), anyInt(), anyInt(), any(Multimap.class)
				, anyInt()))
			.then(new Answer<SearchResult>() {
			@Override
			public SearchResult answer(InvocationOnMock invocation) throws Throwable {
				final Object[] args = invocation.getArguments();

				if (args[3] != null) {
					final Multiset<String> keyMultiset  = ((Multimap<String, String>)args[3]).keys();

					if (!keyMultiset.isEmpty()) {
						String key = (String)keyMultiset.toArray()[0];
						List<SearchResultFacet> newFacets = new ArrayList<SearchResultFacet>();
						for (SearchResultFacet facet : searchResult.getFacets()) {
							if (!key.equals(facet.getName())) {
								newFacets.add(facet);
							}
						}
						searchResult.setFacets(newFacets);
					}
				}
				return searchResult;
			}
		});
		
		when(searchService.executeSuggestRequest(anyString())).thenReturn(new SuggestResult());
		final SuggestResult suggestResult = new SuggestResult();
		for (int i=0; i<10; i++) {
			suggestResult.addSuggestion("ordered result " + i);
		}
		when(searchService.executeSuggestRequest("or")).thenReturn(suggestResult);
		
		when(searchService.getFilters(anyList(), anyInt())).thenCallRealMethod();
		
		when(userRightsService.userHasAtLeastGroupID(anyInt())).thenReturn(false);
	}

	// TODO: add test(s) for editorfields for user with editor rights 
	
	@Test
	public void testSearchNoParameters() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}

	@Test
	public void testSearchLimit() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("limit", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchLimit0() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("limit", "0")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchLimit2() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("limit", "2")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":2,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"},"
						+ "{\"thumbnailId\":2,"
						+ "\"entityId\":2,"
						+ "\"@id\":\"testServer.com/entity/2\","
						+ "\"title\":\"Test title 1\","
						+ "\"subtitle\":\"Test subtitle 1\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchLimitNegativeInt() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("limit", "-1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchLimitInvalidFloat() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("limit", "1.7")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchLimitInvalidString() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("limit", "high")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchOffset() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("offset", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"offset\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":2,"
						+ "\"entityId\":2,"
						+ "\"@id\":\"testServer.com/entity/2\","
						+ "\"title\":\"Test title 1\","
						+ "\"subtitle\":\"Test subtitle 1\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchOffsetNegativeInt() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("offset", "-1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchOffsetInvalidFloat() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("offset", "1.7")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchOffsetInvalidString() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("offset", "high")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchFq() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("fq", "facet_test2:\"test2_value1\"")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchFqUnknownFacet() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("fq", "facet_unknown:\"some value\"")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchFqInvalidFacet() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("fq", "f,a:c,e+t")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchFl() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "debug").param("fl", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));		
	}
	
	@Test
	public void testSearchFlLimit0() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "debug").param("fl", "0")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12},"
						+ "{\"value\":\"test3_value3\",\"count\":11}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchSort() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("sort", "title")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchDesc() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("desc", "true")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchDescInvalidInt() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("desc", "2")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchDescInvalidFloat() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("desc", "2.1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchDescInvalidString() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("desc", "maybe")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchBbox() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("bbox", "0,0,1,1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchBboxInvalidTooShort() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("bbox", "0,0,1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchBboxInvalidTooLong() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("bbox", "0,0,1,1,2")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchBboxInvalidString() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("bbox", "bbox")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchGhprec() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("ghprec", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testSearchGhprecInvalidFloat() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("ghprec", "5.5")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchGhprecInvalidString() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("ghprec", "high")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSearchSortFacet() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("sf", "facet_test1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1a\",\"count\":2},"
						+ "{\"value\":\"test1_value1b\",\"count\":1}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"))
				// explicitly test the order as the above does not
				.andExpect(jsonPath("$.facets[0].values[0].value").value("test1_value1a"))
				.andExpect(jsonPath("$.facets[0].values[1].value").value("test1_value1b"));
	}
	
	@Test
	public void testSearchSortFacetInvalidUnknownFacet() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("sf", "unknown")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"))
				// explicitly test the order as the above does not
				.andExpect(jsonPath("$.facets[0].values[0].value").value("test1_value1b"))
				.andExpect(jsonPath("$.facets[0].values[1].value").value("test1_value1a"));
	}
	
	@Test
	public void testContextSearchNoParameters() throws Exception {
		mockMvc.perform(
				get("/contexts/1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}

	@Test
	public void testContextSearchNoParameterInvalidId() throws Exception {
		mockMvc.perform(
				get("/contexts/2")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":0,"
						+ "\"limit\":1,"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchLimit() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("limit", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchLimit0() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("limit", "0")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchLimit2() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("limit", "2")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":2,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"},"
						+ "{\"thumbnailId\":2,"
						+ "\"entityId\":2,"
						+ "\"@id\":\"testServer.com/entity/2\","
						+ "\"title\":\"Test title 1\","
						+ "\"subtitle\":\"Test subtitle 1\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchLimitNegativeInt() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("limit", "-1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchLimitInvalidFloat() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("limit", "1.7")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testContextSearchLimitInvalidString() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("limit", "high")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testContextSearchOffset() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("offset", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"offset\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":2,"
						+ "\"entityId\":2,"
						+ "\"@id\":\"testServer.com/entity/2\","
						+ "\"title\":\"Test title 1\","
						+ "\"subtitle\":\"Test subtitle 1\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchOffsetNegativeInt() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("offset", "-1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchOffsetInvalidFloat() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("offset", "1.7")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testContextSearchOffsetInvalidString() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("offset", "high")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testContextSearchFq() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("fq", "facet_test2:\"test2_value1\"")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchFqUnknownFacet() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("fq", "facet_unknown:\"some value\"")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchFqInvalidFacet() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("fq", "f,a:c,e+t")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchFl() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("fl", "1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));		
	}
	
	@Test
	public void testContextSearchFlLimit0() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("fl", "0")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12},"
						+ "{\"value\":\"test3_value3\",\"count\":11}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchSort() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("sort", "title")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchDesc() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("desc", "true")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1b\",\"count\":1},"
						+ "{\"value\":\"test1_value1a\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
						+ "\"@id\":\"testServer.com/entity/1\","
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	@Test
	public void testContextSearchDescInvalidInt() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("desc", "2")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testContextSearchDescInvalidFloat() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("desc", "2.1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testContextSearchDescInvalidString() throws Exception {
		mockMvc.perform(
				get("/contexts/1").param("desc", "maybe")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testIndexSearchNoParameter() throws Exception {
		mockMvc.perform(
				get("/index/facet_test1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"facetValues\":[\"test1_value1a\",\"test1_value1b\"]}"));
	}
	
	@Test
	public void testIndexSearchNoParameterUnknownFacet() throws Exception {
		mockMvc.perform(
				get("/contexts/facet_unknown")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testIndexSearchNoParameterInvalidFacet() throws Exception {
		mockMvc.perform(
				get("/index/f,a:c,e+t")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSuggest() throws Exception {
		mockMvc.perform(
				get("/suggest").param("q", "or")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{\"suggestions\":"
						+ "[\"ordered result 0\""
						+ ",\"ordered result 1\""
						+ ",\"ordered result 2\""
						+ ",\"ordered result 3\""
						+ ",\"ordered result 4\""
						+ ",\"ordered result 5\""
						+ ",\"ordered result 6\""
						+ ",\"ordered result 7\""
						+ ",\"ordered result 8\""
						+ ",\"ordered result 9\"]}"));
	}
	
	@Test
	public void testSuggestNoResult() throws Exception {
		mockMvc.perform(
				get("/suggest").param("q", "ar")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{}"));
	}
	
	@Test
	public void testSuggestNoParam() throws Exception {
		mockMvc.perform(
				get("/suggest")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isBadRequest());
	}
}
