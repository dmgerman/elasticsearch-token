begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|Maps
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
name|index
operator|.
name|IndexRequestBuilder
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
name|indexedscripts
operator|.
name|delete
operator|.
name|DeleteIndexedScriptResponse
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
name|indexedscripts
operator|.
name|get
operator|.
name|GetIndexedScriptResponse
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
name|indexedscripts
operator|.
name|put
operator|.
name|PutIndexedScriptResponse
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
name|SearchPhaseExecutionException
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
name|SearchRequest
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
name|common
operator|.
name|bytes
operator|.
name|BytesArray
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
name|bytes
operator|.
name|BytesReference
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
name|script
operator|.
name|ScriptService
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
name|ElasticsearchIntegrationTest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
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
name|*
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_comment
comment|/**  * Full integration test of the template query plugin.  */
end_comment

begin_class
annotation|@
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ElasticsearchIntegrationTest
operator|.
name|Scope
operator|.
name|SUITE
argument_list|)
DECL|class|TemplateQueryTest
specifier|public
class|class
name|TemplateQueryTest
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"testtype"
argument_list|,
literal|"1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"testtype"
argument_list|,
literal|"2"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|nodeSettings
specifier|public
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.conf"
argument_list|,
name|this
operator|.
name|getResource
argument_list|(
literal|"config"
argument_list|)
operator|.
name|getPath
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testTemplateInBody
specifier|public
name|void
name|testTemplateInBody
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|vars
operator|.
name|put
argument_list|(
literal|"template"
argument_list|,
literal|"all"
argument_list|)
expr_stmt|;
name|TemplateQueryBuilder
name|builder
init|=
operator|new
name|TemplateQueryBuilder
argument_list|(
literal|"{\"match_{{template}}\": {}}\""
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|SearchResponse
name|sr
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|builder
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|sr
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testTemplateInBodyWithSize
specifier|public
name|void
name|testTemplateInBodyWithSize
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|request
init|=
literal|"{\n"
operator|+
literal|"    \"size\":0,"
operator|+
literal|"    \"query\": {\n"
operator|+
literal|"        \"template\": {\n"
operator|+
literal|"            \"query\": {\"match_{{template}}\": {}},\n"
operator|+
literal|"            \"params\" : {\n"
operator|+
literal|"                \"template\" : \"all\"\n"
operator|+
literal|"            }\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
decl_stmt|;
name|SearchResponse
name|sr
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSource
argument_list|(
name|request
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|sr
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sr
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|request
operator|=
literal|"{\n"
operator|+
literal|"    \"query\": {\n"
operator|+
literal|"        \"template\": {\n"
operator|+
literal|"            \"query\": {\"match_{{template}}\": {}},\n"
operator|+
literal|"            \"params\" : {\n"
operator|+
literal|"                \"template\" : \"all\"\n"
operator|+
literal|"            }\n"
operator|+
literal|"        }\n"
operator|+
literal|"    },\n"
operator|+
literal|"    \"size\":0"
operator|+
literal|"}"
expr_stmt|;
name|sr
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSource
argument_list|(
name|request
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|sr
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sr
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testTemplateWOReplacementInBody
specifier|public
name|void
name|testTemplateWOReplacementInBody
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|TemplateQueryBuilder
name|builder
init|=
operator|new
name|TemplateQueryBuilder
argument_list|(
literal|"{\"match_all\": {}}\""
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|SearchResponse
name|sr
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|builder
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|sr
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testTemplateInFile
specifier|public
name|void
name|testTemplateInFile
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|vars
operator|.
name|put
argument_list|(
literal|"template"
argument_list|,
literal|"all"
argument_list|)
expr_stmt|;
name|TemplateQueryBuilder
name|builder
init|=
operator|new
name|TemplateQueryBuilder
argument_list|(
literal|"storedTemplate"
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|FILE
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|SearchResponse
name|sr
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|builder
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|sr
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testRawEscapedTemplate
specifier|public
name|void
name|testRawEscapedTemplate
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
init|=
literal|"{\"template\": {\"query\": \"{\\\"match_{{template}}\\\": {}}\\\"\",\"params\" : {\"template\" : \"all\"}}}"
decl_stmt|;
name|SearchResponse
name|sr
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|query
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|sr
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testRawTemplate
specifier|public
name|void
name|testRawTemplate
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
init|=
literal|"{\"template\": {\"query\": {\"match_{{template}}\": {}},\"params\" : {\"template\" : \"all\"}}}"
decl_stmt|;
name|SearchResponse
name|sr
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|query
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|sr
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testRawFSTemplate
specifier|public
name|void
name|testRawFSTemplate
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
init|=
literal|"{\"template\": {\"file\": \"storedTemplate\",\"params\" : {\"template\" : \"all\"}}}"
decl_stmt|;
name|SearchResponse
name|sr
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|query
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|sr
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSearchRequestTemplateSource
specifier|public
name|void
name|testSearchRequestTemplateSource
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchRequest
name|searchRequest
init|=
operator|new
name|SearchRequest
argument_list|()
decl_stmt|;
name|searchRequest
operator|.
name|indices
argument_list|(
literal|"_all"
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{ \"template\" : { \"query\": {\"match_{{template}}\": {} } }, \"params\" : { \"template\":\"all\" } }"
decl_stmt|;
name|BytesReference
name|bytesRef
init|=
operator|new
name|BytesArray
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|searchRequest
operator|.
name|templateSource
argument_list|(
name|bytesRef
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// Releates to #6318
DECL|method|testSearchRequestFail
specifier|public
name|void
name|testSearchRequestFail
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchRequest
name|searchRequest
init|=
operator|new
name|SearchRequest
argument_list|()
decl_stmt|;
name|searchRequest
operator|.
name|indices
argument_list|(
literal|"_all"
argument_list|)
expr_stmt|;
try|try
block|{
name|String
name|query
init|=
literal|"{ \"template\" : { \"query\": {\"match_all\": {}}, \"size\" : \"{{my_size}}\"  } }"
decl_stmt|;
name|BytesReference
name|bytesRef
init|=
operator|new
name|BytesArray
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|searchRequest
operator|.
name|templateSource
argument_list|(
name|bytesRef
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"expected exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|ex
parameter_list|)
block|{
comment|// expected - no params
block|}
name|String
name|query
init|=
literal|"{ \"template\" : { \"query\": {\"match_all\": {}}, \"size\" : \"{{my_size}}\"  }, \"params\" : { \"my_size\": 1 } }"
decl_stmt|;
name|BytesReference
name|bytesRef
init|=
operator|new
name|BytesArray
argument_list|(
name|query
argument_list|)
decl_stmt|;
name|searchRequest
operator|.
name|templateSource
argument_list|(
name|bytesRef
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testThatParametersCanBeSet
specifier|public
name|void
name|testThatParametersCanBeSet
parameter_list|()
throws|throws
name|Exception
block|{
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"theField"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"2"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"theField"
argument_list|,
literal|"foo 2"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"3"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"theField"
argument_list|,
literal|"foo 3"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"4"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"theField"
argument_list|,
literal|"foo 4"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"5"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"otherField"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateParams
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|templateParams
operator|.
name|put
argument_list|(
literal|"mySize"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|templateParams
operator|.
name|put
argument_list|(
literal|"myField"
argument_list|,
literal|"theField"
argument_list|)
expr_stmt|;
name|templateParams
operator|.
name|put
argument_list|(
literal|"myValue"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setTemplateName
argument_list|(
literal|"full-query-template"
argument_list|)
operator|.
name|setTemplateParams
argument_list|(
name|templateParams
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|4
argument_list|)
expr_stmt|;
comment|// size kicks in here...
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|templateParams
operator|.
name|put
argument_list|(
literal|"myField"
argument_list|,
literal|"otherField"
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setTemplateName
argument_list|(
literal|"full-query-template"
argument_list|)
operator|.
name|setTemplateParams
argument_list|(
name|templateParams
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIndexedTemplateClient
specifier|public
name|void
name|testIndexedTemplateClient
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|)
expr_stmt|;
name|PutIndexedScriptResponse
name|scriptResponse
init|=
name|client
argument_list|()
operator|.
name|preparePutIndexedScript
argument_list|(
literal|"mustache"
argument_list|,
literal|"testTemplate"
argument_list|,
literal|"{"
operator|+
literal|"\"template\":{"
operator|+
literal|"                \"query\":{"
operator|+
literal|"                   \"match\":{"
operator|+
literal|"                    \"theField\" : \"{{fieldParam}}\"}"
operator|+
literal|"       }"
operator|+
literal|"}"
operator|+
literal|"}"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|scriptResponse
operator|.
name|isCreated
argument_list|()
argument_list|)
expr_stmt|;
name|scriptResponse
operator|=
name|client
argument_list|()
operator|.
name|preparePutIndexedScript
argument_list|(
literal|"mustache"
argument_list|,
literal|"testTemplate"
argument_list|,
literal|"{"
operator|+
literal|"\"template\":{"
operator|+
literal|"                \"query\":{"
operator|+
literal|"                   \"match\":{"
operator|+
literal|"                    \"theField\" : \"{{fieldParam}}\"}"
operator|+
literal|"       }"
operator|+
literal|"}"
operator|+
literal|"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|scriptResponse
operator|.
name|getVersion
argument_list|()
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|GetIndexedScriptResponse
name|getResponse
init|=
name|client
argument_list|()
operator|.
name|prepareGetIndexedScript
argument_list|(
literal|"mustache"
argument_list|,
literal|"testTemplate"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|builders
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo 2\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo 3\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"4"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo 4\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"bar\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|builders
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateParams
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|templateParams
operator|.
name|put
argument_list|(
literal|"fieldParam"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setTemplateName
argument_list|(
literal|"testTemplate"
argument_list|)
operator|.
name|setTemplateType
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|)
operator|.
name|setTemplateType
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|)
operator|.
name|setTemplateParams
argument_list|(
name|templateParams
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|DeleteIndexedScriptResponse
name|deleteResponse
init|=
name|client
argument_list|()
operator|.
name|prepareDeleteIndexedScript
argument_list|(
literal|"mustache"
argument_list|,
literal|"testTemplate"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|deleteResponse
operator|.
name|isFound
argument_list|()
operator|==
literal|true
argument_list|)
expr_stmt|;
name|getResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareGetIndexedScript
argument_list|(
literal|"mustache"
argument_list|,
literal|"testTemplate"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertFalse
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|Exception
name|e
init|=
literal|null
decl_stmt|;
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setTemplateName
argument_list|(
literal|"/template_index/mustache/1000"
argument_list|)
operator|.
name|setTemplateType
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|)
operator|.
name|setTemplateParams
argument_list|(
name|templateParams
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|spee
parameter_list|)
block|{
name|e
operator|=
name|spee
expr_stmt|;
block|}
assert|assert
name|e
operator|!=
literal|null
assert|;
name|e
operator|=
literal|null
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testIndexedTemplate
specifier|public
name|void
name|testIndexedTemplate
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|builders
init|=
operator|new
name|ArrayList
argument_list|<
name|IndexRequestBuilder
argument_list|>
argument_list|()
decl_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|,
literal|"mustache"
argument_list|,
literal|"1a"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{"
operator|+
literal|"\"template\":{"
operator|+
literal|"                \"query\":{"
operator|+
literal|"                   \"match\":{"
operator|+
literal|"                    \"theField\" : \"{{fieldParam}}\"}"
operator|+
literal|"       }"
operator|+
literal|"}"
operator|+
literal|"}"
argument_list|)
argument_list|)
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|,
literal|"mustache"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{"
operator|+
literal|"\"template\":{"
operator|+
literal|"                \"query\":{"
operator|+
literal|"                   \"match\":{"
operator|+
literal|"                    \"theField\" : \"{{fieldParam}}\"}"
operator|+
literal|"       }"
operator|+
literal|"}"
operator|+
literal|"}"
argument_list|)
argument_list|)
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|,
literal|"mustache"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{"
operator|+
literal|"\"template\":{"
operator|+
literal|"             \"match\":{"
operator|+
literal|"                    \"theField\" : \"{{fieldParam}}\"}"
operator|+
literal|"       }"
operator|+
literal|"}"
argument_list|)
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|builders
argument_list|)
expr_stmt|;
name|builders
operator|.
name|clear
argument_list|()
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo 2\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo 3\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"4"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo 4\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"5"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"bar\"}"
argument_list|)
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|builders
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|templateParams
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|templateParams
operator|.
name|put
argument_list|(
literal|"fieldParam"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setTemplateName
argument_list|(
literal|"/mustache/1a"
argument_list|)
operator|.
name|setTemplateType
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|)
operator|.
name|setTemplateParams
argument_list|(
name|templateParams
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|Exception
name|e
init|=
literal|null
decl_stmt|;
try|try
block|{
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setTemplateName
argument_list|(
literal|"/template_index/mustache/1000"
argument_list|)
operator|.
name|setTemplateType
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|)
operator|.
name|setTemplateParams
argument_list|(
name|templateParams
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|spee
parameter_list|)
block|{
name|e
operator|=
name|spee
expr_stmt|;
block|}
assert|assert
name|e
operator|!=
literal|null
assert|;
name|e
operator|=
literal|null
expr_stmt|;
try|try
block|{
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setTemplateName
argument_list|(
literal|"/myindex/mustache/1"
argument_list|)
operator|.
name|setTemplateType
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|)
operator|.
name|setTemplateParams
argument_list|(
name|templateParams
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertFailures
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|spee
parameter_list|)
block|{
name|e
operator|=
name|spee
expr_stmt|;
block|}
assert|assert
name|e
operator|!=
literal|null
assert|;
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setTemplateName
argument_list|(
literal|"1a"
argument_list|)
operator|.
name|setTemplateType
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|)
operator|.
name|setTemplateParams
argument_list|(
name|templateParams
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|templateParams
operator|.
name|put
argument_list|(
literal|"fieldParam"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setTemplateName
argument_list|(
literal|"/mustache/2"
argument_list|)
operator|.
name|setTemplateType
argument_list|(
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|)
operator|.
name|setTemplateParams
argument_list|(
name|templateParams
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|vars
operator|.
name|put
argument_list|(
literal|"fieldParam"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|TemplateQueryBuilder
name|builder
init|=
operator|new
name|TemplateQueryBuilder
argument_list|(
literal|"3"
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|SearchResponse
name|sr
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|builder
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|sr
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\"template\": {\"id\": \"3\",\"params\" : {\"fieldParam\" : \"foo\"}}}"
decl_stmt|;
name|sr
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|query
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|sr
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|query
operator|=
literal|"{\"template\": {\"id\": \"/mustache/3\",\"params\" : {\"fieldParam\" : \"foo\"}}}"
expr_stmt|;
name|sr
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|query
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|sr
argument_list|,
literal|4
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

