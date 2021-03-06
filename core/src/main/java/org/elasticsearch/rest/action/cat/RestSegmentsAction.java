begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.cat
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|cat
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
name|cluster
operator|.
name|state
operator|.
name|ClusterStateRequest
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
name|cluster
operator|.
name|state
operator|.
name|ClusterStateResponse
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
name|segments
operator|.
name|IndexSegments
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
name|segments
operator|.
name|IndexShardSegments
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
name|segments
operator|.
name|IndicesSegmentResponse
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
name|segments
operator|.
name|IndicesSegmentsRequest
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
name|segments
operator|.
name|ShardSegments
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
name|node
operator|.
name|NodeClient
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
name|node
operator|.
name|DiscoveryNodes
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
name|Table
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
name|index
operator|.
name|engine
operator|.
name|Segment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestController
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestRequest
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|RestActionListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|RestResponseListener
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
name|rest
operator|.
name|RestRequest
operator|.
name|Method
operator|.
name|GET
import|;
end_import

begin_class
DECL|class|RestSegmentsAction
specifier|public
class|class
name|RestSegmentsAction
extends|extends
name|AbstractCatAction
block|{
DECL|method|RestSegmentsAction
specifier|public
name|RestSegmentsAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|RestController
name|controller
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_cat/segments"
argument_list|,
name|this
argument_list|)
expr_stmt|;
name|controller
operator|.
name|registerHandler
argument_list|(
name|GET
argument_list|,
literal|"/_cat/segments/{index}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
literal|"cat_segments_action"
return|;
block|}
annotation|@
name|Override
DECL|method|doCatRequest
specifier|protected
name|RestChannelConsumer
name|doCatRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
block|{
specifier|final
name|String
index|[]
name|indices
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index"
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|ClusterStateRequest
name|clusterStateRequest
init|=
operator|new
name|ClusterStateRequest
argument_list|()
decl_stmt|;
name|clusterStateRequest
operator|.
name|local
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"local"
argument_list|,
name|clusterStateRequest
operator|.
name|local
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|masterNodeTimeout
argument_list|(
name|request
operator|.
name|paramAsTime
argument_list|(
literal|"master_timeout"
argument_list|,
name|clusterStateRequest
operator|.
name|masterNodeTimeout
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateRequest
operator|.
name|clear
argument_list|()
operator|.
name|nodes
argument_list|(
literal|true
argument_list|)
operator|.
name|routingTable
argument_list|(
literal|true
argument_list|)
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
return|return
name|channel
lambda|->
name|client
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|state
argument_list|(
name|clusterStateRequest
argument_list|,
operator|new
name|RestActionListener
argument_list|<
name|ClusterStateResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
block|@Override             public void processResponse(final ClusterStateResponse clusterStateResponse
block|)
block|{
specifier|final
name|IndicesSegmentsRequest
name|indicesSegmentsRequest
init|=
operator|new
name|IndicesSegmentsRequest
argument_list|()
decl_stmt|;
name|indicesSegmentsRequest
operator|.
name|indices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|segments
argument_list|(
name|indicesSegmentsRequest
argument_list|,
operator|new
name|RestResponseListener
argument_list|<
name|IndicesSegmentResponse
argument_list|>
argument_list|(
name|channel
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|RestResponse
name|buildResponse
parameter_list|(
specifier|final
name|IndicesSegmentResponse
name|indicesSegmentResponse
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|IndexSegments
argument_list|>
name|indicesSegments
init|=
name|indicesSegmentResponse
operator|.
name|getIndices
argument_list|()
decl_stmt|;
name|Table
name|tab
init|=
name|buildTable
argument_list|(
name|request
argument_list|,
name|clusterStateResponse
argument_list|,
name|indicesSegments
argument_list|)
decl_stmt|;
return|return
name|RestTable
operator|.
name|buildResponse
argument_list|(
name|tab
argument_list|,
name|channel
argument_list|)
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_function
unit|}      @
name|Override
DECL|method|documentation
specifier|protected
name|void
name|documentation
parameter_list|(
name|StringBuilder
name|sb
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"/_cat/segments\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"/_cat/segments/{index}\n"
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Override
DECL|method|getTableWithHeader
specifier|protected
name|Table
name|getTableWithHeader
parameter_list|(
name|RestRequest
name|request
parameter_list|)
block|{
name|Table
name|table
init|=
operator|new
name|Table
argument_list|()
decl_stmt|;
name|table
operator|.
name|startHeaders
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"index"
argument_list|,
literal|"default:true;alias:i,idx;desc:index name"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"shard"
argument_list|,
literal|"default:true;alias:s,sh;desc:shard name"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"prirep"
argument_list|,
literal|"alias:p,pr,primaryOrReplica;default:true;desc:primary or replica"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"ip"
argument_list|,
literal|"default:true;desc:ip of node where it lives"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"id"
argument_list|,
literal|"default:false;desc:unique id of node where it lives"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"segment"
argument_list|,
literal|"default:true;alias:seg;desc:segment name"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"generation"
argument_list|,
literal|"default:true;alias:g,gen;text-align:right;desc:segment generation"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"docs.count"
argument_list|,
literal|"default:true;alias:dc,docsCount;text-align:right;desc:number of docs in segment"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"docs.deleted"
argument_list|,
literal|"default:true;alias:dd,docsDeleted;text-align:right;desc:number of deleted docs in segment"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"size"
argument_list|,
literal|"default:true;alias:si;text-align:right;desc:segment size in bytes"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"size.memory"
argument_list|,
literal|"default:true;alias:sm,sizeMemory;text-align:right;desc:segment memory in bytes"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"committed"
argument_list|,
literal|"default:true;alias:ic,isCommitted;desc:is segment committed"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"searchable"
argument_list|,
literal|"default:true;alias:is,isSearchable;desc:is segment searched"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"version"
argument_list|,
literal|"default:true;alias:v,ver;desc:version"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
literal|"compound"
argument_list|,
literal|"default:true;alias:ico,isCompound;desc:is segment compound"
argument_list|)
expr_stmt|;
name|table
operator|.
name|endHeaders
argument_list|()
expr_stmt|;
return|return
name|table
return|;
block|}
end_function

begin_function
DECL|method|buildTable
specifier|private
name|Table
name|buildTable
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
name|ClusterStateResponse
name|state
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|IndexSegments
argument_list|>
name|indicesSegments
parameter_list|)
block|{
name|Table
name|table
init|=
name|getTableWithHeader
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|DiscoveryNodes
name|nodes
init|=
name|state
operator|.
name|getState
argument_list|()
operator|.
name|nodes
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexSegments
name|indexSegments
range|:
name|indicesSegments
operator|.
name|values
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|Integer
argument_list|,
name|IndexShardSegments
argument_list|>
name|shards
init|=
name|indexSegments
operator|.
name|getShards
argument_list|()
decl_stmt|;
for|for
control|(
name|IndexShardSegments
name|indexShardSegments
range|:
name|shards
operator|.
name|values
argument_list|()
control|)
block|{
name|ShardSegments
index|[]
name|shardSegments
init|=
name|indexShardSegments
operator|.
name|getShards
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardSegments
name|shardSegment
range|:
name|shardSegments
control|)
block|{
name|List
argument_list|<
name|Segment
argument_list|>
name|segments
init|=
name|shardSegment
operator|.
name|getSegments
argument_list|()
decl_stmt|;
for|for
control|(
name|Segment
name|segment
range|:
name|segments
control|)
block|{
name|table
operator|.
name|startRow
argument_list|()
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|shardSegment
operator|.
name|getShardRouting
argument_list|()
operator|.
name|getIndexName
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|shardSegment
operator|.
name|getShardRouting
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|shardSegment
operator|.
name|getShardRouting
argument_list|()
operator|.
name|primary
argument_list|()
condition|?
literal|"p"
else|:
literal|"r"
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|nodes
operator|.
name|get
argument_list|(
name|shardSegment
operator|.
name|getShardRouting
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
operator|.
name|getHostAddress
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|shardSegment
operator|.
name|getShardRouting
argument_list|()
operator|.
name|currentNodeId
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|segment
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|segment
operator|.
name|getGeneration
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|segment
operator|.
name|getNumDocs
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|segment
operator|.
name|getDeletedDocs
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|segment
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|segment
operator|.
name|getMemoryInBytes
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|segment
operator|.
name|isCommitted
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|segment
operator|.
name|isSearch
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|segment
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|addCell
argument_list|(
name|segment
operator|.
name|isCompound
argument_list|()
argument_list|)
expr_stmt|;
name|table
operator|.
name|endRow
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|table
return|;
block|}
end_function

unit|}
end_unit

