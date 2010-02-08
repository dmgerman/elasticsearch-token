begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
package|;
end_package

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
name|search
operator|.
name|internal
operator|.
name|InternalSearchHits
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|InternalSearchHits
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|FetchSearchResult
specifier|public
class|class
name|FetchSearchResult
implements|implements
name|Streamable
implements|,
name|FetchSearchResultProvider
block|{
DECL|field|id
specifier|private
name|long
name|id
decl_stmt|;
DECL|field|shardTarget
specifier|private
name|SearchShardTarget
name|shardTarget
decl_stmt|;
DECL|field|hits
specifier|private
name|InternalSearchHits
name|hits
decl_stmt|;
comment|// client side counter
DECL|field|counter
specifier|private
specifier|transient
name|int
name|counter
decl_stmt|;
DECL|method|FetchSearchResult
specifier|public
name|FetchSearchResult
parameter_list|()
block|{      }
DECL|method|FetchSearchResult
specifier|public
name|FetchSearchResult
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
DECL|method|fetchResult
annotation|@
name|Override
specifier|public
name|FetchSearchResult
name|fetchResult
parameter_list|()
block|{
return|return
name|this
return|;
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
name|this
operator|.
name|shardTarget
return|;
block|}
DECL|method|hits
specifier|public
name|void
name|hits
parameter_list|(
name|InternalSearchHits
name|hits
parameter_list|)
block|{
name|this
operator|.
name|hits
operator|=
name|hits
expr_stmt|;
block|}
DECL|method|hits
specifier|public
name|InternalSearchHits
name|hits
parameter_list|()
block|{
return|return
name|hits
return|;
block|}
DECL|method|initCounter
specifier|public
name|FetchSearchResult
name|initCounter
parameter_list|()
block|{
name|counter
operator|=
literal|0
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|counterGetAndIncrement
specifier|public
name|int
name|counterGetAndIncrement
parameter_list|()
block|{
return|return
name|counter
operator|++
return|;
block|}
DECL|method|readFetchSearchResult
specifier|public
specifier|static
name|FetchSearchResult
name|readFetchSearchResult
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
throws|,
name|ClassNotFoundException
block|{
name|FetchSearchResult
name|result
init|=
operator|new
name|FetchSearchResult
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
name|hits
operator|=
name|readSearchHits
argument_list|(
name|in
argument_list|)
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
name|hits
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

