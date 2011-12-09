begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.dfs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|dfs
package|;
end_package

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|impl
operator|.
name|Constants
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|iterator
operator|.
name|TObjectIntIterator
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|map
operator|.
name|hash
operator|.
name|TObjectIntHashMap
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
name|Term
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
name|trove
operator|.
name|ExtTObjectIntHasMap
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
DECL|class|AggregatedDfs
specifier|public
class|class
name|AggregatedDfs
implements|implements
name|Streamable
block|{
DECL|field|dfMap
specifier|private
name|TObjectIntHashMap
argument_list|<
name|Term
argument_list|>
name|dfMap
decl_stmt|;
DECL|field|maxDoc
specifier|private
name|long
name|maxDoc
decl_stmt|;
DECL|method|AggregatedDfs
specifier|private
name|AggregatedDfs
parameter_list|()
block|{      }
DECL|method|AggregatedDfs
specifier|public
name|AggregatedDfs
parameter_list|(
name|TObjectIntHashMap
argument_list|<
name|Term
argument_list|>
name|dfMap
parameter_list|,
name|long
name|maxDoc
parameter_list|)
block|{
name|this
operator|.
name|dfMap
operator|=
name|dfMap
expr_stmt|;
name|this
operator|.
name|maxDoc
operator|=
name|maxDoc
expr_stmt|;
block|}
DECL|method|dfMap
specifier|public
name|TObjectIntHashMap
argument_list|<
name|Term
argument_list|>
name|dfMap
parameter_list|()
block|{
return|return
name|dfMap
return|;
block|}
DECL|method|maxDoc
specifier|public
name|long
name|maxDoc
parameter_list|()
block|{
return|return
name|maxDoc
return|;
block|}
DECL|method|readAggregatedDfs
specifier|public
specifier|static
name|AggregatedDfs
name|readAggregatedDfs
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|AggregatedDfs
name|result
init|=
operator|new
name|AggregatedDfs
argument_list|()
decl_stmt|;
name|result
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|result
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
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|dfMap
operator|=
operator|new
name|ExtTObjectIntHasMap
argument_list|<
name|Term
argument_list|>
argument_list|(
name|size
argument_list|,
name|Constants
operator|.
name|DEFAULT_LOAD_FACTOR
argument_list|,
operator|-
literal|1
argument_list|)
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
name|dfMap
operator|.
name|put
argument_list|(
operator|new
name|Term
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|,
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
argument_list|,
name|in
operator|.
name|readVInt
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|maxDoc
operator|=
name|in
operator|.
name|readVLong
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
specifier|final
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
name|dfMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|TObjectIntIterator
argument_list|<
name|Term
argument_list|>
name|it
init|=
name|dfMap
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|it
operator|.
name|advance
argument_list|()
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|it
operator|.
name|key
argument_list|()
operator|.
name|field
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|it
operator|.
name|key
argument_list|()
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|it
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeVLong
argument_list|(
name|maxDoc
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

