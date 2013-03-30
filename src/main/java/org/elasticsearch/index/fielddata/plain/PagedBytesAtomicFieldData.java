begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.plain
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|plain
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
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|PagedBytes
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
name|PagedBytes
operator|.
name|Reader
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
name|packed
operator|.
name|GrowableWriter
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
name|packed
operator|.
name|PackedInts
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|AtomicFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ScriptDocValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ordinals
operator|.
name|EmptyOrdinals
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ordinals
operator|.
name|Ordinals
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ordinals
operator|.
name|Ordinals
operator|.
name|Docs
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|PagedBytesAtomicFieldData
specifier|public
class|class
name|PagedBytesAtomicFieldData
implements|implements
name|AtomicFieldData
operator|.
name|WithOrdinals
argument_list|<
name|ScriptDocValues
operator|.
name|Strings
argument_list|>
block|{
DECL|method|empty
specifier|public
specifier|static
name|PagedBytesAtomicFieldData
name|empty
parameter_list|(
name|int
name|numDocs
parameter_list|)
block|{
return|return
operator|new
name|Empty
argument_list|(
name|numDocs
argument_list|)
return|;
block|}
comment|// 0 ordinal in values means no value (its null)
DECL|field|bytes
specifier|private
specifier|final
name|PagedBytes
operator|.
name|Reader
name|bytes
decl_stmt|;
DECL|field|termOrdToBytesOffset
specifier|private
specifier|final
name|PackedInts
operator|.
name|Reader
name|termOrdToBytesOffset
decl_stmt|;
DECL|field|ordinals
specifier|protected
specifier|final
name|Ordinals
name|ordinals
decl_stmt|;
DECL|field|hashes
specifier|private
specifier|volatile
name|int
index|[]
name|hashes
decl_stmt|;
DECL|field|size
specifier|private
name|long
name|size
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|readerBytesSize
specifier|private
specifier|final
name|long
name|readerBytesSize
decl_stmt|;
DECL|method|PagedBytesAtomicFieldData
specifier|public
name|PagedBytesAtomicFieldData
parameter_list|(
name|PagedBytes
operator|.
name|Reader
name|bytes
parameter_list|,
name|long
name|readerBytesSize
parameter_list|,
name|PackedInts
operator|.
name|Reader
name|termOrdToBytesOffset
parameter_list|,
name|Ordinals
name|ordinals
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|termOrdToBytesOffset
operator|=
name|termOrdToBytesOffset
expr_stmt|;
name|this
operator|.
name|ordinals
operator|=
name|ordinals
expr_stmt|;
name|this
operator|.
name|readerBytesSize
operator|=
name|readerBytesSize
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{     }
annotation|@
name|Override
DECL|method|isMultiValued
specifier|public
name|boolean
name|isMultiValued
parameter_list|()
block|{
return|return
name|ordinals
operator|.
name|isMultiValued
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getNumDocs
specifier|public
name|int
name|getNumDocs
parameter_list|()
block|{
return|return
name|ordinals
operator|.
name|getNumDocs
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|isValuesOrdered
specifier|public
name|boolean
name|isValuesOrdered
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|getMemorySizeInBytes
specifier|public
name|long
name|getMemorySizeInBytes
parameter_list|()
block|{
if|if
condition|(
name|size
operator|==
operator|-
literal|1
condition|)
block|{
name|long
name|size
init|=
name|ordinals
operator|.
name|getMemorySizeInBytes
argument_list|()
decl_stmt|;
comment|// PackedBytes
name|size
operator|+=
name|readerBytesSize
expr_stmt|;
comment|// PackedInts
name|size
operator|+=
name|termOrdToBytesOffset
operator|.
name|ramBytesUsed
argument_list|()
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
block|}
return|return
name|size
return|;
block|}
DECL|method|getHashes
specifier|private
specifier|final
name|int
index|[]
name|getHashes
parameter_list|()
block|{
if|if
condition|(
name|hashes
operator|==
literal|null
condition|)
block|{
name|int
name|numberOfValues
init|=
name|termOrdToBytesOffset
operator|.
name|size
argument_list|()
decl_stmt|;
name|int
index|[]
name|hashes
init|=
operator|new
name|int
index|[
name|numberOfValues
index|]
decl_stmt|;
name|BytesRef
name|scratch
init|=
operator|new
name|BytesRef
argument_list|()
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
name|numberOfValues
condition|;
name|i
operator|++
control|)
block|{
name|bytes
operator|.
name|fill
argument_list|(
name|scratch
argument_list|,
name|termOrdToBytesOffset
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|hashes
index|[
name|i
index|]
operator|=
name|scratch
operator|.
name|hashCode
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|hashes
operator|=
name|hashes
expr_stmt|;
block|}
return|return
name|hashes
return|;
block|}
annotation|@
name|Override
DECL|method|getBytesValues
specifier|public
name|BytesValues
operator|.
name|WithOrdinals
name|getBytesValues
parameter_list|()
block|{
return|return
name|ordinals
operator|.
name|isMultiValued
argument_list|()
condition|?
operator|new
name|BytesValues
operator|.
name|Multi
argument_list|(
name|bytes
argument_list|,
name|termOrdToBytesOffset
argument_list|,
name|ordinals
operator|.
name|ordinals
argument_list|()
argument_list|)
else|:
operator|new
name|BytesValues
operator|.
name|Single
argument_list|(
name|bytes
argument_list|,
name|termOrdToBytesOffset
argument_list|,
name|ordinals
operator|.
name|ordinals
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getHashedBytesValues
specifier|public
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|BytesValues
operator|.
name|WithOrdinals
name|getHashedBytesValues
parameter_list|()
block|{
specifier|final
name|int
index|[]
name|hashes
init|=
name|getHashes
argument_list|()
decl_stmt|;
return|return
name|ordinals
operator|.
name|isMultiValued
argument_list|()
condition|?
operator|new
name|BytesValues
operator|.
name|MultiHashed
argument_list|(
name|hashes
argument_list|,
name|bytes
argument_list|,
name|termOrdToBytesOffset
argument_list|,
name|ordinals
operator|.
name|ordinals
argument_list|()
argument_list|)
else|:
operator|new
name|BytesValues
operator|.
name|SingleHashed
argument_list|(
name|hashes
argument_list|,
name|bytes
argument_list|,
name|termOrdToBytesOffset
argument_list|,
name|ordinals
operator|.
name|ordinals
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getScriptValues
specifier|public
name|ScriptDocValues
operator|.
name|Strings
name|getScriptValues
parameter_list|()
block|{
return|return
operator|new
name|ScriptDocValues
operator|.
name|Strings
argument_list|(
name|getBytesValues
argument_list|()
argument_list|)
return|;
block|}
DECL|class|BytesValues
specifier|static
specifier|abstract
class|class
name|BytesValues
extends|extends
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|BytesValues
operator|.
name|WithOrdinals
block|{
DECL|field|bytes
specifier|protected
specifier|final
name|PagedBytes
operator|.
name|Reader
name|bytes
decl_stmt|;
DECL|field|termOrdToBytesOffset
specifier|protected
specifier|final
name|PackedInts
operator|.
name|Reader
name|termOrdToBytesOffset
decl_stmt|;
DECL|field|ordinals
specifier|protected
specifier|final
name|Ordinals
operator|.
name|Docs
name|ordinals
decl_stmt|;
DECL|field|scratch
specifier|protected
specifier|final
name|BytesRef
name|scratch
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
DECL|method|BytesValues
name|BytesValues
parameter_list|(
name|PagedBytes
operator|.
name|Reader
name|bytes
parameter_list|,
name|PackedInts
operator|.
name|Reader
name|termOrdToBytesOffset
parameter_list|,
name|Ordinals
operator|.
name|Docs
name|ordinals
parameter_list|)
block|{
name|super
argument_list|(
name|ordinals
argument_list|)
expr_stmt|;
name|this
operator|.
name|bytes
operator|=
name|bytes
expr_stmt|;
name|this
operator|.
name|termOrdToBytesOffset
operator|=
name|termOrdToBytesOffset
expr_stmt|;
name|this
operator|.
name|ordinals
operator|=
name|ordinals
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|ordinals
specifier|public
name|Ordinals
operator|.
name|Docs
name|ordinals
parameter_list|()
block|{
return|return
name|this
operator|.
name|ordinals
return|;
block|}
annotation|@
name|Override
DECL|method|getValueScratchByOrd
specifier|public
name|BytesRef
name|getValueScratchByOrd
parameter_list|(
name|int
name|ord
parameter_list|,
name|BytesRef
name|ret
parameter_list|)
block|{
name|bytes
operator|.
name|fill
argument_list|(
name|ret
argument_list|,
name|termOrdToBytesOffset
operator|.
name|get
argument_list|(
name|ord
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|ret
return|;
block|}
DECL|class|Single
specifier|static
class|class
name|Single
extends|extends
name|BytesValues
block|{
DECL|field|iter
specifier|private
specifier|final
name|Iter
operator|.
name|Single
name|iter
decl_stmt|;
DECL|method|Single
name|Single
parameter_list|(
name|PagedBytes
operator|.
name|Reader
name|bytes
parameter_list|,
name|PackedInts
operator|.
name|Reader
name|termOrdToBytesOffset
parameter_list|,
name|Ordinals
operator|.
name|Docs
name|ordinals
parameter_list|)
block|{
name|super
argument_list|(
name|bytes
argument_list|,
name|termOrdToBytesOffset
argument_list|,
name|ordinals
argument_list|)
expr_stmt|;
assert|assert
operator|!
name|ordinals
operator|.
name|isMultiValued
argument_list|()
assert|;
name|iter
operator|=
name|newSingleIter
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getIter
specifier|public
name|Iter
name|getIter
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|int
name|ord
init|=
name|ordinals
operator|.
name|getOrd
argument_list|(
name|docId
argument_list|)
decl_stmt|;
if|if
condition|(
name|ord
operator|==
literal|0
condition|)
return|return
name|Iter
operator|.
name|Empty
operator|.
name|INSTANCE
return|;
name|bytes
operator|.
name|fill
argument_list|(
name|scratch
argument_list|,
name|termOrdToBytesOffset
operator|.
name|get
argument_list|(
name|ord
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|iter
operator|.
name|reset
argument_list|(
name|scratch
argument_list|,
name|ord
argument_list|)
return|;
block|}
block|}
DECL|class|SingleHashed
specifier|static
specifier|final
class|class
name|SingleHashed
extends|extends
name|Single
block|{
DECL|field|hashes
specifier|private
specifier|final
name|int
index|[]
name|hashes
decl_stmt|;
DECL|method|SingleHashed
name|SingleHashed
parameter_list|(
name|int
index|[]
name|hashes
parameter_list|,
name|Reader
name|bytes
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|packed
operator|.
name|PackedInts
operator|.
name|Reader
name|termOrdToBytesOffset
parameter_list|,
name|Docs
name|ordinals
parameter_list|)
block|{
name|super
argument_list|(
name|bytes
argument_list|,
name|termOrdToBytesOffset
argument_list|,
name|ordinals
argument_list|)
expr_stmt|;
name|this
operator|.
name|hashes
operator|=
name|hashes
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newSingleIter
specifier|protected
name|Iter
operator|.
name|Single
name|newSingleIter
parameter_list|()
block|{
return|return
operator|new
name|Iter
operator|.
name|Single
argument_list|()
block|{
specifier|public
name|int
name|hash
parameter_list|()
block|{
return|return
name|hashes
index|[
name|ord
index|]
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|getValueHashed
specifier|public
name|int
name|getValueHashed
parameter_list|(
name|int
name|docId
parameter_list|,
name|BytesRef
name|ret
parameter_list|)
block|{
specifier|final
name|int
name|ord
init|=
name|ordinals
operator|.
name|getOrd
argument_list|(
name|docId
argument_list|)
decl_stmt|;
name|getValueScratchByOrd
argument_list|(
name|ord
argument_list|,
name|ret
argument_list|)
expr_stmt|;
return|return
name|hashes
index|[
name|ord
index|]
return|;
block|}
block|}
DECL|class|Multi
specifier|static
class|class
name|Multi
extends|extends
name|BytesValues
block|{
DECL|field|iter
specifier|private
specifier|final
name|Iter
operator|.
name|Multi
name|iter
decl_stmt|;
DECL|method|Multi
name|Multi
parameter_list|(
name|PagedBytes
operator|.
name|Reader
name|bytes
parameter_list|,
name|PackedInts
operator|.
name|Reader
name|termOrdToBytesOffset
parameter_list|,
name|Ordinals
operator|.
name|Docs
name|ordinals
parameter_list|)
block|{
name|super
argument_list|(
name|bytes
argument_list|,
name|termOrdToBytesOffset
argument_list|,
name|ordinals
argument_list|)
expr_stmt|;
assert|assert
name|ordinals
operator|.
name|isMultiValued
argument_list|()
assert|;
name|this
operator|.
name|iter
operator|=
name|newMultiIter
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getIter
specifier|public
name|Iter
name|getIter
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|iter
operator|.
name|reset
argument_list|(
name|ordinals
operator|.
name|getIter
argument_list|(
name|docId
argument_list|)
argument_list|)
return|;
block|}
block|}
DECL|class|MultiHashed
specifier|static
specifier|final
class|class
name|MultiHashed
extends|extends
name|Multi
block|{
DECL|field|hashes
specifier|private
specifier|final
name|int
index|[]
name|hashes
decl_stmt|;
DECL|method|MultiHashed
name|MultiHashed
parameter_list|(
name|int
index|[]
name|hashes
parameter_list|,
name|Reader
name|bytes
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|packed
operator|.
name|PackedInts
operator|.
name|Reader
name|termOrdToBytesOffset
parameter_list|,
name|Docs
name|ordinals
parameter_list|)
block|{
name|super
argument_list|(
name|bytes
argument_list|,
name|termOrdToBytesOffset
argument_list|,
name|ordinals
argument_list|)
expr_stmt|;
name|this
operator|.
name|hashes
operator|=
name|hashes
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newMultiIter
specifier|protected
name|Iter
operator|.
name|Multi
name|newMultiIter
parameter_list|()
block|{
return|return
operator|new
name|Iter
operator|.
name|Multi
argument_list|(
name|this
argument_list|)
block|{
specifier|public
name|int
name|hash
parameter_list|()
block|{
return|return
name|hashes
index|[
name|ord
index|]
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|getValueHashed
specifier|public
name|int
name|getValueHashed
parameter_list|(
name|int
name|docId
parameter_list|,
name|BytesRef
name|ret
parameter_list|)
block|{
name|int
name|ord
init|=
name|ordinals
operator|.
name|getOrd
argument_list|(
name|docId
argument_list|)
decl_stmt|;
name|getValueScratchByOrd
argument_list|(
name|ord
argument_list|,
name|ret
argument_list|)
expr_stmt|;
return|return
name|hashes
index|[
name|ord
index|]
return|;
block|}
block|}
block|}
DECL|class|Empty
specifier|static
class|class
name|Empty
extends|extends
name|PagedBytesAtomicFieldData
block|{
DECL|method|Empty
name|Empty
parameter_list|(
name|int
name|numDocs
parameter_list|)
block|{
name|super
argument_list|(
name|emptyBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
operator|new
name|GrowableWriter
argument_list|(
literal|1
argument_list|,
literal|2
argument_list|,
name|PackedInts
operator|.
name|FASTEST
argument_list|)
operator|.
name|getMutable
argument_list|()
argument_list|,
operator|new
name|EmptyOrdinals
argument_list|(
name|numDocs
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|emptyBytes
specifier|static
name|PagedBytes
operator|.
name|Reader
name|emptyBytes
parameter_list|()
block|{
name|PagedBytes
name|bytes
init|=
operator|new
name|PagedBytes
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|bytes
operator|.
name|copyUsingLengthPrefix
argument_list|(
operator|new
name|BytesRef
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|bytes
operator|.
name|freeze
argument_list|(
literal|true
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|isMultiValued
specifier|public
name|boolean
name|isMultiValued
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
DECL|method|getNumDocs
specifier|public
name|int
name|getNumDocs
parameter_list|()
block|{
return|return
name|ordinals
operator|.
name|getNumDocs
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|isValuesOrdered
specifier|public
name|boolean
name|isValuesOrdered
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|getBytesValues
specifier|public
name|BytesValues
operator|.
name|WithOrdinals
name|getBytesValues
parameter_list|()
block|{
return|return
operator|new
name|BytesValues
operator|.
name|WithOrdinals
operator|.
name|Empty
argument_list|(
name|ordinals
operator|.
name|ordinals
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getScriptValues
specifier|public
name|ScriptDocValues
operator|.
name|Strings
name|getScriptValues
parameter_list|()
block|{
return|return
name|ScriptDocValues
operator|.
name|EMPTY_STRINGS
return|;
block|}
block|}
block|}
end_class

end_unit

