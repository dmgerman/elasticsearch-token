begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.rest.action.admin.cluster
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|admin
operator|.
name|cluster
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
name|node
operator|.
name|stats
operator|.
name|NodesStatsRequest
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
name|stats
operator|.
name|CommonStatsFlags
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
name|rest
operator|.
name|BaseRestHandler
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
name|action
operator|.
name|RestActions
operator|.
name|NodesResponseRestListener
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
name|Collections
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
name|Locale
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
DECL|class|RestNodesStatsAction
specifier|public
class|class
name|RestNodesStatsAction
extends|extends
name|BaseRestHandler
block|{
DECL|method|RestNodesStatsAction
specifier|public
name|RestNodesStatsAction
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
literal|"/_nodes/stats"
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
literal|"/_nodes/{nodeId}/stats"
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
literal|"/_nodes/stats/{metric}"
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
literal|"/_nodes/{nodeId}/stats/{metric}"
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
literal|"/_nodes/stats/{metric}/{index_metric}"
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
literal|"/_nodes/{nodeId}/stats/{metric}/{index_metric}"
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
DECL|field|METRICS
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Consumer
argument_list|<
name|NodesStatsRequest
argument_list|>
argument_list|>
name|METRICS
decl_stmt|;
static|static
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Consumer
argument_list|<
name|NodesStatsRequest
argument_list|>
argument_list|>
name|metrics
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"os"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|os
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"jvm"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|jvm
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"thread_pool"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|threadPool
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"fs"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|fs
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"transport"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|transport
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"http"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|http
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"indices"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|indices
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"process"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|process
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"breaker"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|breaker
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"script"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|script
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"discovery"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|discovery
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|metrics
operator|.
name|put
argument_list|(
literal|"ingest"
argument_list|,
name|r
lambda|->
name|r
operator|.
name|ingest
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|METRICS
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|metrics
argument_list|)
expr_stmt|;
block|}
DECL|field|FLAGS
specifier|static
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Consumer
argument_list|<
name|CommonStatsFlags
argument_list|>
argument_list|>
name|FLAGS
decl_stmt|;
static|static
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Consumer
argument_list|<
name|CommonStatsFlags
argument_list|>
argument_list|>
name|flags
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|Flag
name|flag
range|:
name|CommonStatsFlags
operator|.
name|Flag
operator|.
name|values
argument_list|()
control|)
block|{
name|flags
operator|.
name|put
argument_list|(
name|flag
operator|.
name|getRestName
argument_list|()
argument_list|,
name|f
lambda|->
name|f
operator|.
name|set
argument_list|(
name|flag
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|FLAGS
operator|=
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|flags
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
literal|"nodes_stats_action"
return|;
block|}
annotation|@
name|Override
DECL|method|prepareRequest
specifier|public
name|RestChannelConsumer
name|prepareRequest
parameter_list|(
specifier|final
name|RestRequest
name|request
parameter_list|,
specifier|final
name|NodeClient
name|client
parameter_list|)
throws|throws
name|IOException
block|{
name|String
index|[]
name|nodesIds
init|=
name|Strings
operator|.
name|splitStringByCommaToArray
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"nodeId"
argument_list|)
argument_list|)
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|metrics
init|=
name|Strings
operator|.
name|splitStringByCommaToSet
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"metric"
argument_list|,
literal|"_all"
argument_list|)
argument_list|)
decl_stmt|;
name|NodesStatsRequest
name|nodesStatsRequest
init|=
operator|new
name|NodesStatsRequest
argument_list|(
name|nodesIds
argument_list|)
decl_stmt|;
name|nodesStatsRequest
operator|.
name|timeout
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"timeout"
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|metrics
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|metrics
operator|.
name|contains
argument_list|(
literal|"_all"
argument_list|)
condition|)
block|{
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"index_metric"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"request [%s] contains index metrics [%s] but all stats requested"
argument_list|,
name|request
operator|.
name|path
argument_list|()
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"index_metric"
argument_list|)
argument_list|)
argument_list|)
throw|;
block|}
name|nodesStatsRequest
operator|.
name|all
argument_list|()
expr_stmt|;
name|nodesStatsRequest
operator|.
name|indices
argument_list|(
name|CommonStatsFlags
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"_all"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"request [%s] contains _all and individual metrics [%s]"
argument_list|,
name|request
operator|.
name|path
argument_list|()
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"metric"
argument_list|)
argument_list|)
argument_list|)
throw|;
block|}
else|else
block|{
name|nodesStatsRequest
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// use a sorted set so the unrecognized parameters appear in a reliable sorted order
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|invalidMetrics
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|metric
range|:
name|metrics
control|)
block|{
specifier|final
name|Consumer
argument_list|<
name|NodesStatsRequest
argument_list|>
name|handler
init|=
name|METRICS
operator|.
name|get
argument_list|(
name|metric
argument_list|)
decl_stmt|;
if|if
condition|(
name|handler
operator|!=
literal|null
condition|)
block|{
name|handler
operator|.
name|accept
argument_list|(
name|nodesStatsRequest
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|invalidMetrics
operator|.
name|add
argument_list|(
name|metric
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|invalidMetrics
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|unrecognized
argument_list|(
name|request
argument_list|,
name|invalidMetrics
argument_list|,
name|METRICS
operator|.
name|keySet
argument_list|()
argument_list|,
literal|"metric"
argument_list|)
argument_list|)
throw|;
block|}
comment|// check for index specific metrics
if|if
condition|(
name|metrics
operator|.
name|contains
argument_list|(
literal|"indices"
argument_list|)
condition|)
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|indexMetrics
init|=
name|Strings
operator|.
name|splitStringByCommaToSet
argument_list|(
name|request
operator|.
name|param
argument_list|(
literal|"index_metric"
argument_list|,
literal|"_all"
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexMetrics
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|indexMetrics
operator|.
name|contains
argument_list|(
literal|"_all"
argument_list|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|(
name|CommonStatsFlags
operator|.
name|ALL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|CommonStatsFlags
name|flags
init|=
operator|new
name|CommonStatsFlags
argument_list|()
decl_stmt|;
name|flags
operator|.
name|clear
argument_list|()
expr_stmt|;
comment|// use a sorted set so the unrecognized parameters appear in a reliable sorted order
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|invalidIndexMetrics
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|String
name|indexMetric
range|:
name|indexMetrics
control|)
block|{
specifier|final
name|Consumer
argument_list|<
name|CommonStatsFlags
argument_list|>
name|handler
init|=
name|FLAGS
operator|.
name|get
argument_list|(
name|indexMetric
argument_list|)
decl_stmt|;
if|if
condition|(
name|handler
operator|!=
literal|null
condition|)
block|{
name|handler
operator|.
name|accept
argument_list|(
name|flags
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|invalidIndexMetrics
operator|.
name|add
argument_list|(
name|indexMetric
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|invalidIndexMetrics
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|unrecognized
argument_list|(
name|request
argument_list|,
name|invalidIndexMetrics
argument_list|,
name|FLAGS
operator|.
name|keySet
argument_list|()
argument_list|,
literal|"index metric"
argument_list|)
argument_list|)
throw|;
block|}
name|nodesStatsRequest
operator|.
name|indices
argument_list|(
name|flags
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"index_metric"
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"request [%s] contains index metrics [%s] but indices stats not requested"
argument_list|,
name|request
operator|.
name|path
argument_list|()
argument_list|,
name|request
operator|.
name|param
argument_list|(
literal|"index_metric"
argument_list|)
argument_list|)
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|FieldData
argument_list|)
operator|&&
operator|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"fields"
argument_list|)
operator|||
name|request
operator|.
name|hasParam
argument_list|(
literal|"fielddata_fields"
argument_list|)
operator|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|fieldDataFields
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"fielddata_fields"
argument_list|,
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"fields"
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Completion
argument_list|)
operator|&&
operator|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"fields"
argument_list|)
operator|||
name|request
operator|.
name|hasParam
argument_list|(
literal|"completion_fields"
argument_list|)
operator|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|completionDataFields
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"completion_fields"
argument_list|,
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"fields"
argument_list|,
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Search
argument_list|)
operator|&&
operator|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"groups"
argument_list|)
operator|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|groups
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"groups"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Indexing
argument_list|)
operator|&&
operator|(
name|request
operator|.
name|hasParam
argument_list|(
literal|"types"
argument_list|)
operator|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|types
argument_list|(
name|request
operator|.
name|paramAsStringArray
argument_list|(
literal|"types"
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|isSet
argument_list|(
name|Flag
operator|.
name|Segments
argument_list|)
condition|)
block|{
name|nodesStatsRequest
operator|.
name|indices
argument_list|()
operator|.
name|includeSegmentFileSizes
argument_list|(
name|request
operator|.
name|paramAsBoolean
argument_list|(
literal|"include_segment_file_sizes"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|nodesStats
argument_list|(
name|nodesStatsRequest
argument_list|,
operator|new
name|NodesResponseRestListener
argument_list|<>
argument_list|(
name|channel
argument_list|)
argument_list|)
return|;
block|}
DECL|field|RESPONSE_PARAMS
specifier|private
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|RESPONSE_PARAMS
init|=
name|Collections
operator|.
name|singleton
argument_list|(
literal|"level"
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|responseParams
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|responseParams
parameter_list|()
block|{
return|return
name|RESPONSE_PARAMS
return|;
block|}
annotation|@
name|Override
DECL|method|canTripCircuitBreaker
specifier|public
name|boolean
name|canTripCircuitBreaker
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

