begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket
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
name|ImmutableMap
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
name|bytes
operator|.
name|BytesReference
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
name|collect
operator|.
name|MapBuilder
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|BucketStreams
specifier|public
class|class
name|BucketStreams
block|{
DECL|field|STREAMS
specifier|private
specifier|static
name|ImmutableMap
argument_list|<
name|BytesReference
argument_list|,
name|Stream
argument_list|>
name|STREAMS
init|=
name|ImmutableMap
operator|.
name|of
argument_list|()
decl_stmt|;
comment|/**      * A stream that knows how to read a bucket from the input.      */
DECL|interface|Stream
specifier|public
specifier|static
interface|interface
name|Stream
parameter_list|<
name|B
extends|extends
name|MultiBucketsAggregation
operator|.
name|Bucket
parameter_list|>
block|{
DECL|method|readResult
name|B
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|,
name|BucketStreamContext
name|context
parameter_list|)
throws|throws
name|IOException
function_decl|;
DECL|method|getBucketStreamContext
name|BucketStreamContext
name|getBucketStreamContext
parameter_list|(
name|B
name|bucket
parameter_list|)
function_decl|;
block|}
comment|/**      * Registers the given stream and associate it with the given types.      *      * @param stream    The streams to register      * @param types     The types associated with the streams      */
DECL|method|registerStream
specifier|public
specifier|static
specifier|synchronized
name|void
name|registerStream
parameter_list|(
name|Stream
name|stream
parameter_list|,
name|BytesReference
modifier|...
name|types
parameter_list|)
block|{
name|MapBuilder
argument_list|<
name|BytesReference
argument_list|,
name|Stream
argument_list|>
name|uStreams
init|=
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|(
name|STREAMS
argument_list|)
decl_stmt|;
for|for
control|(
name|BytesReference
name|type
range|:
name|types
control|)
block|{
name|uStreams
operator|.
name|put
argument_list|(
name|type
argument_list|,
name|stream
argument_list|)
expr_stmt|;
block|}
name|STREAMS
operator|=
name|uStreams
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
comment|/**      * Returns the stream that is registered for the given type      *      * @param   type The given type      * @return  The associated stream      */
DECL|method|stream
specifier|public
specifier|static
name|Stream
name|stream
parameter_list|(
name|BytesReference
name|type
parameter_list|)
block|{
return|return
name|STREAMS
operator|.
name|get
argument_list|(
name|type
argument_list|)
return|;
block|}
block|}
end_class

end_unit
