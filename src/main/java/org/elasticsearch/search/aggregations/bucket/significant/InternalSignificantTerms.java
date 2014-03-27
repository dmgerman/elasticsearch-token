begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.significant
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
name|significant
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Maps
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
name|util
operator|.
name|BigArrays
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
name|search
operator|.
name|aggregations
operator|.
name|Aggregations
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
name|InternalAggregation
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
name|InternalAggregations
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|InternalSignificantTerms
specifier|public
specifier|abstract
class|class
name|InternalSignificantTerms
extends|extends
name|InternalAggregation
implements|implements
name|SignificantTerms
implements|,
name|ToXContent
implements|,
name|Streamable
block|{
DECL|field|requiredSize
specifier|protected
name|int
name|requiredSize
decl_stmt|;
DECL|field|minDocCount
specifier|protected
name|long
name|minDocCount
decl_stmt|;
DECL|field|buckets
specifier|protected
name|Collection
argument_list|<
name|Bucket
argument_list|>
name|buckets
decl_stmt|;
DECL|field|bucketMap
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|Bucket
argument_list|>
name|bucketMap
decl_stmt|;
DECL|field|subsetSize
specifier|protected
name|long
name|subsetSize
decl_stmt|;
DECL|field|supersetSize
specifier|protected
name|long
name|supersetSize
decl_stmt|;
DECL|method|InternalSignificantTerms
specifier|protected
name|InternalSignificantTerms
parameter_list|()
block|{}
comment|// for serialization
comment|// TODO updateScore call in constructor to be cleaned up as part of adding pluggable scoring algos
annotation|@
name|SuppressWarnings
argument_list|(
literal|"PMD.ConstructorCallsOverridableMethod"
argument_list|)
DECL|class|Bucket
specifier|public
specifier|static
specifier|abstract
class|class
name|Bucket
extends|extends
name|SignificantTerms
operator|.
name|Bucket
block|{
DECL|field|bucketOrd
name|long
name|bucketOrd
decl_stmt|;
DECL|field|aggregations
specifier|protected
name|InternalAggregations
name|aggregations
decl_stmt|;
DECL|field|score
name|double
name|score
decl_stmt|;
DECL|method|Bucket
specifier|protected
name|Bucket
parameter_list|(
name|long
name|subsetDf
parameter_list|,
name|long
name|subsetSize
parameter_list|,
name|long
name|supersetDf
parameter_list|,
name|long
name|supersetSize
parameter_list|,
name|InternalAggregations
name|aggregations
parameter_list|)
block|{
name|super
argument_list|(
name|subsetDf
argument_list|,
name|subsetSize
argument_list|,
name|supersetDf
argument_list|,
name|supersetSize
argument_list|)
expr_stmt|;
name|this
operator|.
name|aggregations
operator|=
name|aggregations
expr_stmt|;
assert|assert
name|subsetDf
operator|<=
name|supersetDf
assert|;
name|updateScore
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getSubsetDf
specifier|public
name|long
name|getSubsetDf
parameter_list|()
block|{
return|return
name|subsetDf
return|;
block|}
annotation|@
name|Override
DECL|method|getSupersetDf
specifier|public
name|long
name|getSupersetDf
parameter_list|()
block|{
return|return
name|supersetDf
return|;
block|}
annotation|@
name|Override
DECL|method|getSupersetSize
specifier|public
name|long
name|getSupersetSize
parameter_list|()
block|{
return|return
name|supersetSize
return|;
block|}
annotation|@
name|Override
DECL|method|getSubsetSize
specifier|public
name|long
name|getSubsetSize
parameter_list|()
block|{
return|return
name|subsetSize
return|;
block|}
comment|/**          * Calculates the significance of a term in a sample against a background of          * normal distributions by comparing the changes in frequency. This is the heart          * of the significant terms feature.          *<p/>          * TODO - allow pluggable scoring implementations          *          * @param subsetFreq   The frequency of the term in the selected sample          * @param subsetSize   The size of the selected sample (typically number of docs)          * @param supersetFreq The frequency of the term in the superset from which the sample was taken          * @param supersetSize The size of the superset from which the sample was taken  (typically number of docs)          * @return a "significance" score          */
DECL|method|getSampledTermSignificance
specifier|public
specifier|static
name|double
name|getSampledTermSignificance
parameter_list|(
name|long
name|subsetFreq
parameter_list|,
name|long
name|subsetSize
parameter_list|,
name|long
name|supersetFreq
parameter_list|,
name|long
name|supersetSize
parameter_list|)
block|{
if|if
condition|(
operator|(
name|subsetSize
operator|==
literal|0
operator|)
operator|||
operator|(
name|supersetSize
operator|==
literal|0
operator|)
condition|)
block|{
comment|// avoid any divide by zero issues
return|return
literal|0
return|;
block|}
name|double
name|subsetProbability
init|=
operator|(
name|double
operator|)
name|subsetFreq
operator|/
operator|(
name|double
operator|)
name|subsetSize
decl_stmt|;
name|double
name|supersetProbability
init|=
operator|(
name|double
operator|)
name|supersetFreq
operator|/
operator|(
name|double
operator|)
name|supersetSize
decl_stmt|;
comment|// Using absoluteProbabilityChange alone favours very common words e.g. you, we etc
comment|// because a doubling in popularity of a common term is a big percent difference
comment|// whereas a rare term would have to achieve a hundred-fold increase in popularity to
comment|// achieve the same difference measure.
comment|// In favouring common words as suggested features for search we would get high
comment|// recall but low precision.
name|double
name|absoluteProbabilityChange
init|=
name|subsetProbability
operator|-
name|supersetProbability
decl_stmt|;
if|if
condition|(
name|absoluteProbabilityChange
operator|<=
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
comment|// Using relativeProbabilityChange tends to favour rarer terms e.g.mis-spellings or
comment|// unique URLs.
comment|// A very low-probability term can very easily double in popularity due to the low
comment|// numbers required to do so whereas a high-probability term would have to add many
comment|// extra individual sightings to achieve the same shift.
comment|// In favouring rare words as suggested features for search we would get high
comment|// precision but low recall.
name|double
name|relativeProbabilityChange
init|=
operator|(
name|subsetProbability
operator|/
name|supersetProbability
operator|)
decl_stmt|;
comment|// A blend of the above metrics - favours medium-rare terms to strike a useful
comment|// balance between precision and recall.
return|return
name|absoluteProbabilityChange
operator|*
name|relativeProbabilityChange
return|;
block|}
DECL|method|updateScore
specifier|public
name|void
name|updateScore
parameter_list|()
block|{
name|score
operator|=
name|getSampledTermSignificance
argument_list|(
name|subsetDf
argument_list|,
name|subsetSize
argument_list|,
name|supersetDf
argument_list|,
name|supersetSize
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getDocCount
specifier|public
name|long
name|getDocCount
parameter_list|()
block|{
return|return
name|subsetDf
return|;
block|}
annotation|@
name|Override
DECL|method|getAggregations
specifier|public
name|Aggregations
name|getAggregations
parameter_list|()
block|{
return|return
name|aggregations
return|;
block|}
DECL|method|reduce
specifier|public
name|Bucket
name|reduce
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Bucket
argument_list|>
name|buckets
parameter_list|,
name|BigArrays
name|bigArrays
parameter_list|)
block|{
if|if
condition|(
name|buckets
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
return|return
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
name|Bucket
name|reduced
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|InternalAggregations
argument_list|>
name|aggregationsList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Bucket
name|bucket
range|:
name|buckets
control|)
block|{
if|if
condition|(
name|reduced
operator|==
literal|null
condition|)
block|{
name|reduced
operator|=
name|bucket
expr_stmt|;
block|}
else|else
block|{
name|reduced
operator|.
name|subsetDf
operator|+=
name|bucket
operator|.
name|subsetDf
expr_stmt|;
name|reduced
operator|.
name|supersetDf
operator|+=
name|bucket
operator|.
name|supersetDf
expr_stmt|;
name|reduced
operator|.
name|updateScore
argument_list|()
expr_stmt|;
block|}
name|aggregationsList
operator|.
name|add
argument_list|(
name|bucket
operator|.
name|aggregations
argument_list|)
expr_stmt|;
block|}
assert|assert
name|reduced
operator|.
name|subsetDf
operator|<=
name|reduced
operator|.
name|supersetDf
assert|;
name|reduced
operator|.
name|aggregations
operator|=
name|InternalAggregations
operator|.
name|reduce
argument_list|(
name|aggregationsList
argument_list|,
name|bigArrays
argument_list|)
expr_stmt|;
return|return
name|reduced
return|;
block|}
annotation|@
name|Override
DECL|method|getSignificanceScore
specifier|public
name|double
name|getSignificanceScore
parameter_list|()
block|{
return|return
name|score
return|;
block|}
block|}
DECL|method|InternalSignificantTerms
specifier|protected
name|InternalSignificantTerms
parameter_list|(
name|long
name|subsetSize
parameter_list|,
name|long
name|supersetSize
parameter_list|,
name|String
name|name
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|Collection
argument_list|<
name|Bucket
argument_list|>
name|buckets
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|requiredSize
operator|=
name|requiredSize
expr_stmt|;
name|this
operator|.
name|minDocCount
operator|=
name|minDocCount
expr_stmt|;
name|this
operator|.
name|buckets
operator|=
name|buckets
expr_stmt|;
name|this
operator|.
name|subsetSize
operator|=
name|subsetSize
expr_stmt|;
name|this
operator|.
name|supersetSize
operator|=
name|supersetSize
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|SignificantTerms
operator|.
name|Bucket
argument_list|>
name|iterator
parameter_list|()
block|{
name|Object
name|o
init|=
name|buckets
operator|.
name|iterator
argument_list|()
decl_stmt|;
return|return
operator|(
name|Iterator
argument_list|<
name|SignificantTerms
operator|.
name|Bucket
argument_list|>
operator|)
name|o
return|;
block|}
annotation|@
name|Override
DECL|method|getBuckets
specifier|public
name|Collection
argument_list|<
name|SignificantTerms
operator|.
name|Bucket
argument_list|>
name|getBuckets
parameter_list|()
block|{
name|Object
name|o
init|=
name|buckets
decl_stmt|;
return|return
operator|(
name|Collection
argument_list|<
name|SignificantTerms
operator|.
name|Bucket
argument_list|>
operator|)
name|o
return|;
block|}
annotation|@
name|Override
DECL|method|getBucketByKey
specifier|public
name|SignificantTerms
operator|.
name|Bucket
name|getBucketByKey
parameter_list|(
name|String
name|term
parameter_list|)
block|{
if|if
condition|(
name|bucketMap
operator|==
literal|null
condition|)
block|{
name|bucketMap
operator|=
name|Maps
operator|.
name|newHashMapWithExpectedSize
argument_list|(
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Bucket
name|bucket
range|:
name|buckets
control|)
block|{
name|bucketMap
operator|.
name|put
argument_list|(
name|bucket
operator|.
name|getKey
argument_list|()
argument_list|,
name|bucket
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|bucketMap
operator|.
name|get
argument_list|(
name|term
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|reduce
specifier|public
name|InternalSignificantTerms
name|reduce
parameter_list|(
name|ReduceContext
name|reduceContext
parameter_list|)
block|{
name|List
argument_list|<
name|InternalAggregation
argument_list|>
name|aggregations
init|=
name|reduceContext
operator|.
name|aggregations
argument_list|()
decl_stmt|;
if|if
condition|(
name|aggregations
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|InternalSignificantTerms
name|terms
init|=
operator|(
name|InternalSignificantTerms
operator|)
name|aggregations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|terms
operator|.
name|trimExcessEntries
argument_list|()
expr_stmt|;
return|return
name|terms
return|;
block|}
name|InternalSignificantTerms
name|reduced
init|=
literal|null
decl_stmt|;
name|long
name|globalSubsetSize
init|=
literal|0
decl_stmt|;
name|long
name|globalSupersetSize
init|=
literal|0
decl_stmt|;
comment|// Compute the overall result set size and the corpus size using the
comment|// top-level Aggregations from each shard
for|for
control|(
name|InternalAggregation
name|aggregation
range|:
name|aggregations
control|)
block|{
name|InternalSignificantTerms
name|terms
init|=
operator|(
name|InternalSignificantTerms
operator|)
name|aggregation
decl_stmt|;
name|globalSubsetSize
operator|+=
name|terms
operator|.
name|subsetSize
expr_stmt|;
name|globalSupersetSize
operator|+=
name|terms
operator|.
name|supersetSize
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|InternalSignificantTerms
operator|.
name|Bucket
argument_list|>
argument_list|>
name|buckets
init|=
literal|null
decl_stmt|;
for|for
control|(
name|InternalAggregation
name|aggregation
range|:
name|aggregations
control|)
block|{
name|InternalSignificantTerms
name|terms
init|=
operator|(
name|InternalSignificantTerms
operator|)
name|aggregation
decl_stmt|;
if|if
condition|(
name|terms
operator|instanceof
name|UnmappedSignificantTerms
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|reduced
operator|==
literal|null
condition|)
block|{
name|reduced
operator|=
name|terms
expr_stmt|;
block|}
if|if
condition|(
name|buckets
operator|==
literal|null
condition|)
block|{
name|buckets
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|terms
operator|.
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Bucket
name|bucket
range|:
name|terms
operator|.
name|buckets
control|)
block|{
name|List
argument_list|<
name|Bucket
argument_list|>
name|existingBuckets
init|=
name|buckets
operator|.
name|get
argument_list|(
name|bucket
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingBuckets
operator|==
literal|null
condition|)
block|{
name|existingBuckets
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|aggregations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|buckets
operator|.
name|put
argument_list|(
name|bucket
operator|.
name|getKey
argument_list|()
argument_list|,
name|existingBuckets
argument_list|)
expr_stmt|;
block|}
comment|// Adjust the buckets with the global stats representing the
comment|// total size of the pots from which the stats are drawn
name|bucket
operator|.
name|subsetSize
operator|=
name|globalSubsetSize
expr_stmt|;
name|bucket
operator|.
name|supersetSize
operator|=
name|globalSupersetSize
expr_stmt|;
name|bucket
operator|.
name|updateScore
argument_list|()
expr_stmt|;
name|existingBuckets
operator|.
name|add
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|reduced
operator|==
literal|null
condition|)
block|{
comment|// there are only unmapped terms, so we just return the first one
comment|// (no need to reduce)
return|return
operator|(
name|UnmappedSignificantTerms
operator|)
name|aggregations
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
specifier|final
name|int
name|size
init|=
name|Math
operator|.
name|min
argument_list|(
name|requiredSize
argument_list|,
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|BucketSignificancePriorityQueue
name|ordered
init|=
operator|new
name|BucketSignificancePriorityQueue
argument_list|(
name|size
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|Bucket
argument_list|>
argument_list|>
name|entry
range|:
name|buckets
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|List
argument_list|<
name|Bucket
argument_list|>
name|sameTermBuckets
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
specifier|final
name|Bucket
name|b
init|=
name|sameTermBuckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|reduce
argument_list|(
name|sameTermBuckets
argument_list|,
name|reduceContext
operator|.
name|bigArrays
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
operator|(
name|b
operator|.
name|score
operator|>
literal|0
operator|)
operator|&&
operator|(
name|b
operator|.
name|subsetDf
operator|>=
name|minDocCount
operator|)
condition|)
block|{
name|ordered
operator|.
name|insertWithOverflow
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
block|}
name|Bucket
index|[]
name|list
init|=
operator|new
name|Bucket
index|[
name|ordered
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|ordered
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|list
index|[
name|i
index|]
operator|=
operator|(
name|Bucket
operator|)
name|ordered
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
name|reduced
operator|.
name|buckets
operator|=
name|Arrays
operator|.
name|asList
argument_list|(
name|list
argument_list|)
expr_stmt|;
name|reduced
operator|.
name|subsetSize
operator|=
name|globalSubsetSize
expr_stmt|;
name|reduced
operator|.
name|supersetSize
operator|=
name|globalSupersetSize
expr_stmt|;
return|return
name|reduced
return|;
block|}
DECL|method|trimExcessEntries
specifier|final
name|void
name|trimExcessEntries
parameter_list|()
block|{
specifier|final
name|List
argument_list|<
name|Bucket
argument_list|>
name|newBuckets
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|Bucket
name|b
range|:
name|buckets
control|)
block|{
if|if
condition|(
name|newBuckets
operator|.
name|size
argument_list|()
operator|>=
name|requiredSize
condition|)
block|{
break|break;
block|}
if|if
condition|(
name|b
operator|.
name|subsetDf
operator|>=
name|minDocCount
condition|)
block|{
name|newBuckets
operator|.
name|add
argument_list|(
name|b
argument_list|)
expr_stmt|;
block|}
block|}
name|buckets
operator|=
name|newBuckets
expr_stmt|;
block|}
block|}
end_class

end_unit

