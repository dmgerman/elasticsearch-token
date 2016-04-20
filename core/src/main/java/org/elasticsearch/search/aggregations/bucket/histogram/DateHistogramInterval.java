begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.histogram
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
operator|.
name|histogram
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
name|Objects
import|;
end_import

begin_comment
comment|/**  * The interval the date histogram is based on.  */
end_comment

begin_class
DECL|class|DateHistogramInterval
specifier|public
class|class
name|DateHistogramInterval
implements|implements
name|Writeable
argument_list|<
name|DateHistogramInterval
argument_list|>
block|{
DECL|field|SECOND
specifier|public
specifier|static
specifier|final
name|DateHistogramInterval
name|SECOND
init|=
operator|new
name|DateHistogramInterval
argument_list|(
literal|"1s"
argument_list|)
decl_stmt|;
DECL|field|MINUTE
specifier|public
specifier|static
specifier|final
name|DateHistogramInterval
name|MINUTE
init|=
operator|new
name|DateHistogramInterval
argument_list|(
literal|"1m"
argument_list|)
decl_stmt|;
DECL|field|HOUR
specifier|public
specifier|static
specifier|final
name|DateHistogramInterval
name|HOUR
init|=
operator|new
name|DateHistogramInterval
argument_list|(
literal|"1h"
argument_list|)
decl_stmt|;
DECL|field|DAY
specifier|public
specifier|static
specifier|final
name|DateHistogramInterval
name|DAY
init|=
operator|new
name|DateHistogramInterval
argument_list|(
literal|"1d"
argument_list|)
decl_stmt|;
DECL|field|WEEK
specifier|public
specifier|static
specifier|final
name|DateHistogramInterval
name|WEEK
init|=
operator|new
name|DateHistogramInterval
argument_list|(
literal|"1w"
argument_list|)
decl_stmt|;
DECL|field|MONTH
specifier|public
specifier|static
specifier|final
name|DateHistogramInterval
name|MONTH
init|=
operator|new
name|DateHistogramInterval
argument_list|(
literal|"1M"
argument_list|)
decl_stmt|;
DECL|field|QUARTER
specifier|public
specifier|static
specifier|final
name|DateHistogramInterval
name|QUARTER
init|=
operator|new
name|DateHistogramInterval
argument_list|(
literal|"1q"
argument_list|)
decl_stmt|;
DECL|field|YEAR
specifier|public
specifier|static
specifier|final
name|DateHistogramInterval
name|YEAR
init|=
operator|new
name|DateHistogramInterval
argument_list|(
literal|"1y"
argument_list|)
decl_stmt|;
DECL|method|seconds
specifier|public
specifier|static
name|DateHistogramInterval
name|seconds
parameter_list|(
name|int
name|sec
parameter_list|)
block|{
return|return
operator|new
name|DateHistogramInterval
argument_list|(
name|sec
operator|+
literal|"s"
argument_list|)
return|;
block|}
DECL|method|minutes
specifier|public
specifier|static
name|DateHistogramInterval
name|minutes
parameter_list|(
name|int
name|min
parameter_list|)
block|{
return|return
operator|new
name|DateHistogramInterval
argument_list|(
name|min
operator|+
literal|"m"
argument_list|)
return|;
block|}
DECL|method|hours
specifier|public
specifier|static
name|DateHistogramInterval
name|hours
parameter_list|(
name|int
name|hours
parameter_list|)
block|{
return|return
operator|new
name|DateHistogramInterval
argument_list|(
name|hours
operator|+
literal|"h"
argument_list|)
return|;
block|}
DECL|method|days
specifier|public
specifier|static
name|DateHistogramInterval
name|days
parameter_list|(
name|int
name|days
parameter_list|)
block|{
return|return
operator|new
name|DateHistogramInterval
argument_list|(
name|days
operator|+
literal|"d"
argument_list|)
return|;
block|}
DECL|method|weeks
specifier|public
specifier|static
name|DateHistogramInterval
name|weeks
parameter_list|(
name|int
name|weeks
parameter_list|)
block|{
return|return
operator|new
name|DateHistogramInterval
argument_list|(
name|weeks
operator|+
literal|"w"
argument_list|)
return|;
block|}
DECL|field|expression
specifier|private
specifier|final
name|String
name|expression
decl_stmt|;
DECL|method|DateHistogramInterval
specifier|public
name|DateHistogramInterval
parameter_list|(
name|String
name|expression
parameter_list|)
block|{
name|this
operator|.
name|expression
operator|=
name|expression
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|DateHistogramInterval
specifier|public
name|DateHistogramInterval
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|expression
operator|=
name|in
operator|.
name|readString
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
name|expression
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|expression
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|expression
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|DateHistogramInterval
name|other
init|=
operator|(
name|DateHistogramInterval
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|expression
argument_list|,
name|other
operator|.
name|expression
argument_list|)
return|;
block|}
block|}
end_class

end_unit

