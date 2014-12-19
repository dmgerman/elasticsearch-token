begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.engine
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|engine
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
name|Iterators
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
name|Accountable
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
name|Accountables
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
name|Nullable
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
name|lucene
operator|.
name|Lucene
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
name|ByteSizeValue
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

begin_class
DECL|class|Segment
specifier|public
class|class
name|Segment
implements|implements
name|Streamable
block|{
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|generation
specifier|private
name|long
name|generation
decl_stmt|;
DECL|field|committed
specifier|public
name|boolean
name|committed
decl_stmt|;
DECL|field|search
specifier|public
name|boolean
name|search
decl_stmt|;
DECL|field|sizeInBytes
specifier|public
name|long
name|sizeInBytes
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|docCount
specifier|public
name|int
name|docCount
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|delDocCount
specifier|public
name|int
name|delDocCount
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|version
specifier|public
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
name|version
init|=
literal|null
decl_stmt|;
DECL|field|compound
specifier|public
name|Boolean
name|compound
init|=
literal|null
decl_stmt|;
DECL|field|mergeId
specifier|public
name|String
name|mergeId
decl_stmt|;
DECL|field|memoryInBytes
specifier|public
name|long
name|memoryInBytes
decl_stmt|;
DECL|field|ramTree
specifier|public
name|Accountable
name|ramTree
init|=
literal|null
decl_stmt|;
DECL|method|Segment
name|Segment
parameter_list|()
block|{     }
DECL|method|Segment
specifier|public
name|Segment
parameter_list|(
name|String
name|name
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
name|generation
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|name
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|,
name|Character
operator|.
name|MAX_RADIX
argument_list|)
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
DECL|method|getGeneration
specifier|public
name|long
name|getGeneration
parameter_list|()
block|{
return|return
name|this
operator|.
name|generation
return|;
block|}
DECL|method|isCommitted
specifier|public
name|boolean
name|isCommitted
parameter_list|()
block|{
return|return
name|this
operator|.
name|committed
return|;
block|}
DECL|method|isSearch
specifier|public
name|boolean
name|isSearch
parameter_list|()
block|{
return|return
name|this
operator|.
name|search
return|;
block|}
DECL|method|getNumDocs
specifier|public
name|int
name|getNumDocs
parameter_list|()
block|{
return|return
name|this
operator|.
name|docCount
return|;
block|}
DECL|method|getDeletedDocs
specifier|public
name|int
name|getDeletedDocs
parameter_list|()
block|{
return|return
name|this
operator|.
name|delDocCount
return|;
block|}
DECL|method|getSize
specifier|public
name|ByteSizeValue
name|getSize
parameter_list|()
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
name|sizeInBytes
argument_list|)
return|;
block|}
DECL|method|getSizeInBytes
specifier|public
name|long
name|getSizeInBytes
parameter_list|()
block|{
return|return
name|this
operator|.
name|sizeInBytes
return|;
block|}
DECL|method|getVersion
specifier|public
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Version
name|getVersion
parameter_list|()
block|{
return|return
name|version
return|;
block|}
annotation|@
name|Nullable
DECL|method|isCompound
specifier|public
name|Boolean
name|isCompound
parameter_list|()
block|{
return|return
name|compound
return|;
block|}
comment|/**      * If set, a string representing that the segment is part of a merge, with the value representing the      * group of segments that represent this merge.      */
annotation|@
name|Nullable
DECL|method|getMergeId
specifier|public
name|String
name|getMergeId
parameter_list|()
block|{
return|return
name|this
operator|.
name|mergeId
return|;
block|}
comment|/**      * Estimation of the memory usage used by a segment.      */
DECL|method|getMemoryInBytes
specifier|public
name|long
name|getMemoryInBytes
parameter_list|()
block|{
return|return
name|this
operator|.
name|memoryInBytes
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
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
name|Segment
name|segment
init|=
operator|(
name|Segment
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|name
operator|!=
literal|null
condition|?
operator|!
name|name
operator|.
name|equals
argument_list|(
name|segment
operator|.
name|name
argument_list|)
else|:
name|segment
operator|.
name|name
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
return|return
literal|true
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
name|name
operator|!=
literal|null
condition|?
name|name
operator|.
name|hashCode
argument_list|()
else|:
literal|0
return|;
block|}
DECL|method|readSegment
specifier|public
specifier|static
name|Segment
name|readSegment
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|Segment
name|segment
init|=
operator|new
name|Segment
argument_list|()
decl_stmt|;
name|segment
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|segment
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
name|generation
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|name
operator|.
name|substring
argument_list|(
literal|1
argument_list|)
argument_list|,
name|Character
operator|.
name|MAX_RADIX
argument_list|)
expr_stmt|;
name|committed
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|search
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|docCount
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|delDocCount
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|sizeInBytes
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|version
operator|=
name|Lucene
operator|.
name|parseVersionLenient
argument_list|(
name|in
operator|.
name|readOptionalString
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|compound
operator|=
name|in
operator|.
name|readOptionalBoolean
argument_list|()
expr_stmt|;
name|mergeId
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|memoryInBytes
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
comment|// verbose mode
name|ramTree
operator|=
name|readRamTree
argument_list|(
name|in
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
name|writeString
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|committed
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|search
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|docCount
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|delDocCount
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|sizeInBytes
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|version
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalBoolean
argument_list|(
name|compound
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalString
argument_list|(
name|mergeId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|memoryInBytes
argument_list|)
expr_stmt|;
name|boolean
name|verbose
init|=
name|ramTree
operator|!=
literal|null
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|verbose
argument_list|)
expr_stmt|;
if|if
condition|(
name|verbose
condition|)
block|{
name|writeRamTree
argument_list|(
name|out
argument_list|,
name|ramTree
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|readRamTree
name|Accountable
name|readRamTree
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|String
name|name
init|=
name|in
operator|.
name|readString
argument_list|()
decl_stmt|;
specifier|final
name|long
name|bytes
init|=
name|in
operator|.
name|readVLong
argument_list|()
decl_stmt|;
name|int
name|numChildren
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|numChildren
operator|==
literal|0
condition|)
block|{
return|return
name|Accountables
operator|.
name|namedAccountable
argument_list|(
name|name
argument_list|,
name|bytes
argument_list|)
return|;
block|}
name|List
argument_list|<
name|Accountable
argument_list|>
name|children
init|=
operator|new
name|ArrayList
argument_list|(
name|numChildren
argument_list|)
decl_stmt|;
while|while
condition|(
name|numChildren
operator|--
operator|>
literal|0
condition|)
block|{
name|children
operator|.
name|add
argument_list|(
name|readRamTree
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|Accountables
operator|.
name|namedAccountable
argument_list|(
name|name
argument_list|,
name|children
argument_list|,
name|bytes
argument_list|)
return|;
block|}
comment|// the ram tree is written recursively since the depth is fairly low (5 or 6)
DECL|method|writeRamTree
name|void
name|writeRamTree
parameter_list|(
name|StreamOutput
name|out
parameter_list|,
name|Accountable
name|tree
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeString
argument_list|(
name|tree
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVLong
argument_list|(
name|tree
operator|.
name|ramBytesUsed
argument_list|()
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|Accountable
argument_list|>
name|children
init|=
name|tree
operator|.
name|getChildResources
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|children
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Accountable
name|child
range|:
name|children
control|)
block|{
name|writeRamTree
argument_list|(
name|out
argument_list|,
name|child
argument_list|)
expr_stmt|;
block|}
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
literal|"Segment{"
operator|+
literal|"name='"
operator|+
name|name
operator|+
literal|'\''
operator|+
literal|", generation="
operator|+
name|generation
operator|+
literal|", committed="
operator|+
name|committed
operator|+
literal|", search="
operator|+
name|search
operator|+
literal|", sizeInBytes="
operator|+
name|sizeInBytes
operator|+
literal|", docCount="
operator|+
name|docCount
operator|+
literal|", delDocCount="
operator|+
name|delDocCount
operator|+
literal|", version='"
operator|+
name|version
operator|+
literal|'\''
operator|+
literal|", compound="
operator|+
name|compound
operator|+
literal|", mergeId='"
operator|+
name|mergeId
operator|+
literal|'\''
operator|+
literal|", memoryInBytes="
operator|+
name|memoryInBytes
operator|+
literal|'}'
return|;
block|}
block|}
end_class

end_unit

