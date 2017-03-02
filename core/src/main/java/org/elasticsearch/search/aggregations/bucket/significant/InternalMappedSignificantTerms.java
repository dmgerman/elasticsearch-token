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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|DocValueFormat
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
name|significant
operator|.
name|heuristics
operator|.
name|SignificanceHeuristic
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
name|pipeline
operator|.
name|PipelineAggregator
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|Function
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_class
DECL|class|InternalMappedSignificantTerms
specifier|public
specifier|abstract
class|class
name|InternalMappedSignificantTerms
parameter_list|<
name|A
extends|extends
name|InternalMappedSignificantTerms
parameter_list|<
name|A
parameter_list|,
name|B
parameter_list|>
parameter_list|,
name|B
extends|extends
name|InternalSignificantTerms
operator|.
name|Bucket
parameter_list|<
name|B
parameter_list|>
parameter_list|>
extends|extends
name|InternalSignificantTerms
argument_list|<
name|A
argument_list|,
name|B
argument_list|>
block|{
DECL|field|format
specifier|protected
specifier|final
name|DocValueFormat
name|format
decl_stmt|;
DECL|field|subsetSize
specifier|protected
specifier|final
name|long
name|subsetSize
decl_stmt|;
DECL|field|supersetSize
specifier|protected
specifier|final
name|long
name|supersetSize
decl_stmt|;
DECL|field|significanceHeuristic
specifier|protected
specifier|final
name|SignificanceHeuristic
name|significanceHeuristic
decl_stmt|;
DECL|field|buckets
specifier|protected
specifier|final
name|List
argument_list|<
name|B
argument_list|>
name|buckets
decl_stmt|;
DECL|field|bucketMap
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|B
argument_list|>
name|bucketMap
decl_stmt|;
DECL|method|InternalMappedSignificantTerms
specifier|protected
name|InternalMappedSignificantTerms
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|long
name|minDocCount
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|,
name|DocValueFormat
name|format
parameter_list|,
name|long
name|subsetSize
parameter_list|,
name|long
name|supersetSize
parameter_list|,
name|SignificanceHeuristic
name|significanceHeuristic
parameter_list|,
name|List
argument_list|<
name|B
argument_list|>
name|buckets
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|requiredSize
argument_list|,
name|minDocCount
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|format
operator|=
name|format
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
name|this
operator|.
name|significanceHeuristic
operator|=
name|significanceHeuristic
expr_stmt|;
block|}
DECL|method|InternalMappedSignificantTerms
specifier|protected
name|InternalMappedSignificantTerms
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|Bucket
operator|.
name|Reader
argument_list|<
name|B
argument_list|>
name|bucketReader
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|format
operator|=
name|in
operator|.
name|readNamedWriteable
argument_list|(
name|DocValueFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|subsetSize
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|supersetSize
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|significanceHeuristic
operator|=
name|in
operator|.
name|readNamedWriteable
argument_list|(
name|SignificanceHeuristic
operator|.
name|class
argument_list|)
expr_stmt|;
name|buckets
operator|=
name|in
operator|.
name|readList
argument_list|(
name|stream
lambda|->
name|bucketReader
operator|.
name|read
argument_list|(
name|stream
argument_list|,
name|subsetSize
argument_list|,
name|supersetSize
argument_list|,
name|format
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTermTypeInfoTo
specifier|protected
specifier|final
name|void
name|writeTermTypeInfoTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeNamedWriteable
argument_list|(
name|format
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|subsetSize
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|supersetSize
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeNamedWriteable
argument_list|(
name|significanceHeuristic
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeList
argument_list|(
name|buckets
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getBucketsInternal
specifier|protected
name|List
argument_list|<
name|B
argument_list|>
name|getBucketsInternal
parameter_list|()
block|{
return|return
name|buckets
return|;
block|}
annotation|@
name|Override
DECL|method|getBucketByKey
specifier|public
name|B
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
name|buckets
operator|.
name|stream
argument_list|()
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toMap
argument_list|(
name|InternalSignificantTerms
operator|.
name|Bucket
operator|::
name|getKeyAsString
argument_list|,
name|Function
operator|.
name|identity
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
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
DECL|method|getSubsetSize
specifier|protected
name|long
name|getSubsetSize
parameter_list|()
block|{
return|return
name|subsetSize
return|;
block|}
annotation|@
name|Override
DECL|method|getSupersetSize
specifier|protected
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
DECL|method|getSignificanceHeuristic
specifier|protected
name|SignificanceHeuristic
name|getSignificanceHeuristic
parameter_list|()
block|{
return|return
name|significanceHeuristic
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|InternalMappedSignificantTerms
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|that
init|=
operator|(
name|InternalMappedSignificantTerms
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
operator|)
name|obj
decl_stmt|;
return|return
name|super
operator|.
name|doEquals
argument_list|(
name|obj
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|format
argument_list|,
name|that
operator|.
name|format
argument_list|)
operator|&&
name|subsetSize
operator|==
name|that
operator|.
name|subsetSize
operator|&&
name|supersetSize
operator|==
name|that
operator|.
name|supersetSize
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|significanceHeuristic
argument_list|,
name|that
operator|.
name|significanceHeuristic
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|buckets
argument_list|,
name|that
operator|.
name|buckets
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|bucketMap
argument_list|,
name|that
operator|.
name|bucketMap
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|super
operator|.
name|doHashCode
argument_list|()
argument_list|,
name|format
argument_list|,
name|subsetSize
argument_list|,
name|supersetSize
argument_list|,
name|significanceHeuristic
argument_list|,
name|buckets
argument_list|,
name|bucketMap
argument_list|)
return|;
block|}
block|}
end_class

end_unit

