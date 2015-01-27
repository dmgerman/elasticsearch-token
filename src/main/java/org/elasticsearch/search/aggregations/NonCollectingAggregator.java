begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
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
name|aggregations
operator|.
name|support
operator|.
name|AggregationContext
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
name|Map
import|;
end_import

begin_comment
comment|/**  * An aggregator that is not collected, this can typically be used when running an aggregation over a field that doesn't have  * a mapping.  */
end_comment

begin_class
DECL|class|NonCollectingAggregator
specifier|public
specifier|abstract
class|class
name|NonCollectingAggregator
extends|extends
name|AggregatorBase
block|{
DECL|method|NonCollectingAggregator
specifier|protected
name|NonCollectingAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|AggregatorFactories
name|subFactories
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|name
argument_list|,
name|subFactories
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
DECL|method|NonCollectingAggregator
specifier|protected
name|NonCollectingAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|(
name|name
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|AggregatorFactories
operator|.
name|EMPTY
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getLeafCollector
specifier|public
specifier|final
name|LeafBucketCollector
name|getLeafCollector
parameter_list|(
name|LeafReaderContext
name|reader
parameter_list|,
name|LeafBucketCollector
name|sub
parameter_list|)
block|{
comment|// the framework will automatically eliminate it
return|return
name|LeafBucketCollector
operator|.
name|NO_OP_COLLECTOR
return|;
block|}
annotation|@
name|Override
DECL|method|buildAggregation
specifier|public
specifier|final
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
block|{
return|return
name|buildEmptyAggregation
argument_list|()
return|;
block|}
block|}
end_class

end_unit

