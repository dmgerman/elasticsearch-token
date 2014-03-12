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
name|ActionResponse
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
name|Arrays
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
name|ArrayList
import|;
end_import

begin_comment
comment|/**  * Benchmark status response  */
end_comment

begin_class
DECL|class|BenchmarkStatusResponse
specifier|public
class|class
name|BenchmarkStatusResponse
extends|extends
name|ActionResponse
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|nodeName
specifier|private
name|String
name|nodeName
decl_stmt|;
DECL|field|benchmarkResponses
specifier|private
specifier|final
name|List
argument_list|<
name|BenchmarkResponse
argument_list|>
name|benchmarkResponses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|errorMessages
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|errorMessages
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|BenchmarkStatusResponse
specifier|public
name|BenchmarkStatusResponse
parameter_list|()
block|{ }
DECL|method|BenchmarkStatusResponse
specifier|public
name|BenchmarkStatusResponse
parameter_list|(
name|String
name|nodeName
parameter_list|)
block|{
name|this
operator|.
name|nodeName
operator|=
name|nodeName
expr_stmt|;
block|}
DECL|method|addBenchResponse
specifier|public
name|void
name|addBenchResponse
parameter_list|(
name|BenchmarkResponse
name|response
parameter_list|)
block|{
name|benchmarkResponses
operator|.
name|add
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
DECL|method|benchmarkResponses
specifier|public
name|List
argument_list|<
name|BenchmarkResponse
argument_list|>
name|benchmarkResponses
parameter_list|()
block|{
return|return
name|benchmarkResponses
return|;
block|}
DECL|method|nodeName
specifier|public
name|String
name|nodeName
parameter_list|(
name|String
name|nodeName
parameter_list|)
block|{
name|this
operator|.
name|nodeName
operator|=
name|nodeName
expr_stmt|;
return|return
name|nodeName
return|;
block|}
DECL|method|nodeName
specifier|public
name|String
name|nodeName
parameter_list|()
block|{
return|return
name|nodeName
return|;
block|}
DECL|method|addErrors
specifier|public
name|void
name|addErrors
parameter_list|(
name|List
argument_list|<
name|String
argument_list|>
name|errorMessages
parameter_list|)
block|{
name|this
operator|.
name|errorMessages
operator|.
name|addAll
argument_list|(
name|errorMessages
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
if|if
condition|(
name|errorMessages
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
literal|"errors"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|error
range|:
name|errorMessages
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|error
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|benchmarkResponses
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
literal|"active_benchmarks"
argument_list|)
expr_stmt|;
for|for
control|(
name|BenchmarkResponse
name|benchmarkResponse
range|:
name|benchmarkResponses
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|benchmarkResponse
operator|.
name|benchmarkName
argument_list|()
argument_list|)
expr_stmt|;
name|benchmarkResponse
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|nodeName
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|BenchmarkResponse
name|br
init|=
operator|new
name|BenchmarkResponse
argument_list|()
decl_stmt|;
name|br
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|benchmarkResponses
operator|.
name|add
argument_list|(
name|br
argument_list|)
expr_stmt|;
block|}
name|errorMessages
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|in
operator|.
name|readStringArray
argument_list|()
argument_list|)
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|nodeName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|benchmarkResponses
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|BenchmarkResponse
name|br
range|:
name|benchmarkResponses
control|)
block|{
name|br
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeStringArray
argument_list|(
name|errorMessages
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|errorMessages
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

