begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bench
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bench
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
name|search
operator|.
name|SearchRequest
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
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
name|XContentBuilderString
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
name|XContentHelper
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_comment
comment|/**  * Represents a single iteration of a search benchmark competition  */
end_comment

begin_class
DECL|class|CompetitionIteration
specifier|public
class|class
name|CompetitionIteration
implements|implements
name|Streamable
block|{
DECL|field|numQueries
specifier|private
name|long
name|numQueries
decl_stmt|;
DECL|field|slowRequests
specifier|private
name|SlowRequest
index|[]
name|slowRequests
decl_stmt|;
DECL|field|totalTime
specifier|private
name|long
name|totalTime
decl_stmt|;
DECL|field|sum
specifier|private
name|long
name|sum
decl_stmt|;
DECL|field|sumTotalHits
specifier|private
name|long
name|sumTotalHits
decl_stmt|;
DECL|field|stddev
specifier|private
name|double
name|stddev
decl_stmt|;
DECL|field|min
specifier|private
name|long
name|min
decl_stmt|;
DECL|field|max
specifier|private
name|long
name|max
decl_stmt|;
DECL|field|mean
specifier|private
name|double
name|mean
decl_stmt|;
DECL|field|qps
specifier|private
name|double
name|qps
decl_stmt|;
DECL|field|millisPerHit
specifier|private
name|double
name|millisPerHit
decl_stmt|;
DECL|field|percentiles
specifier|private
name|double
index|[]
name|percentiles
init|=
operator|new
name|double
index|[
literal|0
index|]
decl_stmt|;
DECL|field|percentileValues
specifier|private
name|Map
argument_list|<
name|Double
argument_list|,
name|Double
argument_list|>
name|percentileValues
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|iterationData
specifier|private
name|CompetitionIterationData
name|iterationData
decl_stmt|;
DECL|method|CompetitionIteration
specifier|public
name|CompetitionIteration
parameter_list|()
block|{ }
DECL|method|CompetitionIteration
specifier|public
name|CompetitionIteration
parameter_list|(
name|SlowRequest
index|[]
name|slowestRequests
parameter_list|,
name|long
name|totalTime
parameter_list|,
name|long
name|numQueries
parameter_list|,
name|long
name|sumTotalHits
parameter_list|,
name|CompetitionIterationData
name|iterationData
parameter_list|)
block|{
name|this
operator|.
name|totalTime
operator|=
name|totalTime
expr_stmt|;
name|this
operator|.
name|sumTotalHits
operator|=
name|sumTotalHits
expr_stmt|;
name|this
operator|.
name|slowRequests
operator|=
name|slowestRequests
expr_stmt|;
name|this
operator|.
name|numQueries
operator|=
name|numQueries
expr_stmt|;
name|this
operator|.
name|iterationData
operator|=
name|iterationData
expr_stmt|;
name|this
operator|.
name|millisPerHit
operator|=
name|totalTime
operator|/
operator|(
name|double
operator|)
name|sumTotalHits
expr_stmt|;
block|}
DECL|method|computeStatistics
specifier|public
name|void
name|computeStatistics
parameter_list|()
block|{
name|SinglePassStatistics
name|single
init|=
operator|new
name|SinglePassStatistics
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|datum
range|:
name|iterationData
operator|.
name|data
argument_list|()
control|)
block|{
if|if
condition|(
name|datum
operator|>
operator|-
literal|1
condition|)
block|{
comment|// ignore unset values in the underlying array
name|single
operator|.
name|push
argument_list|(
name|datum
argument_list|)
expr_stmt|;
block|}
block|}
name|sum
operator|=
name|single
operator|.
name|sum
argument_list|()
expr_stmt|;
name|stddev
operator|=
name|single
operator|.
name|stddev
argument_list|()
expr_stmt|;
name|min
operator|=
name|single
operator|.
name|min
argument_list|()
expr_stmt|;
name|max
operator|=
name|single
operator|.
name|max
argument_list|()
expr_stmt|;
name|mean
operator|=
name|single
operator|.
name|mean
argument_list|()
expr_stmt|;
name|qps
operator|=
name|numQueries
operator|*
operator|(
literal|1000.d
operator|/
operator|(
name|double
operator|)
name|sum
operator|)
expr_stmt|;
for|for
control|(
name|double
name|percentile
range|:
name|percentiles
control|)
block|{
name|percentileValues
operator|.
name|put
argument_list|(
name|percentile
argument_list|,
name|single
operator|.
name|percentile
argument_list|(
name|percentile
operator|/
literal|100
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|competitionIterationData
specifier|public
name|CompetitionIterationData
name|competitionIterationData
parameter_list|()
block|{
return|return
name|iterationData
return|;
block|}
DECL|method|numQueries
specifier|public
name|long
name|numQueries
parameter_list|()
block|{
return|return
name|numQueries
return|;
block|}
DECL|method|totalTime
specifier|public
name|long
name|totalTime
parameter_list|()
block|{
return|return
name|totalTime
return|;
block|}
DECL|method|sumTotalHits
specifier|public
name|long
name|sumTotalHits
parameter_list|()
block|{
return|return
name|sumTotalHits
return|;
block|}
DECL|method|millisPerHit
specifier|public
name|double
name|millisPerHit
parameter_list|()
block|{
return|return
name|millisPerHit
return|;
block|}
DECL|method|queriesPerSecond
specifier|public
name|double
name|queriesPerSecond
parameter_list|()
block|{
return|return
name|qps
return|;
block|}
DECL|method|slowRequests
specifier|public
name|SlowRequest
index|[]
name|slowRequests
parameter_list|()
block|{
return|return
name|slowRequests
return|;
block|}
DECL|method|min
specifier|public
name|long
name|min
parameter_list|()
block|{
return|return
name|min
return|;
block|}
DECL|method|max
specifier|public
name|long
name|max
parameter_list|()
block|{
return|return
name|max
return|;
block|}
DECL|method|mean
specifier|public
name|double
name|mean
parameter_list|()
block|{
return|return
name|mean
return|;
block|}
DECL|method|percentileValues
specifier|public
name|Map
argument_list|<
name|Double
argument_list|,
name|Double
argument_list|>
name|percentileValues
parameter_list|()
block|{
return|return
name|percentileValues
return|;
block|}
DECL|method|percentiles
specifier|public
name|void
name|percentiles
parameter_list|(
name|double
index|[]
name|percentiles
parameter_list|)
block|{
name|this
operator|.
name|percentiles
operator|=
name|percentiles
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|totalTime
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|sumTotalHits
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|numQueries
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|millisPerHit
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
name|iterationData
operator|=
name|in
operator|.
name|readOptionalStreamable
argument_list|(
operator|new
name|CompetitionIterationData
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|slowRequests
operator|=
operator|new
name|SlowRequest
index|[
name|size
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|slowRequests
index|[
name|i
index|]
operator|=
operator|new
name|SlowRequest
argument_list|()
expr_stmt|;
name|slowRequests
index|[
name|i
index|]
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|percentiles
operator|=
name|in
operator|.
name|readDoubleArray
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVLong
argument_list|(
name|totalTime
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|sumTotalHits
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|numQueries
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|millisPerHit
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalStreamable
argument_list|(
name|iterationData
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|slowRequests
operator|==
literal|null
condition|?
literal|0
else|:
name|slowRequests
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|slowRequests
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|SlowRequest
name|slowRequest
range|:
name|slowRequests
control|)
block|{
name|slowRequest
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|writeDoubleArray
argument_list|(
name|percentiles
argument_list|)
expr_stmt|;
block|}
comment|/**      * Represents a 'slow' search request      */
DECL|class|SlowRequest
specifier|public
specifier|static
class|class
name|SlowRequest
implements|implements
name|ToXContent
implements|,
name|Streamable
block|{
DECL|field|maxTimeTaken
specifier|private
name|long
name|maxTimeTaken
decl_stmt|;
DECL|field|avgTimeTaken
specifier|private
name|long
name|avgTimeTaken
decl_stmt|;
DECL|field|searchRequest
specifier|private
name|SearchRequest
name|searchRequest
decl_stmt|;
DECL|method|SlowRequest
specifier|public
name|SlowRequest
parameter_list|()
block|{ }
DECL|method|SlowRequest
specifier|public
name|SlowRequest
parameter_list|(
name|long
name|avgTimeTaken
parameter_list|,
name|long
name|maxTimeTaken
parameter_list|,
name|SearchRequest
name|searchRequest
parameter_list|)
block|{
name|this
operator|.
name|avgTimeTaken
operator|=
name|avgTimeTaken
expr_stmt|;
name|this
operator|.
name|maxTimeTaken
operator|=
name|maxTimeTaken
expr_stmt|;
name|this
operator|.
name|searchRequest
operator|=
name|searchRequest
expr_stmt|;
block|}
DECL|method|avgTimeTaken
specifier|public
name|long
name|avgTimeTaken
parameter_list|()
block|{
return|return
name|avgTimeTaken
return|;
block|}
DECL|method|maxTimeTaken
specifier|public
name|long
name|maxTimeTaken
parameter_list|()
block|{
return|return
name|maxTimeTaken
return|;
block|}
DECL|method|searchRequest
specifier|public
name|SearchRequest
name|searchRequest
parameter_list|()
block|{
return|return
name|searchRequest
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|avgTimeTaken
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|maxTimeTaken
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|searchRequest
operator|=
operator|new
name|SearchRequest
argument_list|()
expr_stmt|;
name|searchRequest
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeVLong
argument_list|(
name|avgTimeTaken
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|maxTimeTaken
argument_list|)
expr_stmt|;
name|searchRequest
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MAX_TIME
argument_list|,
name|maxTimeTaken
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|AVG_TIME
argument_list|,
name|avgTimeTaken
argument_list|)
expr_stmt|;
name|XContentHelper
operator|.
name|writeRawField
argument_list|(
literal|"request"
argument_list|,
name|searchRequest
operator|.
name|source
argument_list|()
argument_list|,
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|MAX_TIME
specifier|static
specifier|final
name|XContentBuilderString
name|MAX_TIME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"max_time"
argument_list|)
decl_stmt|;
DECL|field|AVG_TIME
specifier|static
specifier|final
name|XContentBuilderString
name|AVG_TIME
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"avg_time"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

