begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search.type
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|type
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
name|ElasticSearchIllegalArgumentException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalStateException
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
name|SearchScrollRequest
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
name|SearchType
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
name|routing
operator|.
name|ShardRouting
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
name|Base64
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
name|Nullable
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
name|Unicode
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
name|search
operator|.
name|SearchPhaseResult
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
name|InternalScrollSearchRequest
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
name|ShardSearchRequest
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
name|Collection
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportSearchHelper
specifier|public
specifier|abstract
class|class
name|TransportSearchHelper
block|{
DECL|method|internalSearchRequest
specifier|public
specifier|static
name|ShardSearchRequest
name|internalSearchRequest
parameter_list|(
name|ShardRouting
name|shardRouting
parameter_list|,
name|int
name|numberOfShards
parameter_list|,
name|SearchRequest
name|request
parameter_list|,
name|String
index|[]
name|filteringAliases
parameter_list|,
name|long
name|nowInMillis
parameter_list|)
block|{
name|ShardSearchRequest
name|shardRequest
init|=
operator|new
name|ShardSearchRequest
argument_list|(
name|request
argument_list|,
name|shardRouting
argument_list|,
name|numberOfShards
argument_list|)
decl_stmt|;
name|shardRequest
operator|.
name|filteringAliases
argument_list|(
name|filteringAliases
argument_list|)
expr_stmt|;
name|shardRequest
operator|.
name|nowInMillis
argument_list|(
name|nowInMillis
argument_list|)
expr_stmt|;
return|return
name|shardRequest
return|;
block|}
DECL|method|internalScrollSearchRequest
specifier|public
specifier|static
name|InternalScrollSearchRequest
name|internalScrollSearchRequest
parameter_list|(
name|long
name|id
parameter_list|,
name|SearchScrollRequest
name|request
parameter_list|)
block|{
return|return
operator|new
name|InternalScrollSearchRequest
argument_list|(
name|request
argument_list|,
name|id
argument_list|)
return|;
block|}
DECL|method|buildScrollId
specifier|public
specifier|static
name|String
name|buildScrollId
parameter_list|(
name|SearchType
name|searchType
parameter_list|,
name|Collection
argument_list|<
name|?
extends|extends
name|SearchPhaseResult
argument_list|>
name|searchPhaseResults
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|searchType
operator|==
name|SearchType
operator|.
name|DFS_QUERY_THEN_FETCH
operator|||
name|searchType
operator|==
name|SearchType
operator|.
name|QUERY_THEN_FETCH
condition|)
block|{
return|return
name|buildScrollId
argument_list|(
name|ParsedScrollId
operator|.
name|QUERY_THEN_FETCH_TYPE
argument_list|,
name|searchPhaseResults
argument_list|,
name|attributes
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|searchType
operator|==
name|SearchType
operator|.
name|QUERY_AND_FETCH
operator|||
name|searchType
operator|==
name|SearchType
operator|.
name|DFS_QUERY_AND_FETCH
condition|)
block|{
return|return
name|buildScrollId
argument_list|(
name|ParsedScrollId
operator|.
name|QUERY_AND_FETCH_TYPE
argument_list|,
name|searchPhaseResults
argument_list|,
name|attributes
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|searchType
operator|==
name|SearchType
operator|.
name|SCAN
condition|)
block|{
return|return
name|buildScrollId
argument_list|(
name|ParsedScrollId
operator|.
name|SCAN
argument_list|,
name|searchPhaseResults
argument_list|,
name|attributes
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticSearchIllegalStateException
argument_list|()
throw|;
block|}
block|}
DECL|method|buildScrollId
specifier|public
specifier|static
name|String
name|buildScrollId
parameter_list|(
name|String
name|type
parameter_list|,
name|Collection
argument_list|<
name|?
extends|extends
name|SearchPhaseResult
argument_list|>
name|searchPhaseResults
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
parameter_list|)
throws|throws
name|IOException
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
operator|.
name|append
argument_list|(
name|type
argument_list|)
operator|.
name|append
argument_list|(
literal|';'
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|searchPhaseResults
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|';'
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchPhaseResult
name|searchPhaseResult
range|:
name|searchPhaseResults
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|searchPhaseResult
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
operator|.
name|append
argument_list|(
name|searchPhaseResult
operator|.
name|shardTarget
argument_list|()
operator|.
name|nodeId
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|';'
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|attributes
operator|==
literal|null
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"0;"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|attributes
operator|.
name|size
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|";"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|attributes
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|';'
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|Base64
operator|.
name|encodeBytes
argument_list|(
name|Unicode
operator|.
name|fromStringAsBytes
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
name|Base64
operator|.
name|URL_SAFE
argument_list|)
return|;
block|}
DECL|method|parseScrollId
specifier|public
specifier|static
name|ParsedScrollId
name|parseScrollId
parameter_list|(
name|String
name|scrollId
parameter_list|)
block|{
try|try
block|{
name|scrollId
operator|=
name|Unicode
operator|.
name|fromBytes
argument_list|(
name|Base64
operator|.
name|decode
argument_list|(
name|scrollId
argument_list|,
name|Base64
operator|.
name|URL_SAFE
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Failed to decode scrollId"
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|String
index|[]
name|elements
init|=
name|Strings
operator|.
name|splitStringToArray
argument_list|(
name|scrollId
argument_list|,
literal|';'
argument_list|)
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
name|String
name|type
init|=
name|elements
index|[
name|index
operator|++
index|]
decl_stmt|;
name|int
name|contextSize
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|elements
index|[
name|index
operator|++
index|]
argument_list|)
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
name|Tuple
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
index|[]
name|context
init|=
operator|new
name|Tuple
index|[
name|contextSize
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
name|contextSize
condition|;
name|i
operator|++
control|)
block|{
name|String
name|element
init|=
name|elements
index|[
name|index
operator|++
index|]
decl_stmt|;
name|int
name|sep
init|=
name|element
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
if|if
condition|(
name|sep
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Malformed scrollId ["
operator|+
name|scrollId
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|context
index|[
name|i
index|]
operator|=
operator|new
name|Tuple
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
argument_list|(
name|element
operator|.
name|substring
argument_list|(
name|sep
operator|+
literal|1
argument_list|)
argument_list|,
name|Long
operator|.
name|parseLong
argument_list|(
name|element
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sep
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|attributes
decl_stmt|;
name|int
name|attributesSize
init|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|elements
index|[
name|index
operator|++
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|attributesSize
operator|==
literal|0
condition|)
block|{
name|attributes
operator|=
name|ImmutableMap
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|attributes
operator|=
name|Maps
operator|.
name|newHashMapWithExpectedSize
argument_list|(
name|attributesSize
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
name|attributesSize
condition|;
name|i
operator|++
control|)
block|{
name|String
name|element
init|=
name|elements
index|[
name|index
operator|++
index|]
decl_stmt|;
name|int
name|sep
init|=
name|element
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
decl_stmt|;
name|attributes
operator|.
name|put
argument_list|(
name|element
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|sep
argument_list|)
argument_list|,
name|element
operator|.
name|substring
argument_list|(
name|sep
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|ParsedScrollId
argument_list|(
name|scrollId
argument_list|,
name|type
argument_list|,
name|context
argument_list|,
name|attributes
argument_list|)
return|;
block|}
DECL|method|TransportSearchHelper
specifier|private
name|TransportSearchHelper
parameter_list|()
block|{      }
block|}
end_class

end_unit

