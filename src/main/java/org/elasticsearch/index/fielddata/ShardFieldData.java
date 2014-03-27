begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|ObjectLongOpenHashMap
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
name|inject
operator|.
name|Inject
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
name|metrics
operator|.
name|CounterMetric
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
name|regex
operator|.
name|Regex
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
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentCollections
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
name|mapper
operator|.
name|FieldMapper
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
name|mapper
operator|.
name|internal
operator|.
name|ParentFieldMapper
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
name|settings
operator|.
name|IndexSettings
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
name|shard
operator|.
name|AbstractIndexShardComponent
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
name|shard
operator|.
name|ShardId
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
name|fielddata
operator|.
name|breaker
operator|.
name|CircuitBreakerService
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
name|concurrent
operator|.
name|ConcurrentMap
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ShardFieldData
specifier|public
class|class
name|ShardFieldData
extends|extends
name|AbstractIndexShardComponent
implements|implements
name|IndexFieldDataCache
operator|.
name|Listener
block|{
DECL|field|evictionsMetric
specifier|final
name|CounterMetric
name|evictionsMetric
init|=
operator|new
name|CounterMetric
argument_list|()
decl_stmt|;
DECL|field|totalMetric
specifier|final
name|CounterMetric
name|totalMetric
init|=
operator|new
name|CounterMetric
argument_list|()
decl_stmt|;
DECL|field|perFieldTotals
specifier|final
name|ConcurrentMap
argument_list|<
name|String
argument_list|,
name|CounterMetric
argument_list|>
name|perFieldTotals
init|=
name|ConcurrentCollections
operator|.
name|newConcurrentMap
argument_list|()
decl_stmt|;
DECL|field|breakerService
specifier|private
specifier|final
name|CircuitBreakerService
name|breakerService
decl_stmt|;
annotation|@
name|Inject
DECL|method|ShardFieldData
specifier|public
name|ShardFieldData
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|,
name|CircuitBreakerService
name|breakerService
parameter_list|)
block|{
name|super
argument_list|(
name|shardId
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|breakerService
operator|=
name|breakerService
expr_stmt|;
block|}
DECL|method|stats
specifier|public
name|FieldDataStats
name|stats
parameter_list|(
name|String
modifier|...
name|fields
parameter_list|)
block|{
name|ObjectLongOpenHashMap
argument_list|<
name|String
argument_list|>
name|fieldTotals
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|fields
operator|!=
literal|null
operator|&&
name|fields
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|fieldTotals
operator|=
operator|new
name|ObjectLongOpenHashMap
argument_list|<>
argument_list|()
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|CounterMetric
argument_list|>
name|entry
range|:
name|perFieldTotals
operator|.
name|entrySet
argument_list|()
control|)
block|{
for|for
control|(
name|String
name|field
range|:
name|fields
control|)
block|{
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|field
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|fieldTotals
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|count
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
comment|// Because we report _parent field used memory separately via id cache, we need to subtract it from the
comment|// field data total memory used. This code should be removed for>= 2.0
name|long
name|memorySize
init|=
name|totalMetric
operator|.
name|count
argument_list|()
decl_stmt|;
if|if
condition|(
name|perFieldTotals
operator|.
name|containsKey
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|)
condition|)
block|{
name|memorySize
operator|-=
name|perFieldTotals
operator|.
name|get
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|)
operator|.
name|count
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|FieldDataStats
argument_list|(
name|memorySize
argument_list|,
name|evictionsMetric
operator|.
name|count
argument_list|()
argument_list|,
name|fieldTotals
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|onLoad
specifier|public
name|void
name|onLoad
parameter_list|(
name|FieldMapper
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|AtomicFieldData
name|fieldData
parameter_list|)
block|{
name|long
name|sizeInBytes
init|=
name|fieldData
operator|.
name|getMemorySizeInBytes
argument_list|()
decl_stmt|;
name|totalMetric
operator|.
name|inc
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
name|String
name|keyFieldName
init|=
name|fieldNames
operator|.
name|indexName
argument_list|()
decl_stmt|;
name|CounterMetric
name|total
init|=
name|perFieldTotals
operator|.
name|get
argument_list|(
name|keyFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|total
operator|!=
literal|null
condition|)
block|{
name|total
operator|.
name|inc
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|total
operator|=
operator|new
name|CounterMetric
argument_list|()
expr_stmt|;
name|total
operator|.
name|inc
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
name|CounterMetric
name|prev
init|=
name|perFieldTotals
operator|.
name|putIfAbsent
argument_list|(
name|keyFieldName
argument_list|,
name|total
argument_list|)
decl_stmt|;
if|if
condition|(
name|prev
operator|!=
literal|null
condition|)
block|{
name|prev
operator|.
name|inc
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|onUnload
specifier|public
name|void
name|onUnload
parameter_list|(
name|FieldMapper
operator|.
name|Names
name|fieldNames
parameter_list|,
name|FieldDataType
name|fieldDataType
parameter_list|,
name|boolean
name|wasEvicted
parameter_list|,
name|long
name|sizeInBytes
parameter_list|,
annotation|@
name|Nullable
name|AtomicFieldData
name|fieldData
parameter_list|)
block|{
if|if
condition|(
name|wasEvicted
condition|)
block|{
name|evictionsMetric
operator|.
name|inc
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|sizeInBytes
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// Since field data is being unloaded (due to expiration or manual
comment|// clearing), we also need to decrement the used bytes in the breaker
name|breakerService
operator|.
name|getBreaker
argument_list|()
operator|.
name|addWithoutBreaking
argument_list|(
operator|-
name|sizeInBytes
argument_list|)
expr_stmt|;
name|totalMetric
operator|.
name|dec
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
name|String
name|keyFieldName
init|=
name|fieldNames
operator|.
name|indexName
argument_list|()
decl_stmt|;
name|CounterMetric
name|total
init|=
name|perFieldTotals
operator|.
name|get
argument_list|(
name|keyFieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|total
operator|!=
literal|null
condition|)
block|{
name|total
operator|.
name|dec
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

