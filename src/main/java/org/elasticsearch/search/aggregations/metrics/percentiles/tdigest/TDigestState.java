begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/* * Licensed to the Apache Software Foundation (ASF) under one or more * contributor license agreements. See the NOTICE file distributed with * this work for additional information regarding copyright ownership. * The ASF licenses this file to You under the Apache License, Version 2.0 * (the "License"); you may not use this file except in compliance with * the License. You may obtain a copy of the License at * * http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.percentiles.tdigest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|percentiles
operator|.
name|tdigest
package|;
end_package

begin_import
import|import
name|com
operator|.
name|tdunning
operator|.
name|math
operator|.
name|stats
operator|.
name|AVLTreeDigest
import|;
end_import

begin_import
import|import
name|com
operator|.
name|tdunning
operator|.
name|math
operator|.
name|stats
operator|.
name|Centroid
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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Extension of {@link TDigest} with custom serialization.  */
end_comment

begin_class
DECL|class|TDigestState
specifier|public
class|class
name|TDigestState
extends|extends
name|AVLTreeDigest
block|{
DECL|field|compression
specifier|private
specifier|final
name|double
name|compression
decl_stmt|;
DECL|method|TDigestState
specifier|public
name|TDigestState
parameter_list|(
name|double
name|compression
parameter_list|)
block|{
name|super
argument_list|(
name|compression
argument_list|)
expr_stmt|;
name|this
operator|.
name|compression
operator|=
name|compression
expr_stmt|;
block|}
DECL|method|write
specifier|public
specifier|static
name|void
name|write
parameter_list|(
name|TDigestState
name|state
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeDouble
argument_list|(
name|state
operator|.
name|compression
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|state
operator|.
name|centroidCount
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Centroid
name|centroid
range|:
name|state
operator|.
name|centroids
argument_list|()
control|)
block|{
name|out
operator|.
name|writeDouble
argument_list|(
name|centroid
operator|.
name|mean
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|centroid
operator|.
name|count
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|read
specifier|public
specifier|static
name|TDigestState
name|read
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|double
name|compression
init|=
name|in
operator|.
name|readDouble
argument_list|()
decl_stmt|;
name|TDigestState
name|state
init|=
operator|new
name|TDigestState
argument_list|(
name|compression
argument_list|)
decl_stmt|;
name|int
name|n
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
name|n
condition|;
name|i
operator|++
control|)
block|{
name|state
operator|.
name|add
argument_list|(
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|in
operator|.
name|readVInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|state
return|;
block|}
block|}
end_class

end_unit

