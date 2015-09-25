begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|PostingsEnum
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|TermsEnum
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRef
import|;
end_import

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
name|action
operator|.
name|termvectors
operator|.
name|TermVectorsRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|termvectors
operator|.
name|TermVectorsResponse
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
name|Priority
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|SearchHitField
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
name|SearchModule
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
name|SearchParseElement
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
name|builder
operator|.
name|SearchSourceBuilder
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
name|internal
operator|.
name|InternalSearchHit
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
name|internal
operator|.
name|InternalSearchHitField
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
name|internal
operator|.
name|SearchContext
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
name|ESIntegTestCase
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
name|ESIntegTestCase
operator|.
name|ClusterScope
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
name|ESIntegTestCase
operator|.
name|Scope
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|indexRequest
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
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertSearchResponse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|FetchSubPhasePluginIT
specifier|public
class|class
name|FetchSubPhasePluginIT
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|pluginList
argument_list|(
name|FetchTermVectorsPlugin
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|testPlugin
specifier|public
name|void
name|testPlugin
parameter_list|()
throws|throws
name|Exception
block|{
name|client
argument_list|()
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
literal|"test"
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
literal|"term_vector"
argument_list|,
literal|"yes"
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
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForYellowStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"I am sam i am"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
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
name|XContentBuilder
name|extSource
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"term_vectors_fetch"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSource
argument_list|(
operator|new
name|SearchSourceBuilder
argument_list|()
operator|.
name|ext
argument_list|(
name|extSource
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
operator|)
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|field
argument_list|(
literal|"term_vectors_fetch"
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"i"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
operator|)
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|field
argument_list|(
literal|"term_vectors_fetch"
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"am"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
operator|)
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|field
argument_list|(
literal|"term_vectors_fetch"
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"sam"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|FetchTermVectorsPlugin
specifier|public
specifier|static
class|class
name|FetchTermVectorsPlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"fetch-term-vectors"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"fetch plugin to test if the plugin mechanism works"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|SearchModule
name|searchModule
parameter_list|)
block|{
name|searchModule
operator|.
name|registerFetchSubPhase
argument_list|(
name|TermVectorsFetchSubPhase
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|TermVectorsFetchSubPhase
specifier|public
specifier|static
class|class
name|TermVectorsFetchSubPhase
implements|implements
name|FetchSubPhase
block|{
DECL|field|CONTEXT_FACTORY
specifier|public
specifier|static
specifier|final
name|ContextFactory
argument_list|<
name|TermVectorsFetchContext
argument_list|>
name|CONTEXT_FACTORY
init|=
operator|new
name|ContextFactory
argument_list|<
name|TermVectorsFetchContext
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|NAMES
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|TermVectorsFetchContext
name|newContextInstance
parameter_list|()
block|{
return|return
operator|new
name|TermVectorsFetchContext
argument_list|()
return|;
block|}
block|}
decl_stmt|;
DECL|method|TermVectorsFetchSubPhase
specifier|public
name|TermVectorsFetchSubPhase
parameter_list|()
block|{         }
DECL|field|NAMES
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|NAMES
init|=
block|{
literal|"term_vectors_fetch"
block|}
decl_stmt|;
annotation|@
name|Override
DECL|method|parseElements
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|?
extends|extends
name|SearchParseElement
argument_list|>
name|parseElements
parameter_list|()
block|{
return|return
name|ImmutableMap
operator|.
name|of
argument_list|(
literal|"term_vectors_fetch"
argument_list|,
operator|new
name|TermVectorsFetchParseElement
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hitsExecutionNeeded
specifier|public
name|boolean
name|hitsExecutionNeeded
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|hitsExecute
specifier|public
name|void
name|hitsExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|InternalSearchHit
index|[]
name|hits
parameter_list|)
block|{         }
annotation|@
name|Override
DECL|method|hitExecutionNeeded
specifier|public
name|boolean
name|hitExecutionNeeded
parameter_list|(
name|SearchContext
name|context
parameter_list|)
block|{
return|return
name|context
operator|.
name|getFetchSubPhaseContext
argument_list|(
name|CONTEXT_FACTORY
argument_list|)
operator|.
name|hitExecutionNeeded
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|hitExecute
specifier|public
name|void
name|hitExecute
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|HitContext
name|hitContext
parameter_list|)
block|{
name|String
name|field
init|=
name|context
operator|.
name|getFetchSubPhaseContext
argument_list|(
name|CONTEXT_FACTORY
argument_list|)
operator|.
name|getField
argument_list|()
decl_stmt|;
if|if
condition|(
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|fieldsOrNull
argument_list|()
operator|==
literal|null
condition|)
block|{
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|fields
argument_list|(
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|SearchHitField
name|hitField
init|=
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|fields
argument_list|()
operator|.
name|get
argument_list|(
name|NAMES
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|hitField
operator|==
literal|null
condition|)
block|{
name|hitField
operator|=
operator|new
name|InternalSearchHitField
argument_list|(
name|NAMES
index|[
literal|0
index|]
argument_list|,
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|fields
argument_list|()
operator|.
name|put
argument_list|(
name|NAMES
index|[
literal|0
index|]
argument_list|,
name|hitField
argument_list|)
expr_stmt|;
block|}
name|TermVectorsResponse
name|termVector
init|=
name|context
operator|.
name|indexShard
argument_list|()
operator|.
name|termVectorsService
argument_list|()
operator|.
name|getTermVectors
argument_list|(
operator|new
name|TermVectorsRequest
argument_list|(
name|context
operator|.
name|indexShard
argument_list|()
operator|.
name|indexService
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|hitContext
operator|.
name|hit
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
name|context
operator|.
name|indexShard
argument_list|()
operator|.
name|indexService
argument_list|()
operator|.
name|index
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|tv
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|TermsEnum
name|terms
init|=
name|termVector
operator|.
name|getFields
argument_list|()
operator|.
name|terms
argument_list|(
name|field
argument_list|)
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|BytesRef
name|term
decl_stmt|;
while|while
condition|(
operator|(
name|term
operator|=
name|terms
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|tv
operator|.
name|put
argument_list|(
name|term
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|terms
operator|.
name|postings
argument_list|(
literal|null
argument_list|,
name|PostingsEnum
operator|.
name|ALL
argument_list|)
operator|.
name|freq
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|hitField
operator|.
name|values
argument_list|()
operator|.
name|add
argument_list|(
name|tv
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|class|TermVectorsFetchParseElement
specifier|public
specifier|static
class|class
name|TermVectorsFetchParseElement
extends|extends
name|FetchSubPhaseParseElement
argument_list|<
name|TermVectorsFetchContext
argument_list|>
block|{
annotation|@
name|Override
DECL|method|innerParse
specifier|protected
name|void
name|innerParse
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|TermVectorsFetchContext
name|termVectorsFetchContext
parameter_list|,
name|SearchContext
name|searchContext
parameter_list|)
throws|throws
name|Exception
block|{
name|XContentParser
operator|.
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
name|String
name|fieldName
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|termVectorsFetchContext
operator|.
name|setField
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Expected a VALUE_STRING but got "
operator|+
name|token
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|getContextFactory
specifier|protected
name|FetchSubPhase
operator|.
name|ContextFactory
name|getContextFactory
parameter_list|()
block|{
return|return
name|TermVectorsFetchSubPhase
operator|.
name|CONTEXT_FACTORY
return|;
block|}
block|}
DECL|class|TermVectorsFetchContext
specifier|public
specifier|static
class|class
name|TermVectorsFetchContext
extends|extends
name|FetchSubPhaseContext
block|{
DECL|field|field
specifier|private
name|String
name|field
init|=
literal|null
decl_stmt|;
DECL|method|TermVectorsFetchContext
specifier|public
name|TermVectorsFetchContext
parameter_list|()
block|{         }
DECL|method|setField
specifier|public
name|void
name|setField
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
block|}
DECL|method|getField
specifier|public
name|String
name|getField
parameter_list|()
block|{
return|return
name|field
return|;
block|}
block|}
block|}
end_class

end_unit

