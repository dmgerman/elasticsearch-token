begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.profile
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|profile
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
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|profile
operator|.
name|aggregation
operator|.
name|AggregationProfileShardResult
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
name|profile
operator|.
name|query
operator|.
name|QueryProfileShardResult
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
name|ArrayList
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
name|List
import|;
end_import

begin_class
DECL|class|ProfileShardResult
specifier|public
class|class
name|ProfileShardResult
implements|implements
name|Writeable
block|{
DECL|field|queryProfileResults
specifier|private
specifier|final
name|List
argument_list|<
name|QueryProfileShardResult
argument_list|>
name|queryProfileResults
decl_stmt|;
DECL|field|aggProfileShardResult
specifier|private
specifier|final
name|AggregationProfileShardResult
name|aggProfileShardResult
decl_stmt|;
DECL|method|ProfileShardResult
specifier|public
name|ProfileShardResult
parameter_list|(
name|List
argument_list|<
name|QueryProfileShardResult
argument_list|>
name|queryProfileResults
parameter_list|,
name|AggregationProfileShardResult
name|aggProfileShardResult
parameter_list|)
block|{
name|this
operator|.
name|aggProfileShardResult
operator|=
name|aggProfileShardResult
expr_stmt|;
name|this
operator|.
name|queryProfileResults
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|queryProfileResults
argument_list|)
expr_stmt|;
block|}
DECL|method|ProfileShardResult
specifier|public
name|ProfileShardResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|profileSize
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|QueryProfileShardResult
argument_list|>
name|queryProfileResults
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|profileSize
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|profileSize
condition|;
name|i
operator|++
control|)
block|{
name|QueryProfileShardResult
name|result
init|=
operator|new
name|QueryProfileShardResult
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|queryProfileResults
operator|.
name|add
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|queryProfileResults
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|queryProfileResults
argument_list|)
expr_stmt|;
name|this
operator|.
name|aggProfileShardResult
operator|=
operator|new
name|AggregationProfileShardResult
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
name|writeVInt
argument_list|(
name|queryProfileResults
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|QueryProfileShardResult
name|queryShardResult
range|:
name|queryProfileResults
control|)
block|{
name|queryShardResult
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|aggProfileShardResult
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
DECL|method|getQueryProfileResults
specifier|public
name|List
argument_list|<
name|QueryProfileShardResult
argument_list|>
name|getQueryProfileResults
parameter_list|()
block|{
return|return
name|queryProfileResults
return|;
block|}
DECL|method|getAggregationProfileResults
specifier|public
name|AggregationProfileShardResult
name|getAggregationProfileResults
parameter_list|()
block|{
return|return
name|aggProfileShardResult
return|;
block|}
block|}
end_class

end_unit

