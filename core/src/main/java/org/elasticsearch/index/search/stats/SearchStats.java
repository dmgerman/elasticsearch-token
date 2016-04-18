begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.stats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|stats
package|;
end_package

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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|common
operator|.
name|xcontent
operator|.
name|ToXContent
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
name|XContentBuilderString
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
name|XContentFactory
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|SearchStats
specifier|public
class|class
name|SearchStats
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|class|Stats
specifier|public
specifier|static
class|class
name|Stats
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|queryCount
specifier|private
name|long
name|queryCount
decl_stmt|;
DECL|field|queryTimeInMillis
specifier|private
name|long
name|queryTimeInMillis
decl_stmt|;
DECL|field|queryCurrent
specifier|private
name|long
name|queryCurrent
decl_stmt|;
DECL|field|fetchCount
specifier|private
name|long
name|fetchCount
decl_stmt|;
DECL|field|fetchTimeInMillis
specifier|private
name|long
name|fetchTimeInMillis
decl_stmt|;
DECL|field|fetchCurrent
specifier|private
name|long
name|fetchCurrent
decl_stmt|;
DECL|field|scrollCount
specifier|private
name|long
name|scrollCount
decl_stmt|;
DECL|field|scrollTimeInMillis
specifier|private
name|long
name|scrollTimeInMillis
decl_stmt|;
DECL|field|scrollCurrent
specifier|private
name|long
name|scrollCurrent
decl_stmt|;
DECL|field|suggestCount
specifier|private
name|long
name|suggestCount
decl_stmt|;
DECL|field|suggestTimeInMillis
specifier|private
name|long
name|suggestTimeInMillis
decl_stmt|;
DECL|field|suggestCurrent
specifier|private
name|long
name|suggestCurrent
decl_stmt|;
DECL|method|Stats
name|Stats
parameter_list|()
block|{          }
DECL|method|Stats
specifier|public
name|Stats
parameter_list|(
name|long
name|queryCount
parameter_list|,
name|long
name|queryTimeInMillis
parameter_list|,
name|long
name|queryCurrent
parameter_list|,
name|long
name|fetchCount
parameter_list|,
name|long
name|fetchTimeInMillis
parameter_list|,
name|long
name|fetchCurrent
parameter_list|,
name|long
name|scrollCount
parameter_list|,
name|long
name|scrollTimeInMillis
parameter_list|,
name|long
name|scrollCurrent
parameter_list|,
name|long
name|suggestCount
parameter_list|,
name|long
name|suggestTimeInMillis
parameter_list|,
name|long
name|suggestCurrent
parameter_list|)
block|{
name|this
operator|.
name|queryCount
operator|=
name|queryCount
expr_stmt|;
name|this
operator|.
name|queryTimeInMillis
operator|=
name|queryTimeInMillis
expr_stmt|;
name|this
operator|.
name|queryCurrent
operator|=
name|queryCurrent
expr_stmt|;
name|this
operator|.
name|fetchCount
operator|=
name|fetchCount
expr_stmt|;
name|this
operator|.
name|fetchTimeInMillis
operator|=
name|fetchTimeInMillis
expr_stmt|;
name|this
operator|.
name|fetchCurrent
operator|=
name|fetchCurrent
expr_stmt|;
name|this
operator|.
name|scrollCount
operator|=
name|scrollCount
expr_stmt|;
name|this
operator|.
name|scrollTimeInMillis
operator|=
name|scrollTimeInMillis
expr_stmt|;
name|this
operator|.
name|scrollCurrent
operator|=
name|scrollCurrent
expr_stmt|;
name|this
operator|.
name|suggestCount
operator|=
name|suggestCount
expr_stmt|;
name|this
operator|.
name|suggestTimeInMillis
operator|=
name|suggestTimeInMillis
expr_stmt|;
name|this
operator|.
name|suggestCurrent
operator|=
name|suggestCurrent
expr_stmt|;
block|}
DECL|method|Stats
specifier|public
name|Stats
parameter_list|(
name|Stats
name|stats
parameter_list|)
block|{
name|this
argument_list|(
name|stats
operator|.
name|queryCount
argument_list|,
name|stats
operator|.
name|queryTimeInMillis
argument_list|,
name|stats
operator|.
name|queryCurrent
argument_list|,
name|stats
operator|.
name|fetchCount
argument_list|,
name|stats
operator|.
name|fetchTimeInMillis
argument_list|,
name|stats
operator|.
name|fetchCurrent
argument_list|,
name|stats
operator|.
name|scrollCount
argument_list|,
name|stats
operator|.
name|scrollTimeInMillis
argument_list|,
name|stats
operator|.
name|scrollCurrent
argument_list|,
name|stats
operator|.
name|suggestCount
argument_list|,
name|stats
operator|.
name|suggestTimeInMillis
argument_list|,
name|stats
operator|.
name|suggestCurrent
argument_list|)
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|Stats
name|stats
parameter_list|)
block|{
name|queryCount
operator|+=
name|stats
operator|.
name|queryCount
expr_stmt|;
name|queryTimeInMillis
operator|+=
name|stats
operator|.
name|queryTimeInMillis
expr_stmt|;
name|queryCurrent
operator|+=
name|stats
operator|.
name|queryCurrent
expr_stmt|;
name|fetchCount
operator|+=
name|stats
operator|.
name|fetchCount
expr_stmt|;
name|fetchTimeInMillis
operator|+=
name|stats
operator|.
name|fetchTimeInMillis
expr_stmt|;
name|fetchCurrent
operator|+=
name|stats
operator|.
name|fetchCurrent
expr_stmt|;
name|scrollCount
operator|+=
name|stats
operator|.
name|scrollCount
expr_stmt|;
name|scrollTimeInMillis
operator|+=
name|stats
operator|.
name|scrollTimeInMillis
expr_stmt|;
name|scrollCurrent
operator|+=
name|stats
operator|.
name|scrollCurrent
expr_stmt|;
name|suggestCount
operator|+=
name|stats
operator|.
name|suggestCount
expr_stmt|;
name|suggestTimeInMillis
operator|+=
name|stats
operator|.
name|suggestTimeInMillis
expr_stmt|;
name|suggestCurrent
operator|+=
name|stats
operator|.
name|suggestCurrent
expr_stmt|;
block|}
DECL|method|getQueryCount
specifier|public
name|long
name|getQueryCount
parameter_list|()
block|{
return|return
name|queryCount
return|;
block|}
DECL|method|getQueryTime
specifier|public
name|TimeValue
name|getQueryTime
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|queryTimeInMillis
argument_list|)
return|;
block|}
DECL|method|getQueryTimeInMillis
specifier|public
name|long
name|getQueryTimeInMillis
parameter_list|()
block|{
return|return
name|queryTimeInMillis
return|;
block|}
DECL|method|getQueryCurrent
specifier|public
name|long
name|getQueryCurrent
parameter_list|()
block|{
return|return
name|queryCurrent
return|;
block|}
DECL|method|getFetchCount
specifier|public
name|long
name|getFetchCount
parameter_list|()
block|{
return|return
name|fetchCount
return|;
block|}
DECL|method|getFetchTime
specifier|public
name|TimeValue
name|getFetchTime
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|fetchTimeInMillis
argument_list|)
return|;
block|}
DECL|method|getFetchTimeInMillis
specifier|public
name|long
name|getFetchTimeInMillis
parameter_list|()
block|{
return|return
name|fetchTimeInMillis
return|;
block|}
DECL|method|getFetchCurrent
specifier|public
name|long
name|getFetchCurrent
parameter_list|()
block|{
return|return
name|fetchCurrent
return|;
block|}
DECL|method|getScrollCount
specifier|public
name|long
name|getScrollCount
parameter_list|()
block|{
return|return
name|scrollCount
return|;
block|}
DECL|method|getScrollTime
specifier|public
name|TimeValue
name|getScrollTime
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|scrollTimeInMillis
argument_list|)
return|;
block|}
DECL|method|getScrollTimeInMillis
specifier|public
name|long
name|getScrollTimeInMillis
parameter_list|()
block|{
return|return
name|scrollTimeInMillis
return|;
block|}
DECL|method|getScrollCurrent
specifier|public
name|long
name|getScrollCurrent
parameter_list|()
block|{
return|return
name|scrollCurrent
return|;
block|}
DECL|method|getSuggestCount
specifier|public
name|long
name|getSuggestCount
parameter_list|()
block|{
return|return
name|suggestCount
return|;
block|}
DECL|method|getSuggestTimeInMillis
specifier|public
name|long
name|getSuggestTimeInMillis
parameter_list|()
block|{
return|return
name|suggestTimeInMillis
return|;
block|}
DECL|method|getSuggestTime
specifier|public
name|TimeValue
name|getSuggestTime
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|suggestTimeInMillis
argument_list|)
return|;
block|}
DECL|method|getSuggestCurrent
specifier|public
name|long
name|getSuggestCurrent
parameter_list|()
block|{
return|return
name|suggestCurrent
return|;
block|}
DECL|method|readStats
specifier|public
specifier|static
name|Stats
name|readStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Stats
name|stats
init|=
operator|new
name|Stats
argument_list|()
decl_stmt|;
name|stats
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|stats
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|queryCount
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|queryTimeInMillis
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|queryCurrent
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|fetchCount
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|fetchTimeInMillis
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|fetchCurrent
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|scrollCount
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|scrollTimeInMillis
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|scrollCurrent
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|suggestCount
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|suggestTimeInMillis
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|suggestCurrent
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVLong
argument_list|(
name|queryCount
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|queryTimeInMillis
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|queryCurrent
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|fetchCount
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|fetchTimeInMillis
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|fetchCurrent
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|scrollCount
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|scrollTimeInMillis
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|scrollCurrent
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|suggestCount
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|suggestTimeInMillis
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|suggestCurrent
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|QUERY_TOTAL
argument_list|,
name|queryCount
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|Fields
operator|.
name|QUERY_TIME_IN_MILLIS
argument_list|,
name|Fields
operator|.
name|QUERY_TIME
argument_list|,
name|queryTimeInMillis
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|QUERY_CURRENT
argument_list|,
name|queryCurrent
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|FETCH_TOTAL
argument_list|,
name|fetchCount
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|Fields
operator|.
name|FETCH_TIME_IN_MILLIS
argument_list|,
name|Fields
operator|.
name|FETCH_TIME
argument_list|,
name|fetchTimeInMillis
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|FETCH_CURRENT
argument_list|,
name|fetchCurrent
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SCROLL_TOTAL
argument_list|,
name|scrollCount
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|Fields
operator|.
name|SCROLL_TIME_IN_MILLIS
argument_list|,
name|Fields
operator|.
name|SCROLL_TIME
argument_list|,
name|scrollTimeInMillis
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SCROLL_CURRENT
argument_list|,
name|scrollCurrent
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SUGGEST_TOTAL
argument_list|,
name|suggestCount
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|Fields
operator|.
name|SUGGEST_TIME_IN_MILLIS
argument_list|,
name|Fields
operator|.
name|SUGGEST_TIME
argument_list|,
name|suggestTimeInMillis
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|SUGGEST_CURRENT
argument_list|,
name|suggestCurrent
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
DECL|field|totalStats
name|Stats
name|totalStats
decl_stmt|;
DECL|field|openContexts
name|long
name|openContexts
decl_stmt|;
annotation|@
name|Nullable
DECL|field|groupStats
name|Map
argument_list|<
name|String
argument_list|,
name|Stats
argument_list|>
name|groupStats
decl_stmt|;
DECL|method|SearchStats
specifier|public
name|SearchStats
parameter_list|()
block|{
name|totalStats
operator|=
operator|new
name|Stats
argument_list|()
expr_stmt|;
block|}
DECL|method|SearchStats
specifier|public
name|SearchStats
parameter_list|(
name|Stats
name|totalStats
parameter_list|,
name|long
name|openContexts
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Stats
argument_list|>
name|groupStats
parameter_list|)
block|{
name|this
operator|.
name|totalStats
operator|=
name|totalStats
expr_stmt|;
name|this
operator|.
name|openContexts
operator|=
name|openContexts
expr_stmt|;
name|this
operator|.
name|groupStats
operator|=
name|groupStats
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|SearchStats
name|searchStats
parameter_list|)
block|{
name|add
argument_list|(
name|searchStats
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|SearchStats
name|searchStats
parameter_list|,
name|boolean
name|includeTypes
parameter_list|)
block|{
if|if
condition|(
name|searchStats
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|addTotals
argument_list|(
name|searchStats
argument_list|)
expr_stmt|;
name|openContexts
operator|+=
name|searchStats
operator|.
name|openContexts
expr_stmt|;
if|if
condition|(
name|includeTypes
operator|&&
name|searchStats
operator|.
name|groupStats
operator|!=
literal|null
operator|&&
operator|!
name|searchStats
operator|.
name|groupStats
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|groupStats
operator|==
literal|null
condition|)
block|{
name|groupStats
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|searchStats
operator|.
name|groupStats
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Stats
argument_list|>
name|entry
range|:
name|searchStats
operator|.
name|groupStats
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Stats
name|stats
init|=
name|groupStats
operator|.
name|get
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|stats
operator|==
literal|null
condition|)
block|{
name|groupStats
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
operator|new
name|Stats
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|stats
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|addTotals
specifier|public
name|void
name|addTotals
parameter_list|(
name|SearchStats
name|searchStats
parameter_list|)
block|{
if|if
condition|(
name|searchStats
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|totalStats
operator|.
name|add
argument_list|(
name|searchStats
operator|.
name|totalStats
argument_list|)
expr_stmt|;
block|}
DECL|method|getTotal
specifier|public
name|Stats
name|getTotal
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalStats
return|;
block|}
DECL|method|getOpenContexts
specifier|public
name|long
name|getOpenContexts
parameter_list|()
block|{
return|return
name|this
operator|.
name|openContexts
return|;
block|}
annotation|@
name|Nullable
DECL|method|getGroupStats
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Stats
argument_list|>
name|getGroupStats
parameter_list|()
block|{
return|return
name|this
operator|.
name|groupStats
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|ToXContent
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|SEARCH
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|OPEN_CONTEXTS
argument_list|,
name|openContexts
argument_list|)
expr_stmt|;
name|totalStats
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
if|if
condition|(
name|groupStats
operator|!=
literal|null
operator|&&
operator|!
name|groupStats
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|GROUPS
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
name|Stats
argument_list|>
name|entry
range|:
name|groupStats
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|SEARCH
specifier|static
specifier|final
name|XContentBuilderString
name|SEARCH
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"search"
argument_list|)
decl_stmt|;
DECL|field|OPEN_CONTEXTS
specifier|static
specifier|final
name|XContentBuilderString
name|OPEN_CONTEXTS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"open_contexts"
argument_list|)
decl_stmt|;
DECL|field|GROUPS
specifier|static
specifier|final
name|XContentBuilderString
name|GROUPS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"groups"
argument_list|)
decl_stmt|;
DECL|field|QUERY_TOTAL
specifier|static
specifier|final
name|XContentBuilderString
name|QUERY_TOTAL
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"query_total"
argument_list|)
decl_stmt|;
DECL|field|QUERY_TIME
specifier|static
specifier|final
name|XContentBuilderString
name|QUERY_TIME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"query_time"
argument_list|)
decl_stmt|;
DECL|field|QUERY_TIME_IN_MILLIS
specifier|static
specifier|final
name|XContentBuilderString
name|QUERY_TIME_IN_MILLIS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"query_time_in_millis"
argument_list|)
decl_stmt|;
DECL|field|QUERY_CURRENT
specifier|static
specifier|final
name|XContentBuilderString
name|QUERY_CURRENT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"query_current"
argument_list|)
decl_stmt|;
DECL|field|FETCH_TOTAL
specifier|static
specifier|final
name|XContentBuilderString
name|FETCH_TOTAL
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"fetch_total"
argument_list|)
decl_stmt|;
DECL|field|FETCH_TIME
specifier|static
specifier|final
name|XContentBuilderString
name|FETCH_TIME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"fetch_time"
argument_list|)
decl_stmt|;
DECL|field|FETCH_TIME_IN_MILLIS
specifier|static
specifier|final
name|XContentBuilderString
name|FETCH_TIME_IN_MILLIS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"fetch_time_in_millis"
argument_list|)
decl_stmt|;
DECL|field|FETCH_CURRENT
specifier|static
specifier|final
name|XContentBuilderString
name|FETCH_CURRENT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"fetch_current"
argument_list|)
decl_stmt|;
DECL|field|SCROLL_TOTAL
specifier|static
specifier|final
name|XContentBuilderString
name|SCROLL_TOTAL
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"scroll_total"
argument_list|)
decl_stmt|;
DECL|field|SCROLL_TIME
specifier|static
specifier|final
name|XContentBuilderString
name|SCROLL_TIME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"scroll_time"
argument_list|)
decl_stmt|;
DECL|field|SCROLL_TIME_IN_MILLIS
specifier|static
specifier|final
name|XContentBuilderString
name|SCROLL_TIME_IN_MILLIS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"scroll_time_in_millis"
argument_list|)
decl_stmt|;
DECL|field|SCROLL_CURRENT
specifier|static
specifier|final
name|XContentBuilderString
name|SCROLL_CURRENT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"scroll_current"
argument_list|)
decl_stmt|;
DECL|field|SUGGEST_TOTAL
specifier|static
specifier|final
name|XContentBuilderString
name|SUGGEST_TOTAL
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"suggest_total"
argument_list|)
decl_stmt|;
DECL|field|SUGGEST_TIME
specifier|static
specifier|final
name|XContentBuilderString
name|SUGGEST_TIME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"suggest_time"
argument_list|)
decl_stmt|;
DECL|field|SUGGEST_TIME_IN_MILLIS
specifier|static
specifier|final
name|XContentBuilderString
name|SUGGEST_TIME_IN_MILLIS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"suggest_time_in_millis"
argument_list|)
decl_stmt|;
DECL|field|SUGGEST_CURRENT
specifier|static
specifier|final
name|XContentBuilderString
name|SUGGEST_CURRENT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"suggest_current"
argument_list|)
decl_stmt|;
block|}
DECL|method|readSearchStats
specifier|public
specifier|static
name|SearchStats
name|readSearchStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|SearchStats
name|searchStats
init|=
operator|new
name|SearchStats
argument_list|()
decl_stmt|;
name|searchStats
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|searchStats
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|totalStats
operator|=
name|Stats
operator|.
name|readStats
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|openContexts
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|groupStats
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|size
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|groupStats
operator|.
name|put
argument_list|(
name|in
operator|.
name|readString
argument_list|()
argument_list|,
name|Stats
operator|.
name|readStats
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|totalStats
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|openContexts
argument_list|)
expr_stmt|;
if|if
condition|(
name|groupStats
operator|==
literal|null
operator|||
name|groupStats
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|groupStats
operator|.
name|size
argument_list|()
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
name|Stats
argument_list|>
name|entry
range|:
name|groupStats
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|toXContent
argument_list|(
name|builder
argument_list|,
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
operator|.
name|string
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|"{ \"error\" : \""
operator|+
name|e
operator|.
name|getMessage
argument_list|()
operator|+
literal|"\"}"
return|;
block|}
block|}
block|}
end_class

end_unit

