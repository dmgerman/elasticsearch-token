begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|reindex
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
name|index
operator|.
name|IndexRequest
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
name|script
operator|.
name|ExecutableScript
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
name|SearchShardTarget
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
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonMap
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

begin_class
DECL|class|AbstractAsyncBulkIndexByScrollActionScriptTestCase
specifier|public
specifier|abstract
class|class
name|AbstractAsyncBulkIndexByScrollActionScriptTestCase
parameter_list|<
name|Request
extends|extends
name|AbstractBulkIndexByScrollRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|,
name|Response
extends|extends
name|BulkIndexByScrollResponse
parameter_list|>
extends|extends
name|AbstractAsyncBulkIndexByScrollActionTestCase
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|method|applyScript
specifier|protected
name|IndexRequest
name|applyScript
parameter_list|(
name|Consumer
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|scriptBody
parameter_list|)
block|{
name|IndexRequest
name|index
init|=
operator|new
name|IndexRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|source
argument_list|(
name|singletonMap
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
name|fields
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|InternalSearchHit
name|doc
init|=
operator|new
name|InternalSearchHit
argument_list|(
literal|0
argument_list|,
literal|"id"
argument_list|,
operator|new
name|StringText
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|fields
argument_list|)
decl_stmt|;
name|doc
operator|.
name|shardTarget
argument_list|(
operator|new
name|SearchShardTarget
argument_list|(
literal|"nodeid"
argument_list|,
literal|"index"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|ExecutableScript
name|script
init|=
operator|new
name|SimpleExecutableScript
argument_list|(
name|scriptBody
argument_list|)
decl_stmt|;
name|action
argument_list|()
operator|.
name|applyScript
argument_list|(
name|index
argument_list|,
name|doc
argument_list|,
name|script
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|index
return|;
block|}
DECL|method|testScriptAddingJunkToCtxIsError
specifier|public
name|void
name|testScriptAddingJunkToCtxIsError
parameter_list|()
block|{
try|try
block|{
name|applyScript
argument_list|(
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|ctx
parameter_list|)
lambda|->
name|ctx
operator|.
name|put
argument_list|(
literal|"junk"
argument_list|,
literal|"junk"
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected error"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
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
name|equalTo
argument_list|(
literal|"Invalid fields added to ctx [junk]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testChangeSource
specifier|public
name|void
name|testChangeSource
parameter_list|()
block|{
name|IndexRequest
name|index
init|=
name|applyScript
argument_list|(
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|ctx
parameter_list|)
lambda|->
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ctx
operator|.
name|get
argument_list|(
literal|"_source"
argument_list|)
decl_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"bar"
argument_list|,
literal|"cat"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"cat"
argument_list|,
name|index
operator|.
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

