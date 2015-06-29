package de.uni_koeln.arachne.controller;

import static de.uni_koeln.arachne.util.network.CustomMediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// needed to use .andDo(print()) for debugging
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

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
import de.uni_koeln.arachne.service.SearchService;
import de.uni_koeln.arachne.testconfig.TestData;
import de.uni_koeln.arachne.util.search.SearchParameters;

@RunWith(MockitoJUnitRunner.class)
public class TestSearchController {
	
	@Mock
	private SearchService searchService;
	
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
		
		when(searchService.buildDefaultSearchRequest(any(SearchParameters.class), any(Multimap.class)))
		.then(new Answer<SearchRequestBuilder>() {
			@Override
			public SearchRequestBuilder answer(InvocationOnMock invocation)	throws Throwable {
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
							newFacets.add(facet);
						}
					}
					searchResult.setFacets(newFacets);
				}
								
				return null;
			}
		});
		
		when(searchService.executeSearchRequest(eq((SearchRequestBuilder)null), anyInt(), anyInt(), any(Multimap.class)))
		.thenAnswer(new Answer<SearchResult>() {
			@Override
			public SearchResult answer(InvocationOnMock invocation) throws Throwable {
				final Object[] args = invocation.getArguments();
				final Multiset<String> keyMultiset = ((Multimap<String, String>)args[3]).keys();
				
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
				return searchResult;
			}
		});
		
		when(searchService.getFilters(anyList(), anyInt())).thenCallRealMethod();
	}

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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"},"
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
				.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void testSearchLimitInvalidString() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("limit", "high")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
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
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":1,"
						+ "\"entityId\":1,"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
				.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void testSearchOffsetInvalidString() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("offset", "high")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12},"
						+ "{\"value\":\"test3_value3\",\"count\":11}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
				.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void testSearchDescInvalidFloat() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("desc", "2.1")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void testSearchDescInvalidString() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("desc", "maybe")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
				.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void testSearchBboxInvalidTooLong() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("bbox", "0,0,1,1,2")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void testSearchBboxInvalidString() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("bbox", "bbox")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
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
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
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
				.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void testSearchGhprecInvalidString() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("ghprec", "high")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void testSearchSortFacet() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("sortfacet", "facet_test")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(content().json("{"
						+ "\"size\":2,"
						+ "\"limit\":1,"
						+ "\"offset\":0,"
						+ "\"facets\":["
						+ "{\"name\":\"facet_test1\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test1_value1\",\"count\":1},"
						+ "{\"value\":\"test1_value2\",\"count\":2}]},"
						+ "{\"name\":\"facet_test2\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test2_value1\",\"count\":4}]},"
						+ "{\"name\":\"facet_test3\",\"label\":null,\"values\":"
						+ "[{\"value\":\"test3_value1\",\"count\":13},"
						+ "{\"value\":\"test3_value2\",\"count\":12}]}],"
						+ "\"entities\":["
						+ "{\"thumbnailId\":0,"
						+ "\"entityId\":0,"
						+ "\"title\":\"Test title\","
						+ "\"subtitle\":\"Test subtitle\","
						+ "\"type\":\"test\"}],"
						+ "\"status\":\"OK\"}"));
	}
	
	// TODO implement when facet sorting is implemented
	/*@Test
	public void testSearchSortFacetInvalidName() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("sortfacet", "the_facet")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void testSearchSortFacetInvalidInt() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("sortfacet", "2")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void testSearchSortFacetInvalidFloat() throws Exception {
		mockMvc.perform(
				get("/search").param("q", "*").param("sortfacet", "2.0")
				.contentType(APPLICATION_JSON_UTF8))
				.andExpect(status().is4xxClientError());
	}*/
	
	// TODO add tests fo other endpoints
}
