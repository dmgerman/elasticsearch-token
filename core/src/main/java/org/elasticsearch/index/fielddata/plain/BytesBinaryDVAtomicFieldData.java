begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|index
operator|.
name|BinaryDocValues
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
name|store
operator|.
name|ByteArrayDataInput
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
name|ArrayUtil
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
name|BytesRefBuilder
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
name|RamUsageEstimator
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
name|SortedBinaryDocValues
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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
name|Collections
import|;
end_import

begin_class
DECL|class|BytesBinaryDVAtomicFieldData
specifier|final
class|class
name|BytesBinaryDVAtomicFieldData
implements|implements
name|AtomicFieldData
block|{
DECL|field|values
specifier|private
specifier|final
name|BinaryDocValues
name|values
decl_stmt|;
DECL|method|BytesBinaryDVAtomicFieldData
name|BytesBinaryDVAtomicFieldData
parameter_list|(
name|BinaryDocValues
name|values
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|ramBytesUsed
specifier|public
name|long
name|ramBytesUsed
parameter_list|()
block|{
return|return
literal|0
return|;
comment|// not exposed by Lucene
block|}
annotation|@
name|Override
DECL|method|getChildResources
specifier|public
name|Collection
argument_list|<
name|Accountable
argument_list|>
name|getChildResources
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|emptyList
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getBytesValues
specifier|public
name|SortedBinaryDocValues
name|getBytesValues
parameter_list|()
block|{
return|return
operator|new
name|SortedBinaryDocValues
argument_list|()
block|{
name|int
name|count
decl_stmt|;
name|BytesRefBuilder
index|[]
name|refs
init|=
operator|new
name|BytesRefBuilder
index|[
literal|0
index|]
decl_stmt|;
specifier|final
name|ByteArrayDataInput
name|in
init|=
operator|new
name|ByteArrayDataInput
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
specifier|final
name|BytesRef
name|bytes
init|=
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|)
decl_stmt|;
name|in
operator|.
name|reset
argument_list|(
name|bytes
operator|.
name|bytes
argument_list|,
name|bytes
operator|.
name|offset
argument_list|,
name|bytes
operator|.
name|length
argument_list|)
expr_stmt|;
if|if
condition|(
name|bytes
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|count
operator|=
literal|0
expr_stmt|;
block|}
else|else
block|{
name|count
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
if|if
condition|(
name|count
operator|>
name|refs
operator|.
name|length
condition|)
block|{
specifier|final
name|int
name|previousLength
init|=
name|refs
operator|.
name|length
decl_stmt|;
name|refs
operator|=
name|Arrays
operator|.
name|copyOf
argument_list|(
name|refs
argument_list|,
name|ArrayUtil
operator|.
name|oversize
argument_list|(
name|count
argument_list|,
name|RamUsageEstimator
operator|.
name|NUM_BYTES_OBJECT_REF
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
name|previousLength
init|;
name|i
operator|<
name|refs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|refs
index|[
name|i
index|]
operator|=
operator|new
name|BytesRefBuilder
argument_list|()
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|count
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|int
name|length
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
specifier|final
name|BytesRefBuilder
name|scratch
init|=
name|refs
index|[
name|i
index|]
decl_stmt|;
name|scratch
operator|.
name|grow
argument_list|(
name|length
argument_list|)
expr_stmt|;
name|in
operator|.
name|readBytes
argument_list|(
name|scratch
operator|.
name|bytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|scratch
operator|.
name|setLength
argument_list|(
name|length
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|count
parameter_list|()
block|{
return|return
name|count
return|;
block|}
annotation|@
name|Override
specifier|public
name|BytesRef
name|valueAt
parameter_list|(
name|int
name|index
parameter_list|)
block|{
return|return
name|refs
index|[
name|index
index|]
operator|.
name|get
argument_list|()
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|getScriptValues
specifier|public
name|ScriptDocValues
name|getScriptValues
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
comment|// no-op
block|}
block|}
end_class

end_unit
