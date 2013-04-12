begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.stats
package|package
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
name|stats
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
name|stats
operator|.
name|CommonStatsFlags
operator|.
name|Flag
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
name|support
operator|.
name|broadcast
operator|.
name|BroadcastOperationRequest
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * A request to get indices level stats. Allow to enable different stats to be returned.  *<p/>  *<p>By default, the {@link #docs(boolean)}, {@link #store(boolean)}, {@link #indexing(boolean)}  * are enabled. Other stats can be enabled as well.  *<p/>  *<p>All the stats to be returned can be cleared using {@link #clear()}, at which point, specific  * stats can be enabled.  */
end_comment

begin_class
DECL|class|IndicesStatsRequest
specifier|public
class|class
name|IndicesStatsRequest
extends|extends
name|BroadcastOperationRequest
argument_list|<
name|IndicesStatsRequest
argument_list|>
block|{
DECL|field|flags
specifier|private
name|CommonStatsFlags
name|flags
init|=
operator|new
name|CommonStatsFlags
argument_list|()
decl_stmt|;
comment|/**      * Sets all flags to return all stats.      */
DECL|method|all
specifier|public
name|IndicesStatsRequest
name|all
parameter_list|()
block|{
name|flags
operator|.
name|all
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Clears all stats.      */
DECL|method|clear
specifier|public
name|IndicesStatsRequest
name|clear
parameter_list|()
block|{
name|flags
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Document types to return stats for. Mainly affects {@link #indexing(boolean)} when      * enabled, returning specific indexing stats for those types.      */
DECL|method|types
specifier|public
name|IndicesStatsRequest
name|types
parameter_list|(
name|String
modifier|...
name|types
parameter_list|)
block|{
name|flags
operator|.
name|types
argument_list|(
name|types
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Document types to return stats for. Mainly affects {@link #indexing(boolean)} when      * enabled, returning specific indexing stats for those types.      */
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
name|this
operator|.
name|flags
operator|.
name|types
argument_list|()
return|;
block|}
comment|/**      * Sets specific search group stats to retrieve the stats for. Mainly affects search      * when enabled.      */
DECL|method|groups
specifier|public
name|IndicesStatsRequest
name|groups
parameter_list|(
name|String
modifier|...
name|groups
parameter_list|)
block|{
name|flags
operator|.
name|groups
argument_list|(
name|groups
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|groups
specifier|public
name|String
index|[]
name|groups
parameter_list|()
block|{
return|return
name|this
operator|.
name|flags
operator|.
name|groups
argument_list|()
return|;
block|}
DECL|method|docs
specifier|public
name|IndicesStatsRequest
name|docs
parameter_list|(
name|boolean
name|docs
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|Docs
argument_list|,
name|docs
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|docs
specifier|public
name|boolean
name|docs
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Docs
argument_list|)
return|;
block|}
DECL|method|store
specifier|public
name|IndicesStatsRequest
name|store
parameter_list|(
name|boolean
name|store
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|Store
argument_list|,
name|store
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|store
specifier|public
name|boolean
name|store
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Store
argument_list|)
return|;
block|}
DECL|method|indexing
specifier|public
name|IndicesStatsRequest
name|indexing
parameter_list|(
name|boolean
name|indexing
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|Indexing
argument_list|,
name|indexing
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|indexing
specifier|public
name|boolean
name|indexing
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Indexing
argument_list|)
return|;
block|}
DECL|method|get
specifier|public
name|IndicesStatsRequest
name|get
parameter_list|(
name|boolean
name|get
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|Get
argument_list|,
name|get
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|get
specifier|public
name|boolean
name|get
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Get
argument_list|)
return|;
block|}
DECL|method|search
specifier|public
name|IndicesStatsRequest
name|search
parameter_list|(
name|boolean
name|search
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|Search
argument_list|,
name|search
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|search
specifier|public
name|boolean
name|search
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Search
argument_list|)
return|;
block|}
DECL|method|merge
specifier|public
name|IndicesStatsRequest
name|merge
parameter_list|(
name|boolean
name|merge
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|Merge
argument_list|,
name|merge
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|merge
specifier|public
name|boolean
name|merge
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Merge
argument_list|)
return|;
block|}
DECL|method|refresh
specifier|public
name|IndicesStatsRequest
name|refresh
parameter_list|(
name|boolean
name|refresh
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|Refresh
argument_list|,
name|refresh
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|refresh
specifier|public
name|boolean
name|refresh
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Refresh
argument_list|)
return|;
block|}
DECL|method|flush
specifier|public
name|IndicesStatsRequest
name|flush
parameter_list|(
name|boolean
name|flush
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|Flush
argument_list|,
name|flush
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|flush
specifier|public
name|boolean
name|flush
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Flush
argument_list|)
return|;
block|}
DECL|method|warmer
specifier|public
name|IndicesStatsRequest
name|warmer
parameter_list|(
name|boolean
name|warmer
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|Warmer
argument_list|,
name|warmer
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|warmer
specifier|public
name|boolean
name|warmer
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Warmer
argument_list|)
return|;
block|}
DECL|method|filterCache
specifier|public
name|IndicesStatsRequest
name|filterCache
parameter_list|(
name|boolean
name|filterCache
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|FilterCache
argument_list|,
name|filterCache
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|filterCache
specifier|public
name|boolean
name|filterCache
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|FilterCache
argument_list|)
return|;
block|}
DECL|method|idCache
specifier|public
name|IndicesStatsRequest
name|idCache
parameter_list|(
name|boolean
name|idCache
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|IdCache
argument_list|,
name|idCache
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|idCache
specifier|public
name|boolean
name|idCache
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|IdCache
argument_list|)
return|;
block|}
DECL|method|fieldData
specifier|public
name|IndicesStatsRequest
name|fieldData
parameter_list|(
name|boolean
name|fieldData
parameter_list|)
block|{
name|flags
operator|.
name|set
argument_list|(
name|Flag
operator|.
name|FieldData
argument_list|,
name|fieldData
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fieldData
specifier|public
name|boolean
name|fieldData
parameter_list|()
block|{
return|return
name|flags
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|FieldData
argument_list|)
return|;
block|}
DECL|method|fieldDataFields
specifier|public
name|IndicesStatsRequest
name|fieldDataFields
parameter_list|(
name|String
modifier|...
name|fieldDataFields
parameter_list|)
block|{
name|flags
operator|.
name|fieldDataFields
argument_list|(
name|fieldDataFields
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|fieldDataFields
specifier|public
name|String
index|[]
name|fieldDataFields
parameter_list|()
block|{
return|return
name|flags
operator|.
name|fieldDataFields
argument_list|()
return|;
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|flags
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|flags
operator|=
name|CommonStatsFlags
operator|.
name|readCommonStatsFlags
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

