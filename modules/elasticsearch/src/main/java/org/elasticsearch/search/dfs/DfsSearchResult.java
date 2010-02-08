begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|search
operator|.
name|SearchShardTarget
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
operator|.
name|Streamable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchShardTarget
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|DfsSearchResult
specifier|public
class|class
name|DfsSearchResult
implements|implements
name|Streamable
block|{
DECL|field|EMPTY_TERMS
specifier|private
specifier|static
name|Term
index|[]
name|EMPTY_TERMS
init|=
operator|new
name|Term
index|[
literal|0
index|]
decl_stmt|;
DECL|field|EMPTY_FREQS
specifier|private
specifier|static
name|int
index|[]
name|EMPTY_FREQS
init|=
operator|new
name|int
index|[
literal|0
index|]
decl_stmt|;
DECL|field|shardTarget
specifier|private
name|SearchShardTarget
name|shardTarget
decl_stmt|;
DECL|field|id
specifier|private
name|long
name|id
decl_stmt|;
DECL|field|terms
specifier|private
name|Term
index|[]
name|terms
decl_stmt|;
DECL|field|freqs
specifier|private
name|int
index|[]
name|freqs
decl_stmt|;
DECL|field|numDocs
specifier|private
name|int
name|numDocs
decl_stmt|;
DECL|method|DfsSearchResult
specifier|public
name|DfsSearchResult
parameter_list|()
block|{      }
DECL|method|DfsSearchResult
specifier|public
name|DfsSearchResult
parameter_list|(
name|long
name|id
parameter_list|,
name|SearchShardTarget
name|shardTarget
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|shardTarget
operator|=
name|shardTarget
expr_stmt|;
block|}
DECL|method|id
specifier|public
name|long
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
DECL|method|shardTarget
specifier|public
name|SearchShardTarget
name|shardTarget
parameter_list|()
block|{
return|return
name|shardTarget
return|;
block|}
DECL|method|numDocs
specifier|public
name|DfsSearchResult
name|numDocs
parameter_list|(
name|int
name|numDocs
parameter_list|)
block|{
name|this
operator|.
name|numDocs
operator|=
name|numDocs
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|numDocs
specifier|public
name|int
name|numDocs
parameter_list|()
block|{
return|return
name|numDocs
return|;
block|}
DECL|method|termsAndFreqs
specifier|public
name|DfsSearchResult
name|termsAndFreqs
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
name|int
index|[]
name|freqs
parameter_list|)
block|{
name|this
operator|.
name|terms
operator|=
name|terms
expr_stmt|;
name|this
operator|.
name|freqs
operator|=
name|freqs
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|terms
specifier|public
name|Term
index|[]
name|terms
parameter_list|()
block|{
return|return
name|terms
return|;
block|}
DECL|method|freqs
specifier|public
name|int
index|[]
name|freqs
parameter_list|()
block|{
return|return
name|freqs
return|;
block|}
DECL|method|readDfsSearchResult
specifier|public
specifier|static
name|DfsSearchResult
name|readDfsSearchResult
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|DfsSearchResult
name|result
init|=
operator|new
name|DfsSearchResult
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
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|id
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|shardTarget
operator|=
name|readSearchShardTarget
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|int
name|termsSize
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|termsSize
operator|==
literal|0
condition|)
block|{
name|terms
operator|=
name|EMPTY_TERMS
expr_stmt|;
block|}
else|else
block|{
name|terms
operator|=
operator|new
name|Term
index|[
name|termsSize
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
name|terms
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|terms
index|[
name|i
index|]
operator|=
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
expr_stmt|;
block|}
block|}
name|int
name|freqsSize
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|freqsSize
operator|==
literal|0
condition|)
block|{
name|freqs
operator|=
name|EMPTY_FREQS
expr_stmt|;
block|}
else|else
block|{
name|freqs
operator|=
operator|new
name|int
index|[
name|freqsSize
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
name|freqs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|freqs
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
block|}
name|numDocs
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|id
argument_list|)
expr_stmt|;
name|shardTarget
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|terms
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Term
name|term
range|:
name|terms
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|term
operator|.
name|field
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|freqs
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|freq
range|:
name|freqs
control|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|freq
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

