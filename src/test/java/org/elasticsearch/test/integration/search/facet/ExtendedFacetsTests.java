begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.test.integration.search.facet
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|search
operator|.
name|facet
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|RandomStringGenerator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|Tuple
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|regex
operator|.
name|Regex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|text
operator|.
name|StringText
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|text
operator|.
name|Text
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|FacetBuilders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|terms
operator|.
name|TermsFacet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|terms
operator|.
name|TermsFacetBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|AbstractNodesTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ExtendedFacetsTests
specifier|public
class|class
name|ExtendedFacetsTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|client
specifier|private
name|Client
name|client
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|createNodes
specifier|public
name|void
name|createNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
name|numberOfShards
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numberOfNodes
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|startNode
argument_list|(
literal|"node"
operator|+
name|i
argument_list|,
name|settings
argument_list|)
expr_stmt|;
block|}
name|client
operator|=
name|getClient
argument_list|()
expr_stmt|;
block|}
DECL|method|numberOfShards
specifier|protected
name|int
name|numberOfShards
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
DECL|method|numberOfNodes
specifier|protected
name|int
name|numberOfNodes
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
DECL|method|numDocs
specifier|protected
name|int
name|numDocs
parameter_list|()
block|{
return|return
literal|2500
return|;
block|}
annotation|@
name|AfterClass
DECL|method|closeNodes
specifier|public
name|void
name|closeNodes
parameter_list|()
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
DECL|method|getClient
specifier|protected
name|Client
name|getClient
parameter_list|()
block|{
return|return
name|client
argument_list|(
literal|"node0"
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|testTermFacet_stringFields
specifier|public
name|void
name|testTermFacet_stringFields
parameter_list|()
throws|throws
name|Throwable
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field1_concrete"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fielddata"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"concrete_bytes"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"field1_paged"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fielddata"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"paged_bytes"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"field1_fst"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fielddata"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"fst"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"field2"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"fielddata"
argument_list|)
operator|.
name|field
argument_list|(
literal|"format"
argument_list|,
literal|"fst"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"q_field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|long
name|seed
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|// LuceneTestCase...
try|try
block|{
name|Random
name|random
init|=
operator|new
name|Random
argument_list|(
name|seed
argument_list|)
decl_stmt|;
name|int
name|numOfQueryValues
init|=
literal|50
decl_stmt|;
name|String
index|[]
name|queryValues
init|=
operator|new
name|String
index|[
name|numOfQueryValues
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numOfQueryValues
condition|;
name|i
operator|++
control|)
block|{
name|queryValues
index|[
name|i
index|]
operator|=
name|RandomStringGenerator
operator|.
name|random
argument_list|(
literal|5
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
name|random
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|uniqueValuesSet
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|int
name|numOfVals
init|=
literal|400
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numOfVals
condition|;
name|i
operator|++
control|)
block|{
name|uniqueValuesSet
operator|.
name|add
argument_list|(
name|RandomStringGenerator
operator|.
name|random
argument_list|(
literal|10
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|null
argument_list|,
name|random
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
index|[]
name|allUniqueFieldValues
init|=
name|uniqueValuesSet
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|uniqueValuesSet
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|allField1Values
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|allField1AndField2Values
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|queryValToField1FacetEntries
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|queryValToField1and2FacetEntries
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|numDocs
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|numField1Values
init|=
name|random
operator|.
name|nextInt
argument_list|(
literal|17
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|field1Values
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|(
name|numField1Values
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<=
name|numField1Values
condition|;
name|j
operator|++
control|)
block|{
name|boolean
name|added
init|=
literal|false
decl_stmt|;
while|while
condition|(
operator|!
name|added
condition|)
block|{
name|added
operator|=
name|field1Values
operator|.
name|add
argument_list|(
name|allUniqueFieldValues
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|numOfVals
argument_list|)
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|allField1Values
operator|.
name|addAll
argument_list|(
name|field1Values
argument_list|)
expr_stmt|;
name|allField1AndField2Values
operator|.
name|addAll
argument_list|(
name|field1Values
argument_list|)
expr_stmt|;
name|String
name|field2Val
init|=
name|allUniqueFieldValues
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|numOfVals
argument_list|)
index|]
decl_stmt|;
name|allField1AndField2Values
operator|.
name|add
argument_list|(
name|field2Val
argument_list|)
expr_stmt|;
name|String
name|queryVal
init|=
name|queryValues
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|numOfQueryValues
argument_list|)
index|]
decl_stmt|;
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field1_concrete"
argument_list|,
name|field1Values
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1_paged"
argument_list|,
name|field1Values
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1_fst"
argument_list|,
name|field1Values
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
name|field2Val
argument_list|)
operator|.
name|field
argument_list|(
literal|"q_field"
argument_list|,
name|queryVal
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|2000
argument_list|)
operator|==
literal|854
condition|)
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|addControlValues
argument_list|(
name|queryValToField1FacetEntries
argument_list|,
name|field1Values
argument_list|,
name|queryVal
argument_list|)
expr_stmt|;
name|addControlValues
argument_list|(
name|queryValToField1and2FacetEntries
argument_list|,
name|field1Values
argument_list|,
name|queryVal
argument_list|)
expr_stmt|;
name|addControlValues
argument_list|(
name|queryValToField1and2FacetEntries
argument_list|,
name|field2Val
argument_list|,
name|queryVal
argument_list|)
expr_stmt|;
block|}
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|String
index|[]
name|facetFields
init|=
operator|new
name|String
index|[]
block|{
literal|"field1_concrete"
block|,
literal|"field1_paged"
block|,
literal|"field1_fst"
block|}
decl_stmt|;
name|TermsFacet
operator|.
name|ComparatorType
index|[]
name|compTypes
init|=
name|TermsFacet
operator|.
name|ComparatorType
operator|.
name|values
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|facetField
range|:
name|facetFields
control|)
block|{
for|for
control|(
name|String
name|queryVal
range|:
name|queryValToField1FacetEntries
operator|.
name|keySet
argument_list|()
control|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|allFieldValues
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|queryControlFacets
decl_stmt|;
name|TermsFacet
operator|.
name|ComparatorType
name|compType
init|=
name|compTypes
index|[
name|random
operator|.
name|nextInt
argument_list|(
name|compTypes
operator|.
name|length
argument_list|)
index|]
decl_stmt|;
name|TermsFacetBuilder
name|termsFacetBuilder
init|=
name|FacetBuilders
operator|.
name|termsFacet
argument_list|(
literal|"facet1"
argument_list|)
operator|.
name|order
argument_list|(
name|compType
argument_list|)
decl_stmt|;
name|boolean
name|useFields
decl_stmt|;
if|if
condition|(
name|random
operator|.
name|nextInt
argument_list|(
literal|4
argument_list|)
operator|==
literal|3
condition|)
block|{
name|useFields
operator|=
literal|true
expr_stmt|;
name|queryControlFacets
operator|=
name|queryValToField1and2FacetEntries
operator|.
name|get
argument_list|(
name|queryVal
argument_list|)
expr_stmt|;
name|allFieldValues
operator|=
name|allField1AndField2Values
expr_stmt|;
name|termsFacetBuilder
operator|.
name|fields
argument_list|(
name|facetField
argument_list|,
literal|"field2"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|queryControlFacets
operator|=
name|queryValToField1FacetEntries
operator|.
name|get
argument_list|(
name|queryVal
argument_list|)
expr_stmt|;
name|allFieldValues
operator|=
name|allField1Values
expr_stmt|;
name|useFields
operator|=
literal|false
expr_stmt|;
name|termsFacetBuilder
operator|.
name|field
argument_list|(
name|facetField
argument_list|)
expr_stmt|;
block|}
name|int
name|size
decl_stmt|;
if|if
condition|(
name|numberOfShards
argument_list|()
operator|==
literal|1
operator|||
name|compType
operator|==
name|TermsFacet
operator|.
name|ComparatorType
operator|.
name|TERM
operator|||
name|compType
operator|==
name|TermsFacet
operator|.
name|ComparatorType
operator|.
name|REVERSE_TERM
condition|)
block|{
name|size
operator|=
name|random
operator|.
name|nextInt
argument_list|(
name|queryControlFacets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|size
operator|=
name|allFieldValues
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|termsFacetBuilder
operator|.
name|size
argument_list|(
name|size
argument_list|)
expr_stmt|;
if|if
condition|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|termsFacetBuilder
operator|.
name|executionHint
argument_list|(
literal|"map"
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|String
argument_list|>
name|excludes
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|int
name|numExcludes
init|=
name|random
operator|.
name|nextInt
argument_list|(
literal|5
argument_list|)
operator|+
literal|1
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|facetValues
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|queryControlFacets
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numExcludes
condition|;
name|i
operator|++
control|)
block|{
name|excludes
operator|.
name|add
argument_list|(
name|facetValues
operator|.
name|get
argument_list|(
name|random
operator|.
name|nextInt
argument_list|(
name|facetValues
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|termsFacetBuilder
operator|.
name|exclude
argument_list|(
name|excludes
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|regex
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|random
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|facetValues
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|queryControlFacets
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|regex
operator|=
name|facetValues
operator|.
name|get
argument_list|(
name|random
operator|.
name|nextInt
argument_list|(
name|facetValues
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|regex
operator|=
literal|"^"
operator|+
name|regex
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|regex
operator|.
name|length
argument_list|()
operator|/
literal|2
argument_list|)
operator|+
literal|".*"
expr_stmt|;
name|termsFacetBuilder
operator|.
name|regex
argument_list|(
name|regex
argument_list|)
expr_stmt|;
block|}
name|boolean
name|allTerms
init|=
name|random
operator|.
name|nextInt
argument_list|(
literal|10
argument_list|)
operator|==
literal|3
decl_stmt|;
name|termsFacetBuilder
operator|.
name|allTerms
argument_list|(
name|allTerms
argument_list|)
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"q_field"
argument_list|,
name|queryVal
argument_list|)
argument_list|)
operator|.
name|addFacet
argument_list|(
name|termsFacetBuilder
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|TermsFacet
name|actualFacetEntries
init|=
name|response
operator|.
name|getFacets
argument_list|()
operator|.
name|facet
argument_list|(
literal|"facet1"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|expectedFacetEntries
init|=
name|getExpectedFacetEntries
argument_list|(
name|allFieldValues
argument_list|,
name|queryControlFacets
argument_list|,
name|size
argument_list|,
name|compType
argument_list|,
name|excludes
argument_list|,
name|regex
argument_list|,
name|allTerms
argument_list|)
decl_stmt|;
name|String
name|reason
init|=
name|String
operator|.
name|format
argument_list|(
literal|"query: [%s] field: [%s] size: [%d] order: [%s] all_terms: [%s] fields: [%s] regex: [%s] excludes: [%s]"
argument_list|,
name|queryVal
argument_list|,
name|facetField
argument_list|,
name|size
argument_list|,
name|compType
argument_list|,
name|allTerms
argument_list|,
name|useFields
argument_list|,
name|regex
argument_list|,
name|excludes
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|reason
argument_list|,
name|actualFacetEntries
operator|.
name|getEntries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedFacetEntries
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|expectedFacetEntries
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|reason
argument_list|,
name|actualFacetEntries
operator|.
name|getEntries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedFacetEntries
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|v1
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|reason
argument_list|,
name|actualFacetEntries
operator|.
name|getEntries
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedFacetEntries
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|v2
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"Failed with seed:"
operator|+
name|seed
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
DECL|method|addControlValues
specifier|private
name|void
name|addControlValues
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|queryValToFacetFieldEntries
parameter_list|,
name|String
name|fieldVal
parameter_list|,
name|String
name|queryVal
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|controlFieldFacets
init|=
name|queryValToFacetFieldEntries
operator|.
name|get
argument_list|(
name|queryVal
argument_list|)
decl_stmt|;
if|if
condition|(
name|controlFieldFacets
operator|==
literal|null
condition|)
block|{
name|controlFieldFacets
operator|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
expr_stmt|;
name|queryValToFacetFieldEntries
operator|.
name|put
argument_list|(
name|queryVal
argument_list|,
name|controlFieldFacets
argument_list|)
expr_stmt|;
block|}
name|Integer
name|controlCount
init|=
name|controlFieldFacets
operator|.
name|get
argument_list|(
name|fieldVal
argument_list|)
decl_stmt|;
if|if
condition|(
name|controlCount
operator|==
literal|null
condition|)
block|{
name|controlCount
operator|=
literal|0
expr_stmt|;
block|}
name|controlFieldFacets
operator|.
name|put
argument_list|(
name|fieldVal
argument_list|,
operator|++
name|controlCount
argument_list|)
expr_stmt|;
block|}
DECL|method|addControlValues
specifier|private
name|void
name|addControlValues
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|queryValToFacetFieldEntries
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|fieldValues
parameter_list|,
name|String
name|queryVal
parameter_list|)
block|{
for|for
control|(
name|String
name|fieldValue
range|:
name|fieldValues
control|)
block|{
name|addControlValues
argument_list|(
name|queryValToFacetFieldEntries
argument_list|,
name|fieldValue
argument_list|,
name|queryVal
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|getExpectedFacetEntries
specifier|private
name|List
argument_list|<
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|getExpectedFacetEntries
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|fieldValues
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|controlFacetsField
parameter_list|,
name|int
name|size
parameter_list|,
name|TermsFacet
operator|.
name|ComparatorType
name|sort
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|excludes
parameter_list|,
name|String
name|regex
parameter_list|,
name|boolean
name|allTerms
parameter_list|)
block|{
name|Pattern
name|pattern
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|regex
operator|!=
literal|null
condition|)
block|{
name|pattern
operator|=
name|Regex
operator|.
name|compile
argument_list|(
name|regex
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|entries
init|=
operator|new
name|ArrayList
argument_list|<
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|e
range|:
name|controlFacetsField
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|excludes
operator|.
name|contains
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|pattern
operator|!=
literal|null
operator|&&
operator|!
name|pattern
operator|.
name|matcher
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|entries
operator|.
name|add
argument_list|(
operator|new
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
argument_list|(
operator|new
name|StringText
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|,
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allTerms
condition|)
block|{
for|for
control|(
name|String
name|fieldValue
range|:
name|fieldValues
control|)
block|{
if|if
condition|(
operator|!
name|controlFacetsField
operator|.
name|containsKey
argument_list|(
name|fieldValue
argument_list|)
condition|)
block|{
if|if
condition|(
name|excludes
operator|.
name|contains
argument_list|(
name|fieldValue
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|pattern
operator|!=
literal|null
operator|&&
operator|!
name|pattern
operator|.
name|matcher
argument_list|(
name|fieldValue
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
continue|continue;
block|}
name|entries
operator|.
name|add
argument_list|(
operator|new
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
argument_list|(
operator|new
name|StringText
argument_list|(
name|fieldValue
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
switch|switch
condition|(
name|sort
condition|)
block|{
case|case
name|COUNT
case|:
name|Collections
operator|.
name|sort
argument_list|(
name|entries
argument_list|,
name|count
argument_list|)
expr_stmt|;
break|break;
case|case
name|REVERSE_COUNT
case|:
name|Collections
operator|.
name|sort
argument_list|(
name|entries
argument_list|,
name|count_reverse
argument_list|)
expr_stmt|;
break|break;
case|case
name|TERM
case|:
name|Collections
operator|.
name|sort
argument_list|(
name|entries
argument_list|,
name|term
argument_list|)
expr_stmt|;
break|break;
case|case
name|REVERSE_TERM
case|:
name|Collections
operator|.
name|sort
argument_list|(
name|entries
argument_list|,
name|term_reverse
argument_list|)
expr_stmt|;
break|break;
block|}
return|return
name|size
operator|>=
name|entries
operator|.
name|size
argument_list|()
condition|?
name|entries
else|:
name|entries
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|size
argument_list|)
return|;
block|}
DECL|field|count
specifier|private
specifier|final
specifier|static
name|Count
name|count
init|=
operator|new
name|Count
argument_list|()
decl_stmt|;
DECL|field|count_reverse
specifier|private
specifier|final
specifier|static
name|CountReverse
name|count_reverse
init|=
operator|new
name|CountReverse
argument_list|()
decl_stmt|;
DECL|field|term
specifier|private
specifier|final
specifier|static
name|Term
name|term
init|=
operator|new
name|Term
argument_list|()
decl_stmt|;
DECL|field|term_reverse
specifier|private
specifier|final
specifier|static
name|TermReverse
name|term_reverse
init|=
operator|new
name|TermReverse
argument_list|()
decl_stmt|;
DECL|class|Count
specifier|private
specifier|static
class|class
name|Count
implements|implements
name|Comparator
argument_list|<
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
name|o1
parameter_list|,
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
name|o2
parameter_list|)
block|{
name|int
name|cmp
init|=
name|o2
operator|.
name|v2
argument_list|()
operator|-
name|o1
operator|.
name|v2
argument_list|()
decl_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
name|cmp
operator|=
name|o2
operator|.
name|v1
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o1
operator|.
name|v1
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
return|return
name|System
operator|.
name|identityHashCode
argument_list|(
name|o2
argument_list|)
operator|-
name|System
operator|.
name|identityHashCode
argument_list|(
name|o1
argument_list|)
return|;
block|}
block|}
DECL|class|CountReverse
specifier|private
specifier|static
class|class
name|CountReverse
implements|implements
name|Comparator
argument_list|<
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
name|o1
parameter_list|,
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
name|o2
parameter_list|)
block|{
return|return
operator|-
name|count
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
return|;
block|}
block|}
DECL|class|Term
specifier|private
specifier|static
class|class
name|Term
implements|implements
name|Comparator
argument_list|<
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
name|o1
parameter_list|,
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
name|o2
parameter_list|)
block|{
return|return
name|o1
operator|.
name|v1
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|v1
argument_list|()
argument_list|)
return|;
block|}
block|}
DECL|class|TermReverse
specifier|private
specifier|static
class|class
name|TermReverse
implements|implements
name|Comparator
argument_list|<
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
name|o1
parameter_list|,
name|Tuple
argument_list|<
name|Text
argument_list|,
name|Integer
argument_list|>
name|o2
parameter_list|)
block|{
return|return
operator|-
name|term
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

