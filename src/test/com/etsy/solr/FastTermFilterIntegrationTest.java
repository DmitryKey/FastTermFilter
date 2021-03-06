package com.etsy.solr;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.request.SolrQueryRequest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FastTermFilterIntegrationTest extends SolrTestCaseJ4{

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    super.clearIndex();
    this.addDocuments();
  }
  
  public void addDocuments() {
    assertU(adoc("id", "1", "publisher_id", "1", "title", "all quiet on the western front", "description", "blah blah blah"));
    assertU(adoc("id", "2", "publisher_id", "2", "title", "all quiet on the western front", "description", "blah blah blah"));
    assertU(commit());
    
    assertU(adoc("id", "1", "publisher_id", "3", "title", "all quiet on the western front", "description", "blah blah blah"));
    assertU(adoc("id", "3", "publisher_id", "2", "title", "all quiet on the western front", "description", "blah blah blah"));
    assertU(adoc("id", "4", "publisher_id", "4", "title", "all quiet on the western front", "description", "blah blah blah"));
    assertU(commit());

  }
  
  private String getXPathforDocCount(int count) {
    return String.format("*[count(//doc)=%d]", count);
  }
  

  @AfterClass  
  public static void afterClassSolrTestCase() throws Exception {
   
  }

  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solr/conf/solrconfig.xml", "solr/conf/schema.xml");
  }

  @Test 
  public void findAllTest() {
    assertQ(req("*:*"), this.getXPathforDocCount(4));
  }
  
  @Test 
  public void oneToOneFilterUsingContextKey() {
    SolrQueryRequest req = req("q", "*:*", "fq", "{!fastTermFilter}idCache:ids");
    req.getContext().put("ids", new int[] {1, 2, 3});
    assertQ(req, this.getXPathforDocCount(3));
  }
  
  @Test 
  public void oneToManyFilterUsingContextKey() {
    SolrQueryRequest req = req("q", "*:*", "fq", "{!fastTermFilter}publisherIdCache:ids");
    req.getContext().put("ids", new int[] {2, 4});
    assertQ(req, this.getXPathforDocCount(3));
  }
  
  @Test 
  public void oneToOneNegativeFilterUsingContextKey() {
    SolrQueryRequest req = req("q", "*:*", "fq", "{!fastTermFilter}-idCache:ids");
    req.getContext().put("ids", new int[] {1, 2, 3});
    assertQ(req, this.getXPathforDocCount(1));
  }
  
  @Test 
  public void oneToManyNegativeFilterUsingContextKey() {
    SolrQueryRequest req = req("q", "*:*", "fq", "{!fastTermFilter}-publisherIdCache:ids");
    req.getContext().put("ids", new int[] {2, 4});
    assertQ(req, this.getXPathforDocCount(1));
  }

  @Test 
  public void oneToOneFilterUsingStringList() {
    SolrQueryRequest req = req("q", "*:*", "fq", "{!fastTermFilter}idCache:[1,2,3]");
    assertQ(req, this.getXPathforDocCount(3));
  }
  
  @Test 
  public void oneToManyFilterUsingStringList() {
    SolrQueryRequest req = req("q", "*:*", "fq", "{!fastTermFilter}publisherIdCache:[2,4]");
    assertQ(req, this.getXPathforDocCount(3));
  }

  @Test 
  public void oneToOneNegativeFilterUsingStringList() {
    SolrQueryRequest req = req("q", "*:*", "fq", "{!fastTermFilter}-idCache:[1,2,3]");
    assertQ(req, this.getXPathforDocCount(1));
  }
  
  @Test 
  public void oneToManyNegativeFilterUsingStringList() {
    SolrQueryRequest req = req("q", "*:*", "fq", "{!fastTermFilter}-publisherIdCache:[2,4]");
    assertQ(req, this.getXPathforDocCount(1));
  }


}
