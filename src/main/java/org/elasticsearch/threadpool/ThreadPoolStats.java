begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.threadpool
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
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
name|Iterator
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ThreadPoolStats
specifier|public
class|class
name|ThreadPoolStats
implements|implements
name|Streamable
implements|,
name|ToXContent
implements|,
name|Iterable
argument_list|<
name|ThreadPoolStats
operator|.
name|Stats
argument_list|>
block|{
DECL|class|Stats
specifier|public
specifier|static
class|class
name|Stats
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|threads
specifier|private
name|int
name|threads
decl_stmt|;
DECL|field|queue
specifier|private
name|int
name|queue
decl_stmt|;
DECL|field|active
specifier|private
name|int
name|active
decl_stmt|;
DECL|field|rejected
specifier|private
name|long
name|rejected
decl_stmt|;
DECL|field|largest
specifier|private
name|int
name|largest
decl_stmt|;
DECL|field|completed
specifier|private
name|long
name|completed
decl_stmt|;
DECL|method|Stats
name|Stats
parameter_list|()
block|{          }
DECL|method|Stats
specifier|public
name|Stats
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|threads
parameter_list|,
name|int
name|queue
parameter_list|,
name|int
name|active
parameter_list|,
name|long
name|rejected
parameter_list|,
name|int
name|largest
parameter_list|,
name|long
name|completed
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|threads
operator|=
name|threads
expr_stmt|;
name|this
operator|.
name|queue
operator|=
name|queue
expr_stmt|;
name|this
operator|.
name|active
operator|=
name|active
expr_stmt|;
name|this
operator|.
name|rejected
operator|=
name|rejected
expr_stmt|;
name|this
operator|.
name|largest
operator|=
name|largest
expr_stmt|;
name|this
operator|.
name|completed
operator|=
name|completed
expr_stmt|;
block|}
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
DECL|method|getThreads
specifier|public
name|int
name|getThreads
parameter_list|()
block|{
return|return
name|this
operator|.
name|threads
return|;
block|}
DECL|method|getQueue
specifier|public
name|int
name|getQueue
parameter_list|()
block|{
return|return
name|this
operator|.
name|queue
return|;
block|}
DECL|method|getActive
specifier|public
name|int
name|getActive
parameter_list|()
block|{
return|return
name|this
operator|.
name|active
return|;
block|}
DECL|method|getRejected
specifier|public
name|long
name|getRejected
parameter_list|()
block|{
return|return
name|rejected
return|;
block|}
DECL|method|getLargest
specifier|public
name|int
name|getLargest
parameter_list|()
block|{
return|return
name|largest
return|;
block|}
DECL|method|getCompleted
specifier|public
name|long
name|getCompleted
parameter_list|()
block|{
return|return
name|this
operator|.
name|completed
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
name|name
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
name|threads
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|queue
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|active
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|rejected
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|largest
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|completed
operator|=
name|in
operator|.
name|readLong
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
name|writeString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|threads
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|queue
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|active
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|rejected
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|largest
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|completed
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
name|startObject
argument_list|(
name|name
argument_list|,
name|XContentBuilder
operator|.
name|FieldCaseConversion
operator|.
name|NONE
argument_list|)
expr_stmt|;
if|if
condition|(
name|threads
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|THREADS
argument_list|,
name|threads
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|queue
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|QUEUE
argument_list|,
name|queue
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|active
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|ACTIVE
argument_list|,
name|active
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|rejected
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|REJECTED
argument_list|,
name|rejected
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|largest
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|LARGEST
argument_list|,
name|largest
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|completed
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|COMPLETED
argument_list|,
name|completed
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
DECL|field|stats
specifier|private
name|List
argument_list|<
name|Stats
argument_list|>
name|stats
decl_stmt|;
DECL|method|ThreadPoolStats
name|ThreadPoolStats
parameter_list|()
block|{      }
DECL|method|ThreadPoolStats
specifier|public
name|ThreadPoolStats
parameter_list|(
name|List
argument_list|<
name|Stats
argument_list|>
name|stats
parameter_list|)
block|{
name|this
operator|.
name|stats
operator|=
name|stats
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Stats
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|stats
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|readThreadPoolStats
specifier|public
specifier|static
name|ThreadPoolStats
name|readThreadPoolStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ThreadPoolStats
name|stats
init|=
operator|new
name|ThreadPoolStats
argument_list|()
decl_stmt|;
name|stats
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|stats
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
name|stats
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|size
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
name|Stats
name|stats1
init|=
operator|new
name|Stats
argument_list|()
decl_stmt|;
name|stats1
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|stats
operator|.
name|add
argument_list|(
name|stats1
argument_list|)
expr_stmt|;
block|}
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
name|stats
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Stats
name|stat
range|:
name|stats
control|)
block|{
name|stat
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|THREAD_POOL
specifier|static
specifier|final
name|XContentBuilderString
name|THREAD_POOL
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"thread_pool"
argument_list|)
decl_stmt|;
DECL|field|THREADS
specifier|static
specifier|final
name|XContentBuilderString
name|THREADS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"threads"
argument_list|)
decl_stmt|;
DECL|field|QUEUE
specifier|static
specifier|final
name|XContentBuilderString
name|QUEUE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"queue"
argument_list|)
decl_stmt|;
DECL|field|ACTIVE
specifier|static
specifier|final
name|XContentBuilderString
name|ACTIVE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"active"
argument_list|)
decl_stmt|;
DECL|field|REJECTED
specifier|static
specifier|final
name|XContentBuilderString
name|REJECTED
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"rejected"
argument_list|)
decl_stmt|;
DECL|field|LARGEST
specifier|static
specifier|final
name|XContentBuilderString
name|LARGEST
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"largest"
argument_list|)
decl_stmt|;
DECL|field|COMPLETED
specifier|static
specifier|final
name|XContentBuilderString
name|COMPLETED
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"completed"
argument_list|)
decl_stmt|;
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
name|ToXContent
operator|.
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|Fields
operator|.
name|THREAD_POOL
argument_list|)
expr_stmt|;
for|for
control|(
name|Stats
name|stat
range|:
name|stats
control|)
block|{
name|stat
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
block|}
end_class

end_unit

