package de.uni_koeln.arachne.response.search;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestIndexResult {

	IndexResult indexResult; 
	
	@Before
	public void setUp() throws Exception {
		indexResult = new IndexResult();
		// add unordered
		indexResult.addValue("g Test Value");
		indexResult.addValue("0 Test Value");
		indexResult.addValue("„Test“ Value");
		indexResult.addValue("z Test Value");
		indexResult.addValue("a Test Value");
		indexResult.addValue("_ Test Value");		
	}
	
	@After
	public void tearDown() throws Exception {
		indexResult = null;
	}

	/**
	 * Tests that the values are ordered.
	 */
	@Test
	public void testGetFacetValues() {
		final String[] expected = {
				"_ Test Value",
				"0 Test Value",
				"a Test Value",
				"g Test Value",
				"z Test Value",
				"„Test“ Value"
		};
		assertArrayEquals(expected, indexResult.getFacetValues().toArray());
	}
	
	/**
	 * Tests the reduce function with the '<' marker as argument.
	 */
	@Test
	public void testReduceSmallerThan() {
		indexResult.reduce('<');
		final String[] expected = {
				"_ Test Value"
		};
		assertArrayEquals(expected, indexResult.getFacetValues().toArray());
	}

	/**
	 * Tests the reduce function with the '$' marker as argument.
	 */
	@Test
	public void testReduce$() {
		indexResult.reduce('$');
		final String[] expected = {
				"0 Test Value"
		};
		assertArrayEquals(expected, indexResult.getFacetValues().toArray());
	}
	
	/**
	 * Tests the reduce function with the 'a' marker as argument.
	 */
	@Test
	public void testReduceA() {
		indexResult.reduce('a');
		final String[] expected = {
				"a Test Value"
		};
		assertArrayEquals(expected, indexResult.getFacetValues().toArray());
	}
	
	/**
	 * Tests the reduce function with the 'g' marker as argument.
	 */
	@Test
	public void testReduceG() {
		indexResult.reduce('g');
		final String[] expected = {
				"g Test Value"
		};
		assertArrayEquals(expected, indexResult.getFacetValues().toArray());
	}
	
	/**
	 * Tests the reduce function with the 'z' marker as argument.
	 */
	@Test
	public void testReduceZ() {
		indexResult.reduce('z');
		final String[] expected = {
				"z Test Value"
		};
		assertArrayEquals(expected, indexResult.getFacetValues().toArray());
	}
	
	/**
	 * Tests the reduce function with the '>' marker as argument.
	 */
	@Test
	public void testReduceGreaterThan() {
		indexResult.reduce('>');
		final String[] expected = {
				"„Test“ Value"
		};
		assertArrayEquals(expected, indexResult.getFacetValues().toArray());
	}
}
