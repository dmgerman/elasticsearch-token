begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.termsstats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|termsstats
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
name|util
operator|.
name|BytesRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|search
operator|.
name|facet
operator|.
name|Facet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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

begin_interface
DECL|interface|TermsStatsFacet
specifier|public
interface|interface
name|TermsStatsFacet
extends|extends
name|Facet
extends|,
name|Iterable
argument_list|<
name|TermsStatsFacet
operator|.
name|Entry
argument_list|>
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"terms_stats"
decl_stmt|;
comment|/**      * The number of docs missing a value.      */
DECL|method|missingCount
name|long
name|missingCount
parameter_list|()
function_decl|;
comment|/**      * The number of docs missing a value.      */
DECL|method|getMissingCount
name|long
name|getMissingCount
parameter_list|()
function_decl|;
comment|/**      * The terms and counts.      */
DECL|method|entries
name|List
argument_list|<
name|?
extends|extends
name|TermsStatsFacet
operator|.
name|Entry
argument_list|>
name|entries
parameter_list|()
function_decl|;
comment|/**      * The terms and counts.      */
DECL|method|getEntries
name|List
argument_list|<
name|?
extends|extends
name|TermsStatsFacet
operator|.
name|Entry
argument_list|>
name|getEntries
parameter_list|()
function_decl|;
comment|/**      * Controls how the terms facets are ordered.      */
DECL|enum|ComparatorType
specifier|public
specifier|static
enum|enum
name|ComparatorType
block|{
comment|/**          * Order by the (higher) count of each term.          */
DECL|enum constant|COUNT
name|COUNT
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|i
init|=
operator|(
name|o2
operator|.
name|count
argument_list|()
operator|<
name|o1
operator|.
name|count
argument_list|()
condition|?
operator|-
literal|1
else|:
operator|(
name|o1
operator|.
name|count
argument_list|()
operator|==
name|o2
operator|.
name|count
argument_list|()
condition|?
literal|0
else|:
literal|1
operator|)
operator|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|o2
operator|.
name|compareTo
argument_list|(
name|o1
argument_list|)
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the (lower) count of each term.          */
DECL|enum constant|REVERSE_COUNT
name|REVERSE_COUNT
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
operator|-
name|COUNT
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the terms.          */
DECL|enum constant|TERM
name|TERM
argument_list|(
operator|(
name|byte
operator|)
literal|2
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|i
init|=
name|o1
operator|.
name|compareTo
argument_list|(
name|o2
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|COUNT
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the terms.          */
DECL|enum constant|REVERSE_TERM
name|REVERSE_TERM
argument_list|(
operator|(
name|byte
operator|)
literal|3
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
operator|-
name|TERM
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the (higher) total of each term.          */
DECL|enum constant|TOTAL
name|TOTAL
argument_list|(
operator|(
name|byte
operator|)
literal|4
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|i
init|=
operator|-
name|Double
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|total
argument_list|()
argument_list|,
name|o2
operator|.
name|total
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|COUNT
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the (lower) total of each term.          */
DECL|enum constant|REVERSE_TOTAL
name|REVERSE_TOTAL
argument_list|(
operator|(
name|byte
operator|)
literal|5
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
operator|-
name|TOTAL
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the (lower) min of each term.          */
DECL|enum constant|MIN
name|MIN
argument_list|(
operator|(
name|byte
operator|)
literal|6
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|i
init|=
name|Double
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|min
argument_list|()
argument_list|,
name|o2
operator|.
name|min
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|COUNT
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the (higher) min of each term.          */
DECL|enum constant|REVERSE_MIN
name|REVERSE_MIN
argument_list|(
operator|(
name|byte
operator|)
literal|7
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
operator|-
name|MIN
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the (higher) max of each term.          */
DECL|enum constant|MAX
name|MAX
argument_list|(
operator|(
name|byte
operator|)
literal|8
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|i
init|=
operator|-
name|Double
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|max
argument_list|()
argument_list|,
name|o2
operator|.
name|max
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|COUNT
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the (lower) max of each term.          */
DECL|enum constant|REVERSE_MAX
name|REVERSE_MAX
argument_list|(
operator|(
name|byte
operator|)
literal|9
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
operator|-
name|MAX
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the (higher) mean of each term.          */
DECL|enum constant|MEAN
name|MEAN
argument_list|(
operator|(
name|byte
operator|)
literal|10
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|i
init|=
operator|-
name|Double
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|mean
argument_list|()
argument_list|,
name|o2
operator|.
name|mean
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|COUNT
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
block|}
argument_list|)
block|,
comment|/**          * Order by the (lower) mean of each term.          */
DECL|enum constant|REVERSE_MEAN
name|REVERSE_MEAN
argument_list|(
operator|(
name|byte
operator|)
literal|11
argument_list|,
operator|new
name|Comparator
argument_list|<
name|Entry
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|Entry
name|o1
parameter_list|,
name|Entry
name|o2
parameter_list|)
block|{
comment|// push nulls to the end
if|if
condition|(
name|o1
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
return|return
operator|-
name|MEAN
operator|.
name|comparator
argument_list|()
operator|.
name|compare
argument_list|(
name|o1
argument_list|,
name|o2
argument_list|)
return|;
block|}
block|}
argument_list|)
block|,;
DECL|field|id
specifier|private
specifier|final
name|byte
name|id
decl_stmt|;
DECL|field|comparator
specifier|private
specifier|final
name|Comparator
argument_list|<
name|Entry
argument_list|>
name|comparator
decl_stmt|;
DECL|method|ComparatorType
name|ComparatorType
parameter_list|(
name|byte
name|id
parameter_list|,
name|Comparator
argument_list|<
name|Entry
argument_list|>
name|comparator
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
name|comparator
operator|=
name|comparator
expr_stmt|;
block|}
DECL|method|id
specifier|public
name|byte
name|id
parameter_list|()
block|{
return|return
name|this
operator|.
name|id
return|;
block|}
DECL|method|comparator
specifier|public
name|Comparator
argument_list|<
name|Entry
argument_list|>
name|comparator
parameter_list|()
block|{
return|return
name|comparator
return|;
block|}
DECL|method|fromId
specifier|public
specifier|static
name|ComparatorType
name|fromId
parameter_list|(
name|byte
name|id
parameter_list|)
block|{
if|if
condition|(
name|id
operator|==
name|COUNT
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|COUNT
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|REVERSE_COUNT
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|REVERSE_COUNT
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|TERM
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|TERM
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|REVERSE_TERM
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|REVERSE_TERM
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|TOTAL
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|TOTAL
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|REVERSE_TOTAL
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|REVERSE_TOTAL
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|MIN
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|MIN
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|REVERSE_MIN
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|REVERSE_MIN
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|MAX
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|MAX
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|REVERSE_MAX
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|REVERSE_MAX
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|MEAN
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|MEAN
return|;
block|}
elseif|else
if|if
condition|(
name|id
operator|==
name|REVERSE_MEAN
operator|.
name|id
argument_list|()
condition|)
block|{
return|return
name|REVERSE_MEAN
return|;
block|}
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"No type argument match for terms facet comparator ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
DECL|method|fromString
specifier|public
specifier|static
name|ComparatorType
name|fromString
parameter_list|(
name|String
name|type
parameter_list|)
block|{
if|if
condition|(
literal|"count"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|COUNT
return|;
block|}
elseif|else
if|if
condition|(
literal|"term"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|TERM
return|;
block|}
elseif|else
if|if
condition|(
literal|"reverse_count"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
operator|||
literal|"reverseCount"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|REVERSE_COUNT
return|;
block|}
elseif|else
if|if
condition|(
literal|"reverse_term"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
operator|||
literal|"reverseTerm"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|REVERSE_TERM
return|;
block|}
elseif|else
if|if
condition|(
literal|"total"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|TOTAL
return|;
block|}
elseif|else
if|if
condition|(
literal|"reverse_total"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
operator|||
literal|"reverseTotal"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|REVERSE_TOTAL
return|;
block|}
elseif|else
if|if
condition|(
literal|"min"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|MIN
return|;
block|}
elseif|else
if|if
condition|(
literal|"reverse_min"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
operator|||
literal|"reverseMin"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|REVERSE_MIN
return|;
block|}
elseif|else
if|if
condition|(
literal|"max"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|MAX
return|;
block|}
elseif|else
if|if
condition|(
literal|"reverse_max"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
operator|||
literal|"reverseMax"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|REVERSE_MAX
return|;
block|}
elseif|else
if|if
condition|(
literal|"mean"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|MEAN
return|;
block|}
elseif|else
if|if
condition|(
literal|"reverse_mean"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
operator|||
literal|"reverseMean"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
return|return
name|REVERSE_MEAN
return|;
block|}
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"No type argument match for terms stats facet comparator ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|interface|Entry
specifier|public
interface|interface
name|Entry
extends|extends
name|Comparable
argument_list|<
name|Entry
argument_list|>
block|{
DECL|method|term
name|BytesReference
name|term
parameter_list|()
function_decl|;
DECL|method|getTerm
name|BytesReference
name|getTerm
parameter_list|()
function_decl|;
DECL|method|termAsNumber
name|Number
name|termAsNumber
parameter_list|()
function_decl|;
DECL|method|getTermAsNumber
name|Number
name|getTermAsNumber
parameter_list|()
function_decl|;
DECL|method|count
name|long
name|count
parameter_list|()
function_decl|;
DECL|method|getCount
name|long
name|getCount
parameter_list|()
function_decl|;
DECL|method|totalCount
name|long
name|totalCount
parameter_list|()
function_decl|;
DECL|method|getTotalCount
name|long
name|getTotalCount
parameter_list|()
function_decl|;
DECL|method|min
name|double
name|min
parameter_list|()
function_decl|;
DECL|method|getMin
name|double
name|getMin
parameter_list|()
function_decl|;
DECL|method|max
name|double
name|max
parameter_list|()
function_decl|;
DECL|method|getMax
name|double
name|getMax
parameter_list|()
function_decl|;
DECL|method|total
name|double
name|total
parameter_list|()
function_decl|;
DECL|method|getTotal
name|double
name|getTotal
parameter_list|()
function_decl|;
DECL|method|mean
name|double
name|mean
parameter_list|()
function_decl|;
DECL|method|getMean
name|double
name|getMean
parameter_list|()
function_decl|;
block|}
block|}
end_interface

end_unit

