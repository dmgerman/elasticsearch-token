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
name|rounding
operator|.
name|Rounding
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
name|SearchParseException
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
name|aggregations
operator|.
name|support
operator|.
name|format
operator|.
name|ValueParser
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
name|SearchContext
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
DECL|class|ExtendedBounds
specifier|public
class|class
name|ExtendedBounds
block|{
DECL|field|min
name|Long
name|min
decl_stmt|;
DECL|field|max
name|Long
name|max
decl_stmt|;
DECL|field|minAsStr
name|String
name|minAsStr
decl_stmt|;
DECL|field|maxAsStr
name|String
name|maxAsStr
decl_stmt|;
DECL|method|ExtendedBounds
name|ExtendedBounds
parameter_list|()
block|{}
comment|//for serialization
DECL|method|ExtendedBounds
name|ExtendedBounds
parameter_list|(
name|Long
name|min
parameter_list|,
name|Long
name|max
parameter_list|)
block|{
name|this
operator|.
name|min
operator|=
name|min
expr_stmt|;
name|this
operator|.
name|max
operator|=
name|max
expr_stmt|;
block|}
DECL|method|processAndValidate
name|void
name|processAndValidate
parameter_list|(
name|String
name|aggName
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|ValueParser
name|parser
parameter_list|)
block|{
assert|assert
name|parser
operator|!=
literal|null
assert|;
if|if
condition|(
name|minAsStr
operator|!=
literal|null
condition|)
block|{
name|min
operator|=
name|parser
operator|.
name|parseLong
argument_list|(
name|minAsStr
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|maxAsStr
operator|!=
literal|null
condition|)
block|{
name|max
operator|=
name|parser
operator|.
name|parseLong
argument_list|(
name|maxAsStr
argument_list|,
name|context
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|min
operator|!=
literal|null
operator|&&
name|max
operator|!=
literal|null
operator|&&
name|min
operator|.
name|compareTo
argument_list|(
name|max
argument_list|)
operator|>
literal|0
condition|)
block|{
throw|throw
operator|new
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"[extended_bounds.min]["
operator|+
name|min
operator|+
literal|"] cannot be greater than "
operator|+
literal|"[extended_bounds.max]["
operator|+
name|max
operator|+
literal|"] for histogram aggregation ["
operator|+
name|aggName
operator|+
literal|"]"
argument_list|,
literal|null
argument_list|)
throw|;
block|}
block|}
DECL|method|round
name|ExtendedBounds
name|round
parameter_list|(
name|Rounding
name|rounding
parameter_list|)
block|{
return|return
operator|new
name|ExtendedBounds
argument_list|(
name|min
operator|!=
literal|null
condition|?
name|rounding
operator|.
name|round
argument_list|(
name|min
argument_list|)
else|:
literal|null
argument_list|,
name|max
operator|!=
literal|null
condition|?
name|rounding
operator|.
name|round
argument_list|(
name|max
argument_list|)
else|:
literal|null
argument_list|)
return|;
block|}
DECL|method|writeTo
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|min
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|min
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|max
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|max
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|readFrom
specifier|static
name|ExtendedBounds
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|ExtendedBounds
name|bounds
init|=
operator|new
name|ExtendedBounds
argument_list|()
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|bounds
operator|.
name|min
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|bounds
operator|.
name|max
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
return|return
name|bounds
return|;
block|}
block|}
end_class

end_unit

