begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.benchmark.aliases
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|benchmark
operator|.
name|aliases
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
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|IndicesAliasesRequestBuilder
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
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|get
operator|.
name|GetAliasesResponse
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
name|cluster
operator|.
name|metadata
operator|.
name|AliasMetaData
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
name|Strings
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
name|indices
operator|.
name|IndexAlreadyExistsException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeBuilder
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
name|List
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|AliasesBenchmark
specifier|public
class|class
name|AliasesBenchmark
block|{
DECL|field|INDEX_NAME
specifier|private
specifier|final
specifier|static
name|String
name|INDEX_NAME
init|=
literal|"my-index"
decl_stmt|;
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|NUM_ADDITIONAL_NODES
init|=
literal|0
decl_stmt|;
name|int
name|BASE_ALIAS_COUNT
init|=
literal|100000
decl_stmt|;
name|int
name|NUM_ADD_ALIAS_REQUEST
init|=
literal|1000
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"node.master"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Node
name|node1
init|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|settings
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.master"
argument_list|,
literal|true
argument_list|)
argument_list|)
operator|.
name|node
argument_list|()
decl_stmt|;
name|Node
index|[]
name|otherNodes
init|=
operator|new
name|Node
index|[
name|NUM_ADDITIONAL_NODES
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
name|otherNodes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|otherNodes
index|[
name|i
index|]
operator|=
name|NodeBuilder
operator|.
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|node
argument_list|()
expr_stmt|;
block|}
name|Client
name|client
init|=
name|node1
operator|.
name|client
argument_list|()
decl_stmt|;
try|try
block|{
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
name|INDEX_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexAlreadyExistsException
name|e
parameter_list|)
block|{}
name|client
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
name|setWaitForYellowStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|int
name|numberOfAliases
init|=
name|countAliases
argument_list|(
name|client
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Number of aliases: "
operator|+
name|numberOfAliases
argument_list|)
expr_stmt|;
if|if
condition|(
name|numberOfAliases
operator|<
name|BASE_ALIAS_COUNT
condition|)
block|{
name|int
name|diff
init|=
name|BASE_ALIAS_COUNT
operator|-
name|numberOfAliases
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Adding "
operator|+
name|diff
operator|+
literal|" more aliases to get to the start amount of "
operator|+
name|BASE_ALIAS_COUNT
operator|+
literal|" aliases"
argument_list|)
expr_stmt|;
name|IndicesAliasesRequestBuilder
name|builder
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareAliases
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
name|diff
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|addAlias
argument_list|(
name|INDEX_NAME
argument_list|,
name|Strings
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|1000
operator|==
literal|0
condition|)
block|{
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|builder
operator|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareAliases
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|builder
operator|.
name|request
argument_list|()
operator|.
name|getAliasActions
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|numberOfAliases
operator|>
name|BASE_ALIAS_COUNT
condition|)
block|{
name|IndicesAliasesRequestBuilder
name|builder
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareAliases
argument_list|()
decl_stmt|;
name|int
name|diff
init|=
name|numberOfAliases
operator|-
name|BASE_ALIAS_COUNT
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Removing "
operator|+
name|diff
operator|+
literal|" aliases to get to the start amount of "
operator|+
name|BASE_ALIAS_COUNT
operator|+
literal|" aliases"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|AliasMetaData
argument_list|>
name|aliases
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareGetAliases
argument_list|(
literal|"*"
argument_list|)
operator|.
name|addIndices
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|getAliases
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
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
operator|<=
name|diff
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|removeAlias
argument_list|(
name|INDEX_NAME
argument_list|,
name|aliases
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|alias
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|i
operator|%
literal|1000
operator|==
literal|0
condition|)
block|{
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|builder
operator|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareAliases
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|builder
operator|.
name|request
argument_list|()
operator|.
name|getAliasActions
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
block|}
name|numberOfAliases
operator|=
name|countAliases
argument_list|(
name|client
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Number of aliases: "
operator|+
name|numberOfAliases
argument_list|)
expr_stmt|;
name|long
name|totalTime
init|=
literal|0
decl_stmt|;
name|int
name|max
init|=
name|numberOfAliases
operator|+
name|NUM_ADD_ALIAS_REQUEST
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|numberOfAliases
init|;
name|i
operator|<=
name|max
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|!=
name|numberOfAliases
operator|&&
name|i
operator|%
literal|100
operator|==
literal|0
condition|)
block|{
name|long
name|avgTime
init|=
name|totalTime
operator|/
literal|100
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Added ["
operator|+
operator|(
name|i
operator|-
name|numberOfAliases
operator|)
operator|+
literal|"] aliases. Avg create time: "
operator|+
name|avgTime
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|totalTime
operator|=
literal|0
expr_stmt|;
block|}
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
comment|//            String filter = termFilter("field" + i, "value" + i).toXContent(XContentFactory.jsonBuilder(), null).string();
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareAliases
argument_list|()
operator|.
name|addAlias
argument_list|(
name|INDEX_NAME
argument_list|,
name|Strings
operator|.
name|randomBase64UUID
argument_list|()
comment|/*, filter*/
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|totalTime
operator|+=
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|time
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Number of aliases: "
operator|+
name|countAliases
argument_list|(
name|client
argument_list|)
argument_list|)
expr_stmt|;
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|node1
operator|.
name|close
argument_list|()
expr_stmt|;
for|for
control|(
name|Node
name|otherNode
range|:
name|otherNodes
control|)
block|{
name|otherNode
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|countAliases
specifier|private
specifier|static
name|int
name|countAliases
parameter_list|(
name|Client
name|client
parameter_list|)
block|{
name|GetAliasesResponse
name|response
init|=
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareGetAliases
argument_list|(
literal|"*"
argument_list|)
operator|.
name|addIndices
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|getAliases
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|0
return|;
block|}
else|else
block|{
return|return
name|response
operator|.
name|getAliases
argument_list|()
operator|.
name|get
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|size
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

