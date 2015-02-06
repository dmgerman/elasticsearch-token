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
name|ElasticsearchIllegalStateException
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
name|lease
operator|.
name|Releasables
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
name|ObjectArray
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
comment|/**  * A factory that knows how to create an {@link Aggregator} of a specific type.  */
end_comment

begin_class
DECL|class|AggregatorFactory
specifier|public
specifier|abstract
class|class
name|AggregatorFactory
block|{
DECL|field|name
specifier|protected
name|String
name|name
decl_stmt|;
DECL|field|type
specifier|protected
name|String
name|type
decl_stmt|;
DECL|field|parent
specifier|protected
name|AggregatorFactory
name|parent
decl_stmt|;
DECL|field|factories
specifier|protected
name|AggregatorFactories
name|factories
init|=
name|AggregatorFactories
operator|.
name|EMPTY
decl_stmt|;
DECL|field|metaData
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
decl_stmt|;
comment|/**      * Constructs a new aggregator factory.      *      * @param name  The aggregation name      * @param type  The aggregation type      */
DECL|method|AggregatorFactory
specifier|public
name|AggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|type
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
comment|/**      * Registers sub-factories with this factory. The sub-factory will be responsible for the creation of sub-aggregators under the      * aggregator created by this factory.      *      * @param subFactories  The sub-factories      * @return  this factory (fluent interface)      */
DECL|method|subFactories
specifier|public
name|AggregatorFactory
name|subFactories
parameter_list|(
name|AggregatorFactories
name|subFactories
parameter_list|)
block|{
name|this
operator|.
name|factories
operator|=
name|subFactories
expr_stmt|;
name|this
operator|.
name|factories
operator|.
name|setParent
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Validates the state of this factory (makes sure the factory is properly configured)      */
DECL|method|validate
specifier|public
specifier|final
name|void
name|validate
parameter_list|()
block|{
name|doValidate
argument_list|()
expr_stmt|;
name|factories
operator|.
name|validate
argument_list|()
expr_stmt|;
block|}
comment|/**      * @return  The parent factory if one exists (will always return {@code null} for top level aggregator factories).      */
DECL|method|parent
specifier|public
name|AggregatorFactory
name|parent
parameter_list|()
block|{
return|return
name|parent
return|;
block|}
DECL|method|createInternal
specifier|protected
specifier|abstract
name|Aggregator
name|createInternal
parameter_list|(
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|boolean
name|collectsFromSingleBucket
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
function_decl|;
comment|/**      * Creates the aggregator      *      * @param context               The aggregation context      * @param parent                The parent aggregator (if this is a top level factory, the parent will be {@code null})      * @param collectsFromSingleBucket  If true then the created aggregator will only be collected with<tt>0</tt> as a bucket ordinal.      *                              Some factories can take advantage of this in order to return more optimized implementations.      *      * @return                      The created aggregator      */
DECL|method|create
specifier|public
specifier|final
name|Aggregator
name|create
parameter_list|(
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|boolean
name|collectsFromSingleBucket
parameter_list|)
throws|throws
name|IOException
block|{
name|Aggregator
name|aggregator
init|=
name|createInternal
argument_list|(
name|context
argument_list|,
name|parent
argument_list|,
name|collectsFromSingleBucket
argument_list|,
name|this
operator|.
name|metaData
argument_list|)
decl_stmt|;
return|return
name|aggregator
return|;
block|}
DECL|method|doValidate
specifier|public
name|void
name|doValidate
parameter_list|()
block|{     }
DECL|method|setMetaData
specifier|public
name|void
name|setMetaData
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
block|{
name|this
operator|.
name|metaData
operator|=
name|metaData
expr_stmt|;
block|}
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
name|factories
operator|.
name|needsScores
argument_list|()
return|;
block|}
comment|/**      * Utility method. Given an {@link AggregatorFactory} that creates {@link Aggregator}s that only know how      * to collect bucket<tt>0</tt>, this returns an aggregator that can collect any bucket.      */
DECL|method|asMultiBucketAggregator
specifier|protected
specifier|static
name|Aggregator
name|asMultiBucketAggregator
parameter_list|(
specifier|final
name|AggregatorFactory
name|factory
parameter_list|,
specifier|final
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Aggregator
name|first
init|=
name|factory
operator|.
name|create
argument_list|(
name|context
argument_list|,
name|parent
argument_list|,
literal|true
argument_list|)
decl_stmt|;
return|return
operator|new
name|Aggregator
argument_list|(
name|first
operator|.
name|name
argument_list|()
argument_list|,
name|AggregatorFactories
operator|.
name|EMPTY
argument_list|,
name|first
operator|.
name|context
argument_list|()
argument_list|,
name|first
operator|.
name|parent
argument_list|()
argument_list|,
name|first
operator|.
name|metaData
argument_list|()
argument_list|)
block|{
name|ObjectArray
argument_list|<
name|Aggregator
argument_list|>
name|aggregators
decl_stmt|;
name|LeafReaderContext
name|readerContext
decl_stmt|;
block|{
name|aggregators
operator|=
name|bigArrays
operator|.
name|newObjectArray
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|aggregators
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|first
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|shouldCollect
parameter_list|()
block|{
return|return
name|first
operator|.
name|shouldCollect
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doPreCollection
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|aggregators
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|Aggregator
name|aggregator
init|=
name|aggregators
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|aggregator
operator|!=
literal|null
condition|)
block|{
name|aggregator
operator|.
name|preCollection
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doPostCollection
parameter_list|()
throws|throws
name|IOException
block|{
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|aggregators
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|Aggregator
name|aggregator
init|=
name|aggregators
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|aggregator
operator|!=
literal|null
condition|)
block|{
name|aggregator
operator|.
name|postCollection
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|long
name|owningBucketOrdinal
parameter_list|)
throws|throws
name|IOException
block|{
name|aggregators
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|aggregators
argument_list|,
name|owningBucketOrdinal
operator|+
literal|1
argument_list|)
expr_stmt|;
name|Aggregator
name|aggregator
init|=
name|aggregators
operator|.
name|get
argument_list|(
name|owningBucketOrdinal
argument_list|)
decl_stmt|;
if|if
condition|(
name|aggregator
operator|==
literal|null
condition|)
block|{
name|aggregator
operator|=
name|factory
operator|.
name|create
argument_list|(
name|context
argument_list|,
name|parent
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|aggregator
operator|.
name|preCollection
argument_list|()
expr_stmt|;
name|aggregator
operator|.
name|setNextReader
argument_list|(
name|readerContext
argument_list|)
expr_stmt|;
name|aggregators
operator|.
name|set
argument_list|(
name|owningBucketOrdinal
argument_list|,
name|aggregator
argument_list|)
expr_stmt|;
block|}
name|aggregator
operator|.
name|collect
argument_list|(
name|doc
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setNextReader
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|readerContext
operator|=
name|context
expr_stmt|;
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
name|aggregators
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|Aggregator
name|aggregator
init|=
name|aggregators
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|aggregator
operator|!=
literal|null
condition|)
block|{
name|aggregator
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
literal|"Invalid context - aggregation must use addResults() to collect child results"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
block|{
return|return
name|first
operator|.
name|buildEmptyAggregation
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|doClose
parameter_list|()
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|aggregators
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|gatherAnalysis
parameter_list|(
name|BucketAnalysisCollector
name|results
parameter_list|,
name|long
name|owningBucketOrdinal
parameter_list|)
throws|throws
name|IOException
block|{
comment|// The bucket ordinal may be out of range in case of eg. a terms/filter/terms where
comment|// the filter matches no document in the highest buckets of the first terms agg
if|if
condition|(
name|owningBucketOrdinal
operator|>=
name|aggregators
operator|.
name|size
argument_list|()
operator|||
name|aggregators
operator|.
name|get
argument_list|(
name|owningBucketOrdinal
argument_list|)
operator|==
literal|null
condition|)
block|{
name|results
operator|.
name|add
argument_list|(
name|first
operator|.
name|buildEmptyAggregation
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|aggregators
operator|.
name|get
argument_list|(
name|owningBucketOrdinal
argument_list|)
operator|.
name|gatherAnalysis
argument_list|(
name|results
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

