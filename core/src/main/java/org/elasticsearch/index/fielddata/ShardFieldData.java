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
name|ObjectLongHashMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Accountable
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
name|FieldMemoryStats
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
name|shard
operator|.
name|ShardId
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

begin_class
DECL|class|ShardFieldData
specifier|public
class|class
name|ShardFieldData
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
name|ObjectLongHashMap
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
name|ObjectLongHashMap
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
if|if
condition|(
name|Regex
operator|.
name|simpleMatch
argument_list|(
name|fields
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
return|return
operator|new
name|FieldDataStats
argument_list|(
name|totalMetric
operator|.
name|count
argument_list|()
argument_list|,
name|evictionsMetric
operator|.
name|count
argument_list|()
argument_list|,
name|fieldTotals
operator|==
literal|null
condition|?
literal|null
else|:
operator|new
name|FieldMemoryStats
argument_list|(
name|fieldTotals
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|onCache
specifier|public
name|void
name|onCache
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|Accountable
name|ramUsage
parameter_list|)
block|{
name|totalMetric
operator|.
name|inc
argument_list|(
name|ramUsage
operator|.
name|ramBytesUsed
argument_list|()
argument_list|)
expr_stmt|;
name|CounterMetric
name|total
init|=
name|perFieldTotals
operator|.
name|get
argument_list|(
name|fieldName
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
name|ramUsage
operator|.
name|ramBytesUsed
argument_list|()
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
name|ramUsage
operator|.
name|ramBytesUsed
argument_list|()
argument_list|)
expr_stmt|;
name|CounterMetric
name|prev
init|=
name|perFieldTotals
operator|.
name|putIfAbsent
argument_list|(
name|fieldName
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
name|ramUsage
operator|.
name|ramBytesUsed
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|onRemoval
specifier|public
name|void
name|onRemoval
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|boolean
name|wasEvicted
parameter_list|,
name|long
name|sizeInBytes
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
name|totalMetric
operator|.
name|dec
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
name|CounterMetric
name|total
init|=
name|perFieldTotals
operator|.
name|get
argument_list|(
name|fieldName
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

