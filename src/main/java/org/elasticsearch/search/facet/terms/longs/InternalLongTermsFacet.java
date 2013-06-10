begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.terms.longs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|terms
operator|.
name|longs
package|;
end_package

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
name|gnu
operator|.
name|trove
operator|.
name|iterator
operator|.
name|TLongIntIterator
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
name|TLongIntHashMap
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
name|CacheRecycler
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
name|collect
operator|.
name|BoundedTreeSet
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
name|terms
operator|.
name|InternalTermsFacet
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
name|terms
operator|.
name|TermsFacet
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
name|Collection
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
comment|/**  *  */
end_comment

begin_class
DECL|class|InternalLongTermsFacet
specifier|public
class|class
name|InternalLongTermsFacet
extends|extends
name|InternalTermsFacet
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
literal|"lTerms"
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
name|readTermsFacet
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
DECL|class|LongEntry
specifier|public
specifier|static
class|class
name|LongEntry
implements|implements
name|Entry
block|{
DECL|field|term
name|long
name|term
decl_stmt|;
DECL|field|count
name|int
name|count
decl_stmt|;
DECL|method|LongEntry
specifier|public
name|LongEntry
parameter_list|(
name|long
name|term
parameter_list|,
name|int
name|count
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
name|Long
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
name|int
name|getCount
parameter_list|()
block|{
return|return
name|count
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
name|long
name|anotherVal
init|=
operator|(
operator|(
name|LongEntry
operator|)
name|o
operator|)
operator|.
name|term
decl_stmt|;
if|if
condition|(
name|term
operator|<
name|anotherVal
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
if|if
condition|(
name|term
operator|==
name|anotherVal
condition|)
block|{
name|int
name|i
init|=
name|count
operator|-
name|o
operator|.
name|getCount
argument_list|()
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
name|System
operator|.
name|identityHashCode
argument_list|(
name|this
argument_list|)
operator|-
name|System
operator|.
name|identityHashCode
argument_list|(
name|o
argument_list|)
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
return|return
literal|1
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
DECL|field|total
name|long
name|total
decl_stmt|;
DECL|field|entries
name|Collection
argument_list|<
name|LongEntry
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
DECL|method|InternalLongTermsFacet
name|InternalLongTermsFacet
parameter_list|()
block|{     }
DECL|method|InternalLongTermsFacet
specifier|public
name|InternalLongTermsFacet
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
name|LongEntry
argument_list|>
name|entries
parameter_list|,
name|long
name|missing
parameter_list|,
name|long
name|total
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
name|this
operator|.
name|total
operator|=
name|total
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getEntries
specifier|public
name|List
argument_list|<
name|LongEntry
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
name|LongEntry
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
DECL|method|getTotalCount
specifier|public
name|long
name|getTotalCount
parameter_list|()
block|{
return|return
name|this
operator|.
name|total
return|;
block|}
annotation|@
name|Override
DECL|method|getOtherCount
specifier|public
name|long
name|getOtherCount
parameter_list|()
block|{
name|long
name|other
init|=
name|total
decl_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|other
operator|-=
name|entry
operator|.
name|getCount
argument_list|()
expr_stmt|;
block|}
return|return
name|other
return|;
block|}
annotation|@
name|Override
DECL|method|reduce
specifier|public
name|Facet
name|reduce
parameter_list|(
name|List
argument_list|<
name|Facet
argument_list|>
name|facets
parameter_list|)
block|{
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
return|return
name|facets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
return|;
block|}
name|InternalLongTermsFacet
name|first
init|=
literal|null
decl_stmt|;
name|TLongIntHashMap
name|aggregated
init|=
name|CacheRecycler
operator|.
name|popLongIntMap
argument_list|()
decl_stmt|;
name|long
name|missing
init|=
literal|0
decl_stmt|;
name|long
name|total
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Facet
name|facet
range|:
name|facets
control|)
block|{
name|TermsFacet
name|termsFacet
init|=
operator|(
name|TermsFacet
operator|)
name|facet
decl_stmt|;
comment|// termsFacet could be of type InternalStringTermsFacet representing unmapped fields
if|if
condition|(
name|first
operator|==
literal|null
operator|&&
name|termsFacet
operator|instanceof
name|InternalLongTermsFacet
condition|)
block|{
name|first
operator|=
operator|(
name|InternalLongTermsFacet
operator|)
name|termsFacet
expr_stmt|;
block|}
name|missing
operator|+=
name|termsFacet
operator|.
name|getMissingCount
argument_list|()
expr_stmt|;
name|total
operator|+=
name|termsFacet
operator|.
name|getTotalCount
argument_list|()
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|termsFacet
operator|.
name|getEntries
argument_list|()
control|)
block|{
name|aggregated
operator|.
name|adjustOrPutValue
argument_list|(
operator|(
operator|(
name|LongEntry
operator|)
name|entry
operator|)
operator|.
name|term
argument_list|,
name|entry
operator|.
name|getCount
argument_list|()
argument_list|,
name|entry
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|BoundedTreeSet
argument_list|<
name|LongEntry
argument_list|>
name|ordered
init|=
operator|new
name|BoundedTreeSet
argument_list|<
name|LongEntry
argument_list|>
argument_list|(
name|first
operator|.
name|comparatorType
operator|.
name|comparator
argument_list|()
argument_list|,
name|first
operator|.
name|requiredSize
argument_list|)
decl_stmt|;
for|for
control|(
name|TLongIntIterator
name|it
init|=
name|aggregated
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
name|ordered
operator|.
name|add
argument_list|(
operator|new
name|LongEntry
argument_list|(
name|it
operator|.
name|key
argument_list|()
argument_list|,
name|it
operator|.
name|value
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|first
operator|.
name|entries
operator|=
name|ordered
expr_stmt|;
name|first
operator|.
name|missing
operator|=
name|missing
expr_stmt|;
name|first
operator|.
name|total
operator|=
name|total
expr_stmt|;
name|CacheRecycler
operator|.
name|pushLongIntMap
argument_list|(
name|aggregated
argument_list|)
expr_stmt|;
return|return
name|first
return|;
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
DECL|field|OTHER
specifier|static
specifier|final
name|XContentBuilderString
name|OTHER
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"other"
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
name|TermsFacet
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
name|field
argument_list|(
name|Fields
operator|.
name|OTHER
argument_list|,
name|getOtherCount
argument_list|()
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
name|LongEntry
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
name|entry
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
DECL|method|readTermsFacet
specifier|public
specifier|static
name|InternalLongTermsFacet
name|readTermsFacet
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalLongTermsFacet
name|facet
init|=
operator|new
name|InternalLongTermsFacet
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
name|total
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
name|LongEntry
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
name|LongEntry
argument_list|(
name|in
operator|.
name|readLong
argument_list|()
argument_list|,
name|in
operator|.
name|readVInt
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
name|writeVLong
argument_list|(
name|total
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
name|LongEntry
name|entry
range|:
name|entries
control|)
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|entry
operator|.
name|term
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|entry
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

