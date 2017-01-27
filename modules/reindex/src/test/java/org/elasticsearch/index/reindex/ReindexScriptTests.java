begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
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
name|bulk
operator|.
name|byscroll
operator|.
name|BulkByScrollResponse
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
name|IndexRequest
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
name|common
operator|.
name|lucene
operator|.
name|uid
operator|.
name|Versions
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_comment
comment|/**  * Tests index-by-search with a script modifying the documents.  */
end_comment

begin_class
DECL|class|ReindexScriptTests
specifier|public
class|class
name|ReindexScriptTests
extends|extends
name|AbstractAsyncBulkByScrollActionScriptTestCase
argument_list|<
name|ReindexRequest
argument_list|,
name|BulkByScrollResponse
argument_list|>
block|{
DECL|method|testSetIndex
specifier|public
name|void
name|testSetIndex
parameter_list|()
throws|throws
name|Exception
block|{
name|Object
name|dest
init|=
name|randomFrom
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|234
block|,
literal|234L
block|,
literal|"pancake"
block|}
argument_list|)
decl_stmt|;
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
name|ctx
operator|.
name|put
argument_list|(
literal|"_index"
argument_list|,
name|dest
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|dest
operator|.
name|toString
argument_list|()
argument_list|,
name|index
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSettingIndexToNullIsError
specifier|public
name|void
name|testSettingIndexToNullIsError
parameter_list|()
throws|throws
name|Exception
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
literal|"_index"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
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
literal|"Can't reindex without a destination index!"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSetType
specifier|public
name|void
name|testSetType
parameter_list|()
throws|throws
name|Exception
block|{
name|Object
name|type
init|=
name|randomFrom
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|234
block|,
literal|234L
block|,
literal|"pancake"
block|}
argument_list|)
decl_stmt|;
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
name|ctx
operator|.
name|put
argument_list|(
literal|"_type"
argument_list|,
name|type
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|type
operator|.
name|toString
argument_list|()
argument_list|,
name|index
operator|.
name|type
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSettingTypeToNullIsError
specifier|public
name|void
name|testSettingTypeToNullIsError
parameter_list|()
throws|throws
name|Exception
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
literal|"_type"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
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
literal|"Can't reindex without a destination type!"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSetId
specifier|public
name|void
name|testSetId
parameter_list|()
throws|throws
name|Exception
block|{
name|Object
name|id
init|=
name|randomFrom
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|null
block|,
literal|234
block|,
literal|234L
block|,
literal|"pancake"
block|}
argument_list|)
decl_stmt|;
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
name|ctx
operator|.
name|put
argument_list|(
literal|"_id"
argument_list|,
name|id
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|id
operator|==
literal|null
condition|)
block|{
name|assertNull
argument_list|(
name|index
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|id
operator|.
name|toString
argument_list|()
argument_list|,
name|index
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSetVersion
specifier|public
name|void
name|testSetVersion
parameter_list|()
throws|throws
name|Exception
block|{
name|Number
name|version
init|=
name|randomFrom
argument_list|(
operator|new
name|Number
index|[]
block|{
literal|null
block|,
literal|234
block|,
literal|234L
block|}
argument_list|)
decl_stmt|;
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
name|ctx
operator|.
name|put
argument_list|(
literal|"_version"
argument_list|,
name|version
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|version
operator|==
literal|null
condition|)
block|{
name|assertEquals
argument_list|(
name|Versions
operator|.
name|MATCH_ANY
argument_list|,
name|index
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertEquals
argument_list|(
name|version
operator|.
name|longValue
argument_list|()
argument_list|,
name|index
operator|.
name|version
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSettingVersionToJunkIsAnError
specifier|public
name|void
name|testSettingVersionToJunkIsAnError
parameter_list|()
throws|throws
name|Exception
block|{
name|Object
name|junkVersion
init|=
name|randomFrom
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"junk"
block|,
name|Math
operator|.
name|PI
block|}
argument_list|)
decl_stmt|;
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
literal|"_version"
argument_list|,
name|junkVersion
argument_list|)
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
name|containsString
argument_list|(
literal|"_version may only be set to an int or a long but was ["
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
name|junkVersion
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSetParent
specifier|public
name|void
name|testSetParent
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|parent
init|=
name|randomRealisticUnicodeOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
decl_stmt|;
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
name|ctx
operator|.
name|put
argument_list|(
literal|"_parent"
argument_list|,
name|parent
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|parent
argument_list|,
name|index
operator|.
name|parent
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSetRouting
specifier|public
name|void
name|testSetRouting
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|routing
init|=
name|randomRealisticUnicodeOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
decl_stmt|;
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
name|ctx
operator|.
name|put
argument_list|(
literal|"_routing"
argument_list|,
name|routing
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|routing
argument_list|,
name|index
operator|.
name|routing
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|request
specifier|protected
name|ReindexRequest
name|request
parameter_list|()
block|{
return|return
operator|new
name|ReindexRequest
argument_list|(
operator|new
name|SearchRequest
argument_list|()
argument_list|,
operator|new
name|IndexRequest
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|action
specifier|protected
name|TransportReindexAction
operator|.
name|AsyncIndexBySearchAction
name|action
parameter_list|(
name|ScriptService
name|scriptService
parameter_list|,
name|ReindexRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|TransportReindexAction
operator|.
name|AsyncIndexBySearchAction
argument_list|(
name|task
argument_list|,
name|logger
argument_list|,
literal|null
argument_list|,
name|threadPool
argument_list|,
name|request
argument_list|,
name|scriptService
argument_list|,
literal|null
argument_list|,
name|listener
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

