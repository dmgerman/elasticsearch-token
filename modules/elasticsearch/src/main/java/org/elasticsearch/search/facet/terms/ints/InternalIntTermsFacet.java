begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.terms.ints
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
name|ints
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
name|collect
operator|.
name|ImmutableList
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
name|thread
operator|.
name|ThreadLocals
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
name|trove
operator|.
name|TIntIntHashMap
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
name|trove
operator|.
name|TIntIntIterator
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
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|InternalIntTermsFacet
specifier|public
class|class
name|InternalIntTermsFacet
extends|extends
name|InternalTermsFacet
block|{
DECL|field|STREAM_TYPE
specifier|private
specifier|static
specifier|final
name|String
name|STREAM_TYPE
init|=
literal|"iTerms"
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
name|String
name|type
parameter_list|,
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
DECL|method|streamType
annotation|@
name|Override
specifier|public
name|String
name|streamType
parameter_list|()
block|{
return|return
name|STREAM_TYPE
return|;
block|}
DECL|class|IntEntry
specifier|public
specifier|static
class|class
name|IntEntry
implements|implements
name|Entry
block|{
DECL|field|term
name|int
name|term
decl_stmt|;
DECL|field|count
name|int
name|count
decl_stmt|;
DECL|method|IntEntry
specifier|public
name|IntEntry
parameter_list|(
name|int
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
DECL|method|term
specifier|public
name|String
name|term
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|toString
argument_list|(
name|term
argument_list|)
return|;
block|}
DECL|method|getTerm
specifier|public
name|String
name|getTerm
parameter_list|()
block|{
return|return
name|term
argument_list|()
return|;
block|}
DECL|method|termAsNumber
annotation|@
name|Override
specifier|public
name|Number
name|termAsNumber
parameter_list|()
block|{
return|return
name|term
return|;
block|}
DECL|method|getTermAsNumber
annotation|@
name|Override
specifier|public
name|Number
name|getTermAsNumber
parameter_list|()
block|{
return|return
name|termAsNumber
argument_list|()
return|;
block|}
DECL|method|count
specifier|public
name|int
name|count
parameter_list|()
block|{
return|return
name|count
return|;
block|}
DECL|method|getCount
specifier|public
name|int
name|getCount
parameter_list|()
block|{
return|return
name|count
argument_list|()
return|;
block|}
DECL|method|compareTo
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
name|Entry
name|o
parameter_list|)
block|{
name|int
name|anotherVal
init|=
operator|(
operator|(
name|IntEntry
operator|)
name|o
operator|)
operator|.
name|term
decl_stmt|;
name|int
name|i
init|=
name|term
operator|-
name|anotherVal
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
name|count
operator|-
name|o
operator|.
name|count
argument_list|()
expr_stmt|;
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
block|}
return|return
name|i
return|;
block|}
block|}
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|fieldName
specifier|private
name|String
name|fieldName
decl_stmt|;
DECL|field|requiredSize
name|int
name|requiredSize
decl_stmt|;
DECL|field|entries
name|Collection
argument_list|<
name|IntEntry
argument_list|>
name|entries
init|=
name|ImmutableList
operator|.
name|of
argument_list|()
decl_stmt|;
DECL|field|comparatorType
specifier|private
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|method|InternalIntTermsFacet
name|InternalIntTermsFacet
parameter_list|()
block|{     }
DECL|method|InternalIntTermsFacet
specifier|public
name|InternalIntTermsFacet
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|ComparatorType
name|comparatorType
parameter_list|,
name|int
name|requiredSize
parameter_list|,
name|Collection
argument_list|<
name|IntEntry
argument_list|>
name|entries
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
name|fieldName
operator|=
name|fieldName
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
block|}
DECL|method|name
annotation|@
name|Override
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|this
operator|.
name|name
return|;
block|}
DECL|method|getName
annotation|@
name|Override
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
DECL|method|fieldName
annotation|@
name|Override
specifier|public
name|String
name|fieldName
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldName
return|;
block|}
DECL|method|getFieldName
annotation|@
name|Override
specifier|public
name|String
name|getFieldName
parameter_list|()
block|{
return|return
name|fieldName
argument_list|()
return|;
block|}
DECL|method|type
annotation|@
name|Override
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
DECL|method|getType
annotation|@
name|Override
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|type
argument_list|()
return|;
block|}
DECL|method|comparatorType
annotation|@
name|Override
specifier|public
name|ComparatorType
name|comparatorType
parameter_list|()
block|{
return|return
name|comparatorType
return|;
block|}
DECL|method|getComparatorType
annotation|@
name|Override
specifier|public
name|ComparatorType
name|getComparatorType
parameter_list|()
block|{
return|return
name|comparatorType
argument_list|()
return|;
block|}
DECL|method|entries
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|IntEntry
argument_list|>
name|entries
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
name|IntEntry
argument_list|>
operator|)
name|entries
return|;
block|}
DECL|method|getEntries
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|IntEntry
argument_list|>
name|getEntries
parameter_list|()
block|{
return|return
name|entries
argument_list|()
return|;
block|}
DECL|method|iterator
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
annotation|@
name|Override
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
DECL|field|aggregateCache
specifier|private
specifier|static
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|TIntIntHashMap
argument_list|>
argument_list|>
name|aggregateCache
init|=
operator|new
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|TIntIntHashMap
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|TIntIntHashMap
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|TIntIntHashMap
argument_list|>
argument_list|(
operator|new
name|TIntIntHashMap
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|method|reduce
annotation|@
name|Override
specifier|public
name|Facet
name|reduce
parameter_list|(
name|String
name|name
parameter_list|,
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
name|InternalIntTermsFacet
name|first
init|=
operator|(
name|InternalIntTermsFacet
operator|)
name|facets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|TIntIntHashMap
name|aggregated
init|=
name|aggregateCache
operator|.
name|get
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|aggregated
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|Facet
name|facet
range|:
name|facets
control|)
block|{
name|InternalIntTermsFacet
name|mFacet
init|=
operator|(
name|InternalIntTermsFacet
operator|)
name|facet
decl_stmt|;
for|for
control|(
name|IntEntry
name|entry
range|:
name|mFacet
operator|.
name|entries
control|)
block|{
name|aggregated
operator|.
name|adjustOrPutValue
argument_list|(
name|entry
operator|.
name|term
argument_list|,
name|entry
operator|.
name|count
argument_list|()
argument_list|,
name|entry
operator|.
name|count
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|BoundedTreeSet
argument_list|<
name|IntEntry
argument_list|>
name|ordered
init|=
operator|new
name|BoundedTreeSet
argument_list|<
name|IntEntry
argument_list|>
argument_list|(
name|first
operator|.
name|comparatorType
argument_list|()
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
name|TIntIntIterator
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
name|IntEntry
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
DECL|field|_FIELD
specifier|static
specifier|final
name|XContentBuilderString
name|_FIELD
init|=
operator|new
name|XContentBuilderString
argument_list|(
literal|"_field"
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
DECL|method|toXContent
annotation|@
name|Override
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
name|_FIELD
argument_list|,
name|fieldName
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
name|IntEntry
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
name|count
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
name|InternalIntTermsFacet
name|readTermsFacet
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalIntTermsFacet
name|facet
init|=
operator|new
name|InternalIntTermsFacet
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
DECL|method|readFrom
annotation|@
name|Override
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
name|readUTF
argument_list|()
expr_stmt|;
name|fieldName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
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
name|IntEntry
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
name|IntEntry
argument_list|(
name|in
operator|.
name|readInt
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
DECL|method|writeTo
annotation|@
name|Override
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
name|writeUTF
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|fieldName
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
name|IntEntry
name|entry
range|:
name|entries
control|)
block|{
name|out
operator|.
name|writeInt
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
name|count
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

