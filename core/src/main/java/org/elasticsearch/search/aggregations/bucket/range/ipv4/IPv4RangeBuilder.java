begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range.ipv4
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
name|range
operator|.
name|ipv4
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
name|aggregations
operator|.
name|bucket
operator|.
name|range
operator|.
name|AbstractRangeBuilder
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
name|builder
operator|.
name|SearchSourceBuilderException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  * Builder for the {@link IPv4Range} aggregation.  */
end_comment

begin_class
DECL|class|IPv4RangeBuilder
specifier|public
class|class
name|IPv4RangeBuilder
extends|extends
name|AbstractRangeBuilder
argument_list|<
name|IPv4RangeBuilder
argument_list|>
block|{
DECL|field|MASK_PATTERN
specifier|private
specifier|static
specifier|final
name|Pattern
name|MASK_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[\\.|/]"
argument_list|)
decl_stmt|;
comment|/**      * Sole constructor.      */
DECL|method|IPv4RangeBuilder
specifier|public
name|IPv4RangeBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalIPv4Range
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Add a new range to this aggregation.      *      * @param key  the key to use for this range in the response      * @param from the lower bound on the distances, inclusive      * @parap to   the upper bound on the distances, exclusive      */
DECL|method|addRange
specifier|public
name|IPv4RangeBuilder
name|addRange
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addMaskRange(String, String)} but uses the mask itself as a key.      */
DECL|method|addMaskRange
specifier|public
name|IPv4RangeBuilder
name|addMaskRange
parameter_list|(
name|String
name|mask
parameter_list|)
block|{
return|return
name|addMaskRange
argument_list|(
name|mask
argument_list|,
name|mask
argument_list|)
return|;
block|}
comment|/**      * Add a range based on a CIDR mask.      */
DECL|method|addMaskRange
specifier|public
name|IPv4RangeBuilder
name|addMaskRange
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|mask
parameter_list|)
block|{
name|long
index|[]
name|fromTo
init|=
name|cidrMaskToMinMax
argument_list|(
name|mask
argument_list|)
decl_stmt|;
if|if
condition|(
name|fromTo
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchSourceBuilderException
argument_list|(
literal|"invalid CIDR mask ["
operator|+
name|mask
operator|+
literal|"] in ip_range aggregation ["
operator|+
name|getName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|fromTo
index|[
literal|0
index|]
operator|<
literal|0
condition|?
literal|null
else|:
name|fromTo
index|[
literal|0
index|]
argument_list|,
name|fromTo
index|[
literal|1
index|]
operator|<
literal|0
condition|?
literal|null
else|:
name|fromTo
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addRange(String, String, String)} but the key will be      * automatically generated.      */
DECL|method|addRange
specifier|public
name|IPv4RangeBuilder
name|addRange
parameter_list|(
name|String
name|from
parameter_list|,
name|String
name|to
parameter_list|)
block|{
return|return
name|addRange
argument_list|(
literal|null
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Same as {@link #addRange(String, String, String)} but there will be no lower bound.      */
DECL|method|addUnboundedTo
specifier|public
name|IPv4RangeBuilder
name|addUnboundedTo
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|to
parameter_list|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
literal|null
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedTo(String, String)} but the key will be      * generated automatically.      */
DECL|method|addUnboundedTo
specifier|public
name|IPv4RangeBuilder
name|addUnboundedTo
parameter_list|(
name|String
name|to
parameter_list|)
block|{
return|return
name|addUnboundedTo
argument_list|(
literal|null
argument_list|,
name|to
argument_list|)
return|;
block|}
comment|/**      * Same as {@link #addRange(String, String, String)} but there will be no upper bound.      */
DECL|method|addUnboundedFrom
specifier|public
name|IPv4RangeBuilder
name|addUnboundedFrom
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|from
parameter_list|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Same as {@link #addUnboundedFrom(String, String)} but the key will be      * generated automatically.      */
DECL|method|addUnboundedFrom
specifier|public
name|IPv4RangeBuilder
name|addUnboundedFrom
parameter_list|(
name|String
name|from
parameter_list|)
block|{
return|return
name|addUnboundedFrom
argument_list|(
literal|null
argument_list|,
name|from
argument_list|)
return|;
block|}
comment|/**      * Computes the min& max ip addresses (represented as long values - same way as stored in index) represented by the given CIDR mask      * expression. The returned array has the length of 2, where the first entry represents the {@code min} address and the second the {@code max}.      * A {@code -1} value for either the {@code min} or the {@code max}, represents an unbounded end. In other words:      *      *<p>      * {@code min == -1 == "0.0.0.0" }      *</p>      *      * and      *      *<p>      * {@code max == -1 == "255.255.255.255" }      *</p>      *      * @param cidr      * @return      */
DECL|method|cidrMaskToMinMax
specifier|static
name|long
index|[]
name|cidrMaskToMinMax
parameter_list|(
name|String
name|cidr
parameter_list|)
block|{
name|String
index|[]
name|parts
init|=
name|MASK_PATTERN
operator|.
name|split
argument_list|(
name|cidr
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|.
name|length
operator|!=
literal|5
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|addr
init|=
operator|(
operator|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|)
operator|<<
literal|24
operator|)
operator|&
literal|0xFF000000
operator|)
operator||
operator|(
operator|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|parts
index|[
literal|1
index|]
argument_list|)
operator|<<
literal|16
operator|)
operator|&
literal|0xFF0000
operator|)
operator||
operator|(
operator|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|parts
index|[
literal|2
index|]
argument_list|)
operator|<<
literal|8
operator|)
operator|&
literal|0xFF00
operator|)
operator||
operator|(
name|Integer
operator|.
name|parseInt
argument_list|(
name|parts
index|[
literal|3
index|]
argument_list|)
operator|&
literal|0xFF
operator|)
decl_stmt|;
name|int
name|mask
init|=
operator|(
operator|-
literal|1
operator|)
operator|<<
operator|(
literal|32
operator|-
name|Integer
operator|.
name|parseInt
argument_list|(
name|parts
index|[
literal|4
index|]
argument_list|)
operator|)
decl_stmt|;
name|int
name|from
init|=
name|addr
operator|&
name|mask
decl_stmt|;
name|long
name|longFrom
init|=
name|intIpToLongIp
argument_list|(
name|from
argument_list|)
decl_stmt|;
if|if
condition|(
name|longFrom
operator|==
literal|0
condition|)
block|{
name|longFrom
operator|=
operator|-
literal|1
expr_stmt|;
block|}
name|int
name|to
init|=
name|from
operator|+
operator|(
operator|~
name|mask
operator|)
decl_stmt|;
name|long
name|longTo
init|=
name|intIpToLongIp
argument_list|(
name|to
argument_list|)
operator|+
literal|1
decl_stmt|;
comment|// we have to +1 here as the range is non-inclusive on the "to" side
if|if
condition|(
name|longTo
operator|==
name|InternalIPv4Range
operator|.
name|MAX_IP
condition|)
block|{
name|longTo
operator|=
operator|-
literal|1
expr_stmt|;
block|}
return|return
operator|new
name|long
index|[]
block|{
name|longFrom
block|,
name|longTo
block|}
return|;
block|}
DECL|method|intIpToLongIp
specifier|private
specifier|static
name|long
name|intIpToLongIp
parameter_list|(
name|int
name|i
parameter_list|)
block|{
name|long
name|p1
init|=
operator|(
call|(
name|long
call|)
argument_list|(
operator|(
name|i
operator|>>
literal|24
operator|)
operator|&
literal|0xFF
argument_list|)
operator|)
operator|<<
literal|24
decl_stmt|;
name|int
name|p2
init|=
operator|(
operator|(
name|i
operator|>>
literal|16
operator|)
operator|&
literal|0xFF
operator|)
operator|<<
literal|16
decl_stmt|;
name|int
name|p3
init|=
operator|(
operator|(
name|i
operator|>>
literal|8
operator|)
operator|&
literal|0xFF
operator|)
operator|<<
literal|8
decl_stmt|;
name|int
name|p4
init|=
name|i
operator|&
literal|0xFF
decl_stmt|;
return|return
name|p1
operator|+
name|p2
operator|+
name|p3
operator|+
name|p4
return|;
block|}
block|}
end_class

end_unit
