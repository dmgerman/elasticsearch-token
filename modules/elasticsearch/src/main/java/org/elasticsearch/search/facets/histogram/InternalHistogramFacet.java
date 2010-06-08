begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facets.histogram
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facets
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
name|search
operator|.
name|facets
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
name|facets
operator|.
name|internal
operator|.
name|InternalFacet
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
name|util
operator|.
name|gnu
operator|.
name|trove
operator|.
name|TLongDoubleHashMap
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
name|gnu
operator|.
name|trove
operator|.
name|TLongDoubleIterator
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
name|gnu
operator|.
name|trove
operator|.
name|TLongLongHashMap
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
name|gnu
operator|.
name|trove
operator|.
name|TLongLongIterator
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
name|util
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
name|util
operator|.
name|xcontent
operator|.
name|builder
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|InternalHistogramFacet
specifier|public
class|class
name|InternalHistogramFacet
implements|implements
name|HistogramFacet
implements|,
name|InternalFacet
block|{
DECL|field|EMPTY_LONG_LONG_MAP
specifier|private
specifier|static
specifier|final
name|TLongLongHashMap
name|EMPTY_LONG_LONG_MAP
init|=
operator|new
name|TLongLongHashMap
argument_list|()
decl_stmt|;
DECL|field|EMPTY_LONG_DOUBLE_MAP
specifier|private
specifier|static
specifier|final
name|TLongDoubleHashMap
name|EMPTY_LONG_DOUBLE_MAP
init|=
operator|new
name|TLongDoubleHashMap
argument_list|()
decl_stmt|;
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|keyFieldName
specifier|private
name|String
name|keyFieldName
decl_stmt|;
DECL|field|valueFieldName
specifier|private
name|String
name|valueFieldName
decl_stmt|;
DECL|field|interval
specifier|private
name|long
name|interval
decl_stmt|;
DECL|field|comparatorType
specifier|private
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|field|counts
specifier|private
name|TLongLongHashMap
name|counts
decl_stmt|;
DECL|field|totals
specifier|private
name|TLongDoubleHashMap
name|totals
decl_stmt|;
DECL|field|entries
specifier|private
name|Collection
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
literal|null
decl_stmt|;
DECL|method|InternalHistogramFacet
specifier|private
name|InternalHistogramFacet
parameter_list|()
block|{     }
DECL|method|InternalHistogramFacet
specifier|public
name|InternalHistogramFacet
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|keyFieldName
parameter_list|,
name|String
name|valueFieldName
parameter_list|,
name|long
name|interval
parameter_list|,
name|ComparatorType
name|comparatorType
parameter_list|,
name|TLongLongHashMap
name|counts
parameter_list|,
name|TLongDoubleHashMap
name|totals
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
name|keyFieldName
operator|=
name|keyFieldName
expr_stmt|;
name|this
operator|.
name|valueFieldName
operator|=
name|valueFieldName
expr_stmt|;
name|this
operator|.
name|interval
operator|=
name|interval
expr_stmt|;
name|this
operator|.
name|comparatorType
operator|=
name|comparatorType
expr_stmt|;
name|this
operator|.
name|counts
operator|=
name|counts
expr_stmt|;
name|this
operator|.
name|totals
operator|=
name|totals
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
name|name
argument_list|()
return|;
block|}
DECL|method|keyFieldName
annotation|@
name|Override
specifier|public
name|String
name|keyFieldName
parameter_list|()
block|{
return|return
name|this
operator|.
name|keyFieldName
return|;
block|}
DECL|method|getKeyFieldName
annotation|@
name|Override
specifier|public
name|String
name|getKeyFieldName
parameter_list|()
block|{
return|return
name|keyFieldName
argument_list|()
return|;
block|}
DECL|method|valueFieldName
annotation|@
name|Override
specifier|public
name|String
name|valueFieldName
parameter_list|()
block|{
return|return
name|this
operator|.
name|valueFieldName
return|;
block|}
DECL|method|getValueFieldName
annotation|@
name|Override
specifier|public
name|String
name|getValueFieldName
parameter_list|()
block|{
return|return
name|valueFieldName
argument_list|()
return|;
block|}
DECL|method|type
annotation|@
name|Override
specifier|public
name|Type
name|type
parameter_list|()
block|{
return|return
name|Type
operator|.
name|HISTOGRAM
return|;
block|}
DECL|method|getType
annotation|@
name|Override
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|type
argument_list|()
return|;
block|}
DECL|method|entries
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|()
block|{
name|computeEntries
argument_list|()
expr_stmt|;
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
name|Entry
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
name|Entry
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
name|computeEntries
argument_list|()
operator|.
name|iterator
argument_list|()
return|;
block|}
DECL|method|computeEntries
specifier|private
name|Collection
argument_list|<
name|Entry
argument_list|>
name|computeEntries
parameter_list|()
block|{
if|if
condition|(
name|entries
operator|!=
literal|null
condition|)
block|{
return|return
name|entries
return|;
block|}
name|TreeSet
argument_list|<
name|Entry
argument_list|>
name|set
init|=
operator|new
name|TreeSet
argument_list|<
name|Entry
argument_list|>
argument_list|(
name|comparatorType
operator|.
name|comparator
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TLongLongIterator
name|it
init|=
name|counts
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
name|set
operator|.
name|add
argument_list|(
operator|new
name|Entry
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
argument_list|,
name|totals
operator|.
name|get
argument_list|(
name|it
operator|.
name|key
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|entries
operator|=
name|set
expr_stmt|;
return|return
name|entries
return|;
block|}
DECL|method|aggregate
annotation|@
name|Override
specifier|public
name|Facet
name|aggregate
parameter_list|(
name|Iterable
argument_list|<
name|Facet
argument_list|>
name|facets
parameter_list|)
block|{
name|TLongLongHashMap
name|counts
init|=
literal|null
decl_stmt|;
name|TLongDoubleHashMap
name|totals
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Facet
name|facet
range|:
name|facets
control|)
block|{
if|if
condition|(
operator|!
name|facet
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|InternalHistogramFacet
name|histoFacet
init|=
operator|(
name|InternalHistogramFacet
operator|)
name|facet
decl_stmt|;
if|if
condition|(
operator|!
name|histoFacet
operator|.
name|counts
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|counts
operator|==
literal|null
condition|)
block|{
name|counts
operator|=
name|histoFacet
operator|.
name|counts
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|TLongLongIterator
name|it
init|=
name|histoFacet
operator|.
name|counts
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
name|counts
operator|.
name|adjustOrPutValue
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
argument_list|,
name|it
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|histoFacet
operator|.
name|totals
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|totals
operator|==
literal|null
condition|)
block|{
name|totals
operator|=
name|histoFacet
operator|.
name|totals
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|TLongDoubleIterator
name|it
init|=
name|histoFacet
operator|.
name|totals
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
name|totals
operator|.
name|adjustOrPutValue
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
argument_list|,
name|it
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
name|counts
operator|==
literal|null
condition|)
block|{
name|counts
operator|=
name|EMPTY_LONG_LONG_MAP
expr_stmt|;
block|}
if|if
condition|(
name|totals
operator|==
literal|null
condition|)
block|{
name|totals
operator|=
name|EMPTY_LONG_DOUBLE_MAP
expr_stmt|;
block|}
return|return
operator|new
name|InternalHistogramFacet
argument_list|(
name|name
argument_list|,
name|keyFieldName
argument_list|,
name|valueFieldName
argument_list|,
name|interval
argument_list|,
name|comparatorType
argument_list|,
name|counts
argument_list|,
name|totals
argument_list|)
return|;
block|}
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|void
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
literal|"_type"
argument_list|,
literal|"histogram"
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"_key_field"
argument_list|,
name|keyFieldName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"_value_field"
argument_list|,
name|valueFieldName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"_comparator"
argument_list|,
name|comparatorType
operator|.
name|description
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"_interval"
argument_list|,
name|interval
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startArray
argument_list|(
literal|"entries"
argument_list|)
expr_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|computeEntries
argument_list|()
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
literal|"key"
argument_list|,
name|entry
operator|.
name|key
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"count"
argument_list|,
name|entry
operator|.
name|count
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"total"
argument_list|,
name|entry
operator|.
name|total
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"mean"
argument_list|,
name|entry
operator|.
name|mean
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
block|}
DECL|method|readHistogramFacet
specifier|public
specifier|static
name|InternalHistogramFacet
name|readHistogramFacet
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|InternalHistogramFacet
name|facet
init|=
operator|new
name|InternalHistogramFacet
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
name|keyFieldName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|valueFieldName
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|interval
operator|=
name|in
operator|.
name|readVLong
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
name|int
name|size
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|counts
operator|=
name|EMPTY_LONG_LONG_MAP
expr_stmt|;
name|totals
operator|=
name|EMPTY_LONG_DOUBLE_MAP
expr_stmt|;
block|}
else|else
block|{
name|counts
operator|=
operator|new
name|TLongLongHashMap
argument_list|(
name|size
argument_list|)
expr_stmt|;
name|totals
operator|=
operator|new
name|TLongDoubleHashMap
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
name|long
name|key
init|=
name|in
operator|.
name|readLong
argument_list|()
decl_stmt|;
name|counts
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|in
operator|.
name|readVLong
argument_list|()
argument_list|)
expr_stmt|;
name|totals
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|in
operator|.
name|readDouble
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|keyFieldName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|valueFieldName
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|interval
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
comment|// optimize the write, since we know we have the same buckets as keys
name|out
operator|.
name|writeVInt
argument_list|(
name|counts
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|TLongLongIterator
name|it
init|=
name|counts
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
name|out
operator|.
name|writeLong
argument_list|(
name|it
operator|.
name|key
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|it
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|totals
operator|.
name|get
argument_list|(
name|it
operator|.
name|key
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

