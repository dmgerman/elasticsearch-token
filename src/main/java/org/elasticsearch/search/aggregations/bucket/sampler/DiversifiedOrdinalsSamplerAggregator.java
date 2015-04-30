begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.sampler
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
name|sampler
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
name|DocValues
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
name|index
operator|.
name|LeafReaderContext
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
name|index
operator|.
name|NumericDocValues
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
name|index
operator|.
name|RandomAccessOrds
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
name|index
operator|.
name|SortedDocValues
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
name|DiversifiedTopDocsCollector
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
name|DiversifiedTopDocsCollector
operator|.
name|ScoreDocKey
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
name|TopDocsCollector
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
name|Aggregator
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
name|AggregatorFactories
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
name|BestDocsDeferringCollector
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
name|DeferringBucketCollector
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
name|reducers
operator|.
name|Reducer
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
name|ValuesSource
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

begin_class
DECL|class|DiversifiedOrdinalsSamplerAggregator
specifier|public
class|class
name|DiversifiedOrdinalsSamplerAggregator
extends|extends
name|SamplerAggregator
block|{
DECL|field|valuesSource
specifier|private
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|FieldData
name|valuesSource
decl_stmt|;
DECL|field|maxDocsPerValue
specifier|private
name|int
name|maxDocsPerValue
decl_stmt|;
DECL|method|DiversifiedOrdinalsSamplerAggregator
specifier|public
name|DiversifiedOrdinalsSamplerAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|AggregatorFactories
name|factories
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|List
argument_list|<
name|Reducer
argument_list|>
name|reducers
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|,
name|ValuesSource
operator|.
name|Bytes
operator|.
name|WithOrdinals
operator|.
name|FieldData
name|valuesSource
parameter_list|,
name|int
name|maxDocsPerValue
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|name
argument_list|,
name|shardSize
argument_list|,
name|factories
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|reducers
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|valuesSource
operator|=
name|valuesSource
expr_stmt|;
name|this
operator|.
name|maxDocsPerValue
operator|=
name|maxDocsPerValue
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getDeferringCollector
specifier|public
name|DeferringBucketCollector
name|getDeferringCollector
parameter_list|()
block|{
name|bdd
operator|=
operator|new
name|DiverseDocsDeferringCollector
argument_list|()
expr_stmt|;
return|return
name|bdd
return|;
block|}
comment|/**      * A {@link DeferringBucketCollector} that identifies top scoring documents      * but de-duped by a key then passes only these on to nested collectors.      * This implementation is only for use with a single bucket aggregation.      */
DECL|class|DiverseDocsDeferringCollector
class|class
name|DiverseDocsDeferringCollector
extends|extends
name|BestDocsDeferringCollector
block|{
DECL|method|DiverseDocsDeferringCollector
specifier|public
name|DiverseDocsDeferringCollector
parameter_list|()
block|{
name|super
argument_list|(
name|shardSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createTopDocsCollector
specifier|protected
name|TopDocsCollector
argument_list|<
name|ScoreDocKey
argument_list|>
name|createTopDocsCollector
parameter_list|(
name|int
name|size
parameter_list|)
block|{
return|return
operator|new
name|ValuesDiversifiedTopDocsCollector
argument_list|(
name|size
argument_list|,
name|maxDocsPerValue
argument_list|)
return|;
block|}
comment|// This class extends the DiversifiedTopDocsCollector and provides
comment|// a lookup from elasticsearch's ValuesSource
DECL|class|ValuesDiversifiedTopDocsCollector
class|class
name|ValuesDiversifiedTopDocsCollector
extends|extends
name|DiversifiedTopDocsCollector
block|{
DECL|method|ValuesDiversifiedTopDocsCollector
specifier|public
name|ValuesDiversifiedTopDocsCollector
parameter_list|(
name|int
name|numHits
parameter_list|,
name|int
name|maxHitsPerKey
parameter_list|)
block|{
name|super
argument_list|(
name|numHits
argument_list|,
name|maxHitsPerKey
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getKeys
specifier|protected
name|NumericDocValues
name|getKeys
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
block|{
specifier|final
name|RandomAccessOrds
name|globalOrds
init|=
name|valuesSource
operator|.
name|globalOrdinalsValues
argument_list|(
name|context
argument_list|)
decl_stmt|;
specifier|final
name|SortedDocValues
name|singleValues
init|=
name|DocValues
operator|.
name|unwrapSingleton
argument_list|(
name|globalOrds
argument_list|)
decl_stmt|;
if|if
condition|(
name|singleValues
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|NumericDocValues
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|get
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
return|return
name|singleValues
operator|.
name|getOrd
argument_list|(
name|doc
argument_list|)
return|;
block|}
block|}
return|;
block|}
return|return
operator|new
name|NumericDocValues
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|get
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|globalOrds
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|long
name|valuesCount
init|=
name|globalOrds
operator|.
name|cardinality
argument_list|()
decl_stmt|;
if|if
condition|(
name|valuesCount
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Sample diversifying key must be a single valued-field"
argument_list|)
throw|;
block|}
if|if
condition|(
name|valuesCount
operator|==
literal|1
condition|)
block|{
name|long
name|result
init|=
name|globalOrds
operator|.
name|ordAt
argument_list|(
literal|0
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
return|return
name|Long
operator|.
name|MIN_VALUE
return|;
block|}
block|}
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

