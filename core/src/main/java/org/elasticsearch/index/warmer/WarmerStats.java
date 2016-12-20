begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.warmer
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|warmer
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
name|unit
operator|.
name|TimeValue
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

begin_class
DECL|class|WarmerStats
specifier|public
class|class
name|WarmerStats
implements|implements
name|Streamable
implements|,
name|ToXContent
block|{
DECL|field|current
specifier|private
name|long
name|current
decl_stmt|;
DECL|field|total
specifier|private
name|long
name|total
decl_stmt|;
DECL|field|totalTimeInMillis
specifier|private
name|long
name|totalTimeInMillis
decl_stmt|;
DECL|method|WarmerStats
specifier|public
name|WarmerStats
parameter_list|()
block|{      }
DECL|method|WarmerStats
specifier|public
name|WarmerStats
parameter_list|(
name|long
name|current
parameter_list|,
name|long
name|total
parameter_list|,
name|long
name|totalTimeInMillis
parameter_list|)
block|{
name|this
operator|.
name|current
operator|=
name|current
expr_stmt|;
name|this
operator|.
name|total
operator|=
name|total
expr_stmt|;
name|this
operator|.
name|totalTimeInMillis
operator|=
name|totalTimeInMillis
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|long
name|current
parameter_list|,
name|long
name|total
parameter_list|,
name|long
name|totalTimeInMillis
parameter_list|)
block|{
name|this
operator|.
name|current
operator|+=
name|current
expr_stmt|;
name|this
operator|.
name|total
operator|+=
name|total
expr_stmt|;
name|this
operator|.
name|totalTimeInMillis
operator|+=
name|totalTimeInMillis
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|WarmerStats
name|warmerStats
parameter_list|)
block|{
if|if
condition|(
name|warmerStats
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|current
operator|+=
name|warmerStats
operator|.
name|current
expr_stmt|;
name|this
operator|.
name|total
operator|+=
name|warmerStats
operator|.
name|total
expr_stmt|;
name|this
operator|.
name|totalTimeInMillis
operator|+=
name|warmerStats
operator|.
name|totalTimeInMillis
expr_stmt|;
block|}
DECL|method|current
specifier|public
name|long
name|current
parameter_list|()
block|{
return|return
name|this
operator|.
name|current
return|;
block|}
comment|/**      * The total number of warmer executed.      */
DECL|method|total
specifier|public
name|long
name|total
parameter_list|()
block|{
return|return
name|this
operator|.
name|total
return|;
block|}
comment|/**      * The total time warmer have been executed (in milliseconds).      */
DECL|method|totalTimeInMillis
specifier|public
name|long
name|totalTimeInMillis
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalTimeInMillis
return|;
block|}
comment|/**      * The total time warmer have been executed.      */
DECL|method|totalTime
specifier|public
name|TimeValue
name|totalTime
parameter_list|()
block|{
return|return
operator|new
name|TimeValue
argument_list|(
name|totalTimeInMillis
argument_list|)
return|;
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
name|Fields
operator|.
name|WARMER
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|CURRENT
argument_list|,
name|current
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOTAL
argument_list|,
name|total
argument_list|)
expr_stmt|;
name|builder
operator|.
name|timeValueField
argument_list|(
name|Fields
operator|.
name|TOTAL_TIME_IN_MILLIS
argument_list|,
name|Fields
operator|.
name|TOTAL_TIME
argument_list|,
name|totalTimeInMillis
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|WARMER
specifier|static
specifier|final
name|String
name|WARMER
init|=
literal|"warmer"
decl_stmt|;
DECL|field|CURRENT
specifier|static
specifier|final
name|String
name|CURRENT
init|=
literal|"current"
decl_stmt|;
DECL|field|TOTAL
specifier|static
specifier|final
name|String
name|TOTAL
init|=
literal|"total"
decl_stmt|;
DECL|field|TOTAL_TIME
specifier|static
specifier|final
name|String
name|TOTAL_TIME
init|=
literal|"total_time"
decl_stmt|;
DECL|field|TOTAL_TIME_IN_MILLIS
specifier|static
specifier|final
name|String
name|TOTAL_TIME_IN_MILLIS
init|=
literal|"total_time_in_millis"
decl_stmt|;
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
name|current
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|total
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|totalTimeInMillis
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
name|current
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|total
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|totalTimeInMillis
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

