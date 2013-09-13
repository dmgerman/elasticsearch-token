begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.termsstats.doubles
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
operator|.
name|doubles
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|DoubleObjectOpenHashMap
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableList
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
name|util
operator|.
name|CollectionUtil
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
name|Strings
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
name|common
operator|.
name|bytes
operator|.
name|HashedBytesArray
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
name|recycler
operator|.
name|Recycler
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
name|text
operator|.
name|StringText
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
name|text
operator|.
name|Text
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|termsstats
operator|.
name|InternalTermsStatsFacet
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
name|*
import|;
end_import

begin_class
DECL|class|InternalTermsStatsDoubleFacet
specifier|public
class|class
name|InternalTermsStatsDoubleFacet
extends|extends
name|InternalTermsStatsFacet
block|{
DECL|field|STREAM_TYPE
specifier|private
specifier|static
specifier|final
name|BytesReference
name|STREAM_TYPE
init|=
operator|new
name|HashedBytesArray
argument_list|(
name|Strings
operator|.
name|toUTF8Bytes
argument_list|(
literal|"dTS"
argument_list|)
argument_list|)
decl_stmt|;
DECL|method|registerStream
specifier|public
specifier|static
name|void
name|registerStream
parameter_list|()
block|{
name|Streams
operator|.
name|registerStream
argument_list|(
name|STREAM
argument_list|,
name|STREAM_TYPE
argument_list|)
expr_stmt|;
block|}
DECL|field|STREAM
specifier|static
name|Stream
name|STREAM
init|=
operator|new
name|Stream
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Facet
name|readFacet
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|readTermsStatsFacet
argument_list|(
name|in
argument_list|)
return|;
block|}
block|}
decl_stmt|;
annotation|@
name|Override
DECL|method|streamType
specifier|public
name|BytesReference
name|streamType
parameter_list|()
block|{
return|return
name|STREAM_TYPE
return|;
block|}
DECL|method|InternalTermsStatsDoubleFacet
specifier|public
name|InternalTermsStatsDoubleFacet
parameter_list|()
block|{     }
DECL|class|DoubleEntry
specifier|public
specifier|static
class|class
name|DoubleEntry
implements|implements
name|Entry
block|{
DECL|field|term
name|double
name|term
decl_stmt|;
DECL|field|count
name|long
name|count
decl_stmt|;
DECL|field|totalCount
name|long
name|totalCount
decl_stmt|;
DECL|field|total
name|double
name|total
decl_stmt|;
DECL|field|min
name|double
name|min
decl_stmt|;
DECL|field|max
name|double
name|max
decl_stmt|;
DECL|method|DoubleEntry
specifier|public
name|DoubleEntry
parameter_list|(
name|double
name|term
parameter_list|,
name|long
name|count
parameter_list|,
name|long
name|totalCount
parameter_list|,
name|double
name|total
parameter_list|,
name|double
name|min
parameter_list|,
name|double
name|max
parameter_list|)
block|{
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
name|this
operator|.
name|total
operator|=
name|total
expr_stmt|;
name|this
operator|.
name|totalCount
operator|=
name|totalCount
expr_stmt|;
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
annotation|@
name|Override
DECL|method|getTerm
specifier|public
name|Text
name|getTerm
parameter_list|()
block|{
return|return
operator|new
name|StringText
argument_list|(
name|Double
operator|.
name|toString
argument_list|(
name|term
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getTermAsNumber
specifier|public
name|Number
name|getTermAsNumber
parameter_list|()
block|{
return|return
name|term
return|;
block|}
annotation|@
name|Override
DECL|method|getCount
specifier|public
name|long
name|getCount
parameter_list|()
block|{
return|return
name|count
return|;
block|}
annotation|@
name|Override
DECL|method|getTotalCount
specifier|public
name|long
name|getTotalCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|totalCount
return|;
block|}
annotation|@
name|Override
DECL|method|getMin
specifier|public
name|double
name|getMin
parameter_list|()
block|{
return|return
name|this
operator|.
name|min
return|;
block|}
annotation|@
name|Override
DECL|method|getMax
specifier|public
name|double
name|getMax
parameter_list|()
block|{
return|return
name|max
return|;
block|}
annotation|@
name|Override
DECL|method|getTotal
specifier|public
name|double
name|getTotal
parameter_list|()
block|{
return|return
name|total
return|;
block|}
annotation|@
name|Override
DECL|method|getMean
specifier|public
name|double
name|getMean
parameter_list|()
block|{
if|if
condition|(
name|totalCount
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|total
operator|/
name|totalCount
return|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|Entry
name|o
parameter_list|)
block|{
name|DoubleEntry
name|other
init|=
operator|(
name|DoubleEntry
operator|)
name|o
decl_stmt|;
return|return
operator|(
name|term
operator|<
name|other
operator|.
name|term
condition|?
operator|-
literal|1
else|:
operator|(
name|term
operator|==
name|other
operator|.
name|term
condition|?
literal|0
else|:
literal|1
operator|)
operator|)
return|;
block|}
block|}
DECL|field|requiredSize
name|int
name|requiredSize
decl_stmt|;
DECL|field|missing
name|long
name|missing
decl_stmt|;
DECL|field|entries
name|Collection
argument_list|<
name|DoubleEntry
argument_list|>
name|entries
init|=
name|ImmutableList
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|comparatorType
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|method|InternalTermsStatsDoubleFacet
specifier|public
name|InternalTermsStatsDoubleFacet
parameter_list|(
name|String
name|name
parameter_list|,
name|ComparatorType
name|comparatorType
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|Collection
argument_list|<
name|DoubleEntry
argument_list|>
name|entries
parameter_list|,
name|long
name|missing
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|comparatorType
operator|=
name|comparatorType
expr_stmt|;
name|this
operator|.
name|requiredSize
operator|=
name|requiredSize
expr_stmt|;
name|this
operator|.
name|entries
operator|=
name|entries
expr_stmt|;
name|this
operator|.
name|missing
operator|=
name|missing
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getEntries
specifier|public
name|List
argument_list|<
name|DoubleEntry
argument_list|>
name|getEntries
parameter_list|()
block|{
if|if
condition|(
operator|!
operator|(
name|entries
operator|instanceof
name|List
operator|)
condition|)
block|{
name|entries
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|entries
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|List
argument_list|<
name|DoubleEntry
argument_list|>
operator|)
name|entries
return|;
block|}
DECL|method|mutableList
name|List
argument_list|<
name|DoubleEntry
argument_list|>
name|mutableList
parameter_list|()
block|{
if|if
condition|(
operator|!
operator|(
name|entries
operator|instanceof
name|List
operator|)
condition|)
block|{
name|entries
operator|=
operator|new
name|ArrayList
argument_list|<
name|DoubleEntry
argument_list|>
argument_list|(
name|entries
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|List
argument_list|<
name|DoubleEntry
argument_list|>
operator|)
name|entries
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
annotation|@
name|Override
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Entry
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|(
name|Iterator
operator|)
name|entries
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getMissingCount
specifier|public
name|long
name|getMissingCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|missing
return|;
block|}
annotation|@
name|Override
DECL|method|reduce
specifier|public
name|Facet
name|reduce
parameter_list|(
name|ReduceContext
name|context
parameter_list|)
block|{
name|List
argument_list|<
name|Facet
argument_list|>
name|facets
init|=
name|context
operator|.
name|facets
argument_list|()
decl_stmt|;
if|if
condition|(
name|facets
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|InternalTermsStatsDoubleFacet
name|tsFacet
init|=
operator|(
name|InternalTermsStatsDoubleFacet
operator|)
name|facets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|requiredSize
operator|==
literal|0
condition|)
block|{
comment|// we need to sort it here!
if|if
condition|(
operator|!
name|tsFacet
operator|.
name|entries
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|List
argument_list|<
name|DoubleEntry
argument_list|>
name|entries
init|=
name|tsFacet
operator|.
name|mutableList
argument_list|()
decl_stmt|;
name|CollectionUtil
operator|.
name|timSort
argument_list|(
name|entries
argument_list|,
name|comparatorType
operator|.
name|comparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|tsFacet
operator|.
name|trimExcessEntries
argument_list|()
expr_stmt|;
return|return
name|facets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
name|int
name|missing
init|=
literal|0
decl_stmt|;
name|Recycler
operator|.
name|V
argument_list|<
name|DoubleObjectOpenHashMap
argument_list|<
name|DoubleEntry
argument_list|>
argument_list|>
name|map
init|=
name|context
operator|.
name|cacheRecycler
argument_list|()
operator|.
name|doubleObjectMap
argument_list|(
operator|-
literal|1
argument_list|)
decl_stmt|;
for|for
control|(
name|Facet
name|facet
range|:
name|facets
control|)
block|{
name|InternalTermsStatsDoubleFacet
name|tsFacet
init|=
operator|(
name|InternalTermsStatsDoubleFacet
operator|)
name|facet
decl_stmt|;
name|missing
operator|+=
name|tsFacet
operator|.
name|missing
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|tsFacet
control|)
block|{
name|DoubleEntry
name|doubleEntry
init|=
operator|(
name|DoubleEntry
operator|)
name|entry
decl_stmt|;
name|DoubleEntry
name|current
init|=
name|map
operator|.
name|v
argument_list|()
operator|.
name|get
argument_list|(
name|doubleEntry
operator|.
name|term
argument_list|)
decl_stmt|;
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
name|current
operator|.
name|count
operator|+=
name|doubleEntry
operator|.
name|count
expr_stmt|;
name|current
operator|.
name|totalCount
operator|+=
name|doubleEntry
operator|.
name|totalCount
expr_stmt|;
name|current
operator|.
name|total
operator|+=
name|doubleEntry
operator|.
name|total
expr_stmt|;
if|if
condition|(
name|doubleEntry
operator|.
name|min
operator|<
name|current
operator|.
name|min
condition|)
block|{
name|current
operator|.
name|min
operator|=
name|doubleEntry
operator|.
name|min
expr_stmt|;
block|}
if|if
condition|(
name|doubleEntry
operator|.
name|max
operator|>
name|current
operator|.
name|max
condition|)
block|{
name|current
operator|.
name|max
operator|=
name|doubleEntry
operator|.
name|max
expr_stmt|;
block|}
block|}
else|else
block|{
name|map
operator|.
name|v
argument_list|()
operator|.
name|put
argument_list|(
name|doubleEntry
operator|.
name|term
argument_list|,
name|doubleEntry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|// sort
if|if
condition|(
name|requiredSize
operator|==
literal|0
condition|)
block|{
comment|// all terms
name|DoubleEntry
index|[]
name|entries1
init|=
name|map
operator|.
name|v
argument_list|()
operator|.
name|values
argument_list|()
operator|.
name|toArray
argument_list|(
name|DoubleEntry
operator|.
name|class
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|entries1
argument_list|,
name|comparatorType
operator|.
name|comparator
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
operator|new
name|InternalTermsStatsDoubleFacet
argument_list|(
name|getName
argument_list|()
argument_list|,
name|comparatorType
argument_list|,
name|requiredSize
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|entries1
argument_list|)
argument_list|,
name|missing
argument_list|)
return|;
block|}
else|else
block|{
name|Object
index|[]
name|values
init|=
name|map
operator|.
name|v
argument_list|()
operator|.
name|values
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|values
argument_list|,
operator|(
name|Comparator
operator|)
name|comparatorType
operator|.
name|comparator
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|DoubleEntry
argument_list|>
name|ordered
init|=
operator|new
name|ArrayList
argument_list|<
name|DoubleEntry
argument_list|>
argument_list|(
name|map
operator|.
name|v
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
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
name|requiredSize
condition|;
name|i
operator|++
control|)
block|{
name|DoubleEntry
name|value
init|=
operator|(
name|DoubleEntry
operator|)
name|values
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|ordered
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|map
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
operator|new
name|InternalTermsStatsDoubleFacet
argument_list|(
name|getName
argument_list|()
argument_list|,
name|comparatorType
argument_list|,
name|requiredSize
argument_list|,
name|ordered
argument_list|,
name|missing
argument_list|)
return|;
block|}
block|}
DECL|method|trimExcessEntries
specifier|private
name|void
name|trimExcessEntries
parameter_list|()
block|{
if|if
condition|(
name|requiredSize
operator|==
literal|0
operator|||
name|requiredSize
operator|>=
name|entries
operator|.
name|size
argument_list|()
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|entries
operator|instanceof
name|List
condition|)
block|{
name|entries
operator|=
operator|(
operator|(
name|List
operator|)
name|entries
operator|)
operator|.
name|subList
argument_list|(
literal|0
argument_list|,
name|requiredSize
argument_list|)
expr_stmt|;
return|return;
block|}
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Iterator
argument_list|<
name|DoubleEntry
argument_list|>
name|iter
init|=
name|entries
operator|.
name|iterator
argument_list|()
init|;
name|iter
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|iter
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
name|i
operator|++
operator|>=
name|requiredSize
condition|)
block|{
name|iter
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|class|Fields
specifier|static
specifier|final
class|class
name|Fields
block|{
DECL|field|_TYPE
specifier|static
specifier|final
name|XContentBuilderString
name|_TYPE
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_type"
argument_list|)
decl_stmt|;
DECL|field|MISSING
specifier|static
specifier|final
name|XContentBuilderString
name|MISSING
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"missing"
argument_list|)
decl_stmt|;
DECL|field|TERMS
specifier|static
specifier|final
name|XContentBuilderString
name|TERMS
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"terms"
argument_list|)
decl_stmt|;
DECL|field|TERM
specifier|static
specifier|final
name|XContentBuilderString
name|TERM
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"term"
argument_list|)
decl_stmt|;
DECL|field|COUNT
specifier|static
specifier|final
name|XContentBuilderString
name|COUNT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"count"
argument_list|)
decl_stmt|;
DECL|field|TOTAL_COUNT
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL_COUNT
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total_count"
argument_list|)
decl_stmt|;
DECL|field|MIN
specifier|static
specifier|final
name|XContentBuilderString
name|MIN
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"min"
argument_list|)
decl_stmt|;
DECL|field|MAX
specifier|static
specifier|final
name|XContentBuilderString
name|MAX
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"max"
argument_list|)
decl_stmt|;
DECL|field|TOTAL
specifier|static
specifier|final
name|XContentBuilderString
name|TOTAL
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"total"
argument_list|)
decl_stmt|;
DECL|field|MEAN
specifier|static
specifier|final
name|XContentBuilderString
name|MEAN
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"mean"
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
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|_TYPE
argument_list|,
name|InternalTermsStatsFacet
operator|.
name|TYPE
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MISSING
argument_list|,
name|missing
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
name|Fields
operator|.
name|TERMS
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TERM
argument_list|,
operator|(
operator|(
name|DoubleEntry
operator|)
name|entry
operator|)
operator|.
name|term
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|COUNT
argument_list|,
name|entry
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|TOTAL_COUNT
argument_list|,
name|entry
operator|.
name|getTotalCount
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MIN
argument_list|,
name|entry
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MAX
argument_list|,
name|entry
operator|.
name|getMax
argument_list|()
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
name|entry
operator|.
name|getTotal
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|Fields
operator|.
name|MEAN
argument_list|,
name|entry
operator|.
name|getMean
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
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
DECL|method|readTermsStatsFacet
specifier|public
specifier|static
name|InternalTermsStatsDoubleFacet
name|readTermsStatsFacet
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalTermsStatsDoubleFacet
name|facet
init|=
operator|new
name|InternalTermsStatsDoubleFacet
argument_list|()
decl_stmt|;
name|facet
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|facet
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|comparatorType
operator|=
name|ComparatorType
operator|.
name|fromId
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
expr_stmt|;
name|requiredSize
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|missing
operator|=
name|in
operator|.
name|readVLong
argument_list|()
expr_stmt|;
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
name|entries
operator|=
operator|new
name|ArrayList
argument_list|<
name|DoubleEntry
argument_list|>
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
name|entries
operator|.
name|add
argument_list|(
operator|new
name|DoubleEntry
argument_list|(
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|in
operator|.
name|readVLong
argument_list|()
argument_list|,
name|in
operator|.
name|readVLong
argument_list|()
argument_list|,
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|in
operator|.
name|readDouble
argument_list|()
argument_list|)
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|comparatorType
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|requiredSize
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|missing
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|entries
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|out
operator|.
name|writeDouble
argument_list|(
operator|(
operator|(
name|DoubleEntry
operator|)
name|entry
operator|)
operator|.
name|term
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|entry
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|entry
operator|.
name|getTotalCount
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|entry
operator|.
name|getTotal
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|entry
operator|.
name|getMin
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|entry
operator|.
name|getMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

