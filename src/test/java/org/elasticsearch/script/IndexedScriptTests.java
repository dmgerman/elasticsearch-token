begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|unit
operator|.
name|TimeValue
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
name|expression
operator|.
name|ExpressionScriptEngineService
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
name|groovy
operator|.
name|GroovyScriptEngineService
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
name|SearchHit
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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
name|assertHitCount
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
name|*
import|;
end_import

begin_class
DECL|class|IndexedScriptTests
specifier|public
class|class
name|IndexedScriptTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
name|ImmutableSettings
operator|.
name|Builder
name|builder
init|=
name|ImmutableSettings
operator|.
name|builder
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
decl_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"script.engine.groovy.indexed.update"
argument_list|,
literal|"off"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"script.engine.groovy.indexed.search"
argument_list|,
literal|"on"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"script.engine.groovy.indexed.aggs"
argument_list|,
literal|"on"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"script.engine.groovy.inline.aggs"
argument_list|,
literal|"off"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"script.engine.expression.indexed.update"
argument_list|,
literal|"off"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"script.engine.expression.indexed.search"
argument_list|,
literal|"off"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"script.engine.expression.indexed.aggs"
argument_list|,
literal|"off"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|put
argument_list|(
literal|"script.engine.expression.indexed.mapping"
argument_list|,
literal|"off"
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testFieldIndexedScript
specifier|public
name|void
name|testFieldIndexedScript
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
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
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|,
literal|"groovy"
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{"
operator|+
literal|"\"script\":\"2\""
operator|+
literal|"}"
argument_list|)
operator|.
name|setTimeout
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
argument_list|)
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
literal|"groovy"
argument_list|,
literal|"script2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{"
operator|+
literal|"\"script\":\"factor*2\""
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
literal|"scriptTest"
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
literal|"scriptTest"
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
literal|"scriptTest"
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
literal|"scriptTest"
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
literal|"scriptTest"
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
name|String
name|query
init|=
literal|"{ \"query\" : { \"match_all\": {}} , \"script_fields\" : { \"test1\" : { \"script_id\" : \"script1\", \"lang\":\"groovy\" }, \"test2\" : { \"script_id\" : \"script2\", \"lang\":\"groovy\", \"params\":{\"factor\":3}  }}, size:1}"
decl_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSource
argument_list|(
name|query
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"scriptTest"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|assertTrue
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
operator|==
literal|1
argument_list|)
expr_stmt|;
name|SearchHit
name|sh
init|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
name|Integer
operator|)
name|sh
operator|.
name|field
argument_list|(
literal|"test1"
argument_list|)
operator|.
name|getValue
argument_list|()
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
name|Integer
operator|)
name|sh
operator|.
name|field
argument_list|(
literal|"test2"
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|6
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Relates to #10397
annotation|@
name|Test
DECL|method|testUpdateScripts
specifier|public
name|void
name|testUpdateScripts
parameter_list|()
block|{
name|createIndex
argument_list|(
literal|"test_index"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test_index"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test_index"
argument_list|,
literal|"test_type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"foo\":\"bar\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|flush
argument_list|(
literal|"test_index"
argument_list|)
expr_stmt|;
name|int
name|iterations
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|11
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|iterations
condition|;
name|i
operator|++
control|)
block|{
name|PutIndexedScriptResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|preparePutIndexedScript
argument_list|(
name|GroovyScriptEngineService
operator|.
name|NAME
argument_list|,
literal|"script1"
argument_list|,
literal|"{\"script\":\""
operator|+
name|i
operator|+
literal|"\"}"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|i
argument_list|,
name|response
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{"
operator|+
literal|" \"query\" : { \"match_all\": {}}, "
operator|+
literal|" \"script_fields\" : { \"test_field\" : { \"script_id\" : \"script1\", \"lang\":\"groovy\" } } }"
decl_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSource
argument_list|(
name|query
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test_index"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"test_type"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|SearchHit
name|sh
init|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
name|Integer
operator|)
name|sh
operator|.
name|field
argument_list|(
literal|"test_field"
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testDisabledUpdateIndexedScriptsOnly
specifier|public
name|void
name|testDisabledUpdateIndexedScriptsOnly
parameter_list|()
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|client
argument_list|()
operator|.
name|preparePutIndexedScript
argument_list|(
name|GroovyScriptEngineService
operator|.
name|NAME
argument_list|,
literal|"script1"
argument_list|,
literal|"{\"script\":\"2\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|,
name|GroovyScriptEngineService
operator|.
name|NAME
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"script\":\"2\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"scriptTest"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareUpdate
argument_list|(
literal|"test"
argument_list|,
literal|"scriptTest"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setScript
argument_list|(
literal|"script1"
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|)
operator|.
name|setScriptLang
argument_list|(
name|GroovyScriptEngineService
operator|.
name|NAME
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"update script should have been rejected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"failed to execute script"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"scripts of type [indexed], operation [update] and lang [groovy] are disabled"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testDisabledAggsDynamicScripts
specifier|public
name|void
name|testDisabledAggsDynamicScripts
parameter_list|()
block|{
comment|//dynamic scripts don't need to be enabled for an indexed script to be indexed and later on executed
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|client
argument_list|()
operator|.
name|preparePutIndexedScript
argument_list|(
name|GroovyScriptEngineService
operator|.
name|NAME
argument_list|,
literal|"script1"
argument_list|,
literal|"{\"script\":\"2\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|,
name|GroovyScriptEngineService
operator|.
name|NAME
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"script\":\"2\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"scriptTest"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|String
name|source
init|=
literal|"{\"aggs\": {\"test\": { \"terms\" : { \"script_id\":\"script1\" } } } }"
decl_stmt|;
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
name|setSource
argument_list|(
name|source
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testAllOpsDisabledIndexedScripts
specifier|public
name|void
name|testAllOpsDisabledIndexedScripts
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|client
argument_list|()
operator|.
name|preparePutIndexedScript
argument_list|(
name|ExpressionScriptEngineService
operator|.
name|NAME
argument_list|,
literal|"script1"
argument_list|,
literal|"{\"script\":\"2\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_INDEX
argument_list|,
name|ExpressionScriptEngineService
operator|.
name|NAME
argument_list|,
literal|"script1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"script\":\"2\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"scriptTest"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{\"theField\":\"foo\"}"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareUpdate
argument_list|(
literal|"test"
argument_list|,
literal|"scriptTest"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setScript
argument_list|(
literal|"script1"
argument_list|,
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INDEXED
argument_list|)
operator|.
name|setScriptLang
argument_list|(
name|ExpressionScriptEngineService
operator|.
name|NAME
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"update script should have been rejected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"failed to execute script"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"scripts of type [indexed], operation [update] and lang [expression] are disabled"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|String
name|query
init|=
literal|"{ \"script_fields\" : { \"test1\" : { \"script_id\" : \"script1\", \"lang\":\"expression\" }}}"
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSource
argument_list|(
name|query
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setTypes
argument_list|(
literal|"scriptTest"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"search script should have been rejected"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"scripts of type [indexed], operation [search] and lang [expression] are disabled"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|String
name|source
init|=
literal|"{\"aggs\": {\"test\": { \"terms\" : { \"script_id\":\"script1\", \"script_lang\":\"expression\" } } } }"
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"scripts of type [indexed], operation [aggs] and lang [expression] are disabled"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

