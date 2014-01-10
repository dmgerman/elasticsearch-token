begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.nested
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|nested
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
name|AtomicReaderContext
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
name|search
operator|.
name|DocIdSet
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
name|search
operator|.
name|Filter
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
name|Bits
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
name|FixedBitSet
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
name|lucene
operator|.
name|ReaderContextAware
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
name|lucene
operator|.
name|docset
operator|.
name|DocIdSets
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
name|MapperService
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
name|object
operator|.
name|ObjectMapper
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
name|search
operator|.
name|nested
operator|.
name|NonNestedDocsFilter
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
name|*
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
name|bucket
operator|.
name|SingleBucketAggregator
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|NestedAggregator
specifier|public
class|class
name|NestedAggregator
extends|extends
name|SingleBucketAggregator
implements|implements
name|ReaderContextAware
block|{
DECL|field|parentFilter
specifier|private
specifier|final
name|Filter
name|parentFilter
decl_stmt|;
DECL|field|childFilter
specifier|private
specifier|final
name|Filter
name|childFilter
decl_stmt|;
DECL|field|childDocs
specifier|private
name|Bits
name|childDocs
decl_stmt|;
DECL|field|parentDocs
specifier|private
name|FixedBitSet
name|parentDocs
decl_stmt|;
DECL|method|NestedAggregator
specifier|public
name|NestedAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|String
name|nestedPath
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|MapperService
operator|.
name|SmartNameObjectMapper
name|mapper
init|=
name|aggregationContext
operator|.
name|searchContext
argument_list|()
operator|.
name|smartNameObjectMapper
argument_list|(
name|nestedPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|mapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"facet nested path ["
operator|+
name|nestedPath
operator|+
literal|"] not found"
argument_list|)
throw|;
block|}
name|ObjectMapper
name|objectMapper
init|=
name|mapper
operator|.
name|mapper
argument_list|()
decl_stmt|;
if|if
condition|(
name|objectMapper
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"facet nested path ["
operator|+
name|nestedPath
operator|+
literal|"] not found"
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|objectMapper
operator|.
name|nested
argument_list|()
operator|.
name|isNested
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"facet nested path ["
operator|+
name|nestedPath
operator|+
literal|"] is not nested"
argument_list|)
throw|;
block|}
name|parentFilter
operator|=
name|aggregationContext
operator|.
name|searchContext
argument_list|()
operator|.
name|filterCache
argument_list|()
operator|.
name|cache
argument_list|(
name|NonNestedDocsFilter
operator|.
name|INSTANCE
argument_list|)
expr_stmt|;
name|childFilter
operator|=
name|aggregationContext
operator|.
name|searchContext
argument_list|()
operator|.
name|filterCache
argument_list|()
operator|.
name|cache
argument_list|(
name|objectMapper
operator|.
name|nestedTypeFilter
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|reader
parameter_list|)
block|{
try|try
block|{
name|DocIdSet
name|docIdSet
init|=
name|parentFilter
operator|.
name|getDocIdSet
argument_list|(
name|reader
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// In ES if parent is deleted, then also the children are deleted. Therefore acceptedDocs can also null here.
name|childDocs
operator|=
name|DocIdSets
operator|.
name|toSafeBits
argument_list|(
name|reader
operator|.
name|reader
argument_list|()
argument_list|,
name|childFilter
operator|.
name|getDocIdSet
argument_list|(
name|reader
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|docIdSet
argument_list|)
condition|)
block|{
name|parentDocs
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|parentDocs
operator|=
operator|(
name|FixedBitSet
operator|)
name|docIdSet
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"Failed to aggregate ["
operator|+
name|name
operator|+
literal|"]"
argument_list|,
name|ioe
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|parentDoc
parameter_list|,
name|long
name|bucketOrd
parameter_list|)
throws|throws
name|IOException
block|{
comment|// here we translate the parent doc to a list of its nested docs, and then call super.collect for evey one of them
comment|// so they'll be collected
if|if
condition|(
name|parentDoc
operator|==
literal|0
operator|||
name|parentDocs
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|int
name|prevParentDoc
init|=
name|parentDocs
operator|.
name|prevSetBit
argument_list|(
name|parentDoc
operator|-
literal|1
argument_list|)
decl_stmt|;
name|int
name|numChildren
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
operator|(
name|parentDoc
operator|-
literal|1
operator|)
init|;
name|i
operator|>
name|prevParentDoc
condition|;
name|i
operator|--
control|)
block|{
if|if
condition|(
name|childDocs
operator|.
name|get
argument_list|(
name|i
argument_list|)
condition|)
block|{
operator|++
name|numChildren
expr_stmt|;
name|collectBucketNoCounts
argument_list|(
name|i
argument_list|,
name|bucketOrd
argument_list|)
expr_stmt|;
block|}
block|}
name|incrementBucketDocCount
argument_list|(
name|numChildren
argument_list|,
name|bucketOrd
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|buildAggregation
specifier|public
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
block|{
return|return
operator|new
name|InternalNested
argument_list|(
name|name
argument_list|,
name|bucketDocCount
argument_list|(
name|owningBucketOrdinal
argument_list|)
argument_list|,
name|bucketAggregations
argument_list|(
name|owningBucketOrdinal
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|buildEmptyAggregation
specifier|public
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
block|{
return|return
operator|new
name|InternalNested
argument_list|(
name|name
argument_list|,
literal|0
argument_list|,
name|buildEmptySubAggregations
argument_list|()
argument_list|)
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
extends|extends
name|AggregatorFactory
block|{
DECL|field|path
specifier|private
specifier|final
name|String
name|path
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|path
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalNested
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|Aggregator
name|create
parameter_list|(
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|long
name|expectedBucketsCount
parameter_list|)
block|{
name|NestedAggregator
name|aggregator
init|=
operator|new
name|NestedAggregator
argument_list|(
name|name
argument_list|,
name|factories
argument_list|,
name|path
argument_list|,
name|context
argument_list|,
name|parent
argument_list|)
decl_stmt|;
name|context
operator|.
name|registerReaderContextAware
argument_list|(
name|aggregator
argument_list|)
expr_stmt|;
return|return
name|aggregator
return|;
block|}
block|}
block|}
end_class

end_unit

