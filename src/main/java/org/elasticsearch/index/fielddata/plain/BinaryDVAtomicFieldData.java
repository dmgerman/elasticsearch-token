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
name|AtomicReader
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
name|util
operator|.
name|Bits
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
name|elasticsearch
operator|.
name|ElasticsearchIllegalStateException
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
name|BytesValues
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
name|ScriptDocValues
operator|.
name|Strings
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
comment|/** {@link AtomicFieldData} impl on top of Lucene's binary doc values. */
end_comment

begin_class
DECL|class|BinaryDVAtomicFieldData
specifier|public
class|class
name|BinaryDVAtomicFieldData
implements|implements
name|AtomicFieldData
argument_list|<
name|ScriptDocValues
operator|.
name|Strings
argument_list|>
block|{
DECL|field|reader
specifier|private
specifier|final
name|AtomicReader
name|reader
decl_stmt|;
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|method|BinaryDVAtomicFieldData
specifier|public
name|BinaryDVAtomicFieldData
parameter_list|(
name|AtomicReader
name|reader
parameter_list|,
name|String
name|field
parameter_list|)
block|{
name|this
operator|.
name|reader
operator|=
name|reader
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
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
DECL|method|getNumberUniqueValues
specifier|public
name|long
name|getNumberUniqueValues
parameter_list|()
block|{
comment|// probably not accurate, but a good upper limit
return|return
name|reader
operator|.
name|maxDoc
argument_list|()
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
comment|// TODO: Lucene doesn't expose it right now
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
DECL|method|getBytesValues
specifier|public
name|BytesValues
name|getBytesValues
parameter_list|(
name|boolean
name|needsHashes
parameter_list|)
block|{
comment|// if you want hashes to be cached, you should rather store them on disk alongside the values rather than loading them into memory
comment|// here - not supported for now, and probably not useful since this field data only applies to _id and _uid?
specifier|final
name|BinaryDocValues
name|values
decl_stmt|;
specifier|final
name|Bits
name|docsWithField
decl_stmt|;
try|try
block|{
specifier|final
name|BinaryDocValues
name|v
init|=
name|reader
operator|.
name|getBinaryDocValues
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|v
operator|==
literal|null
condition|)
block|{
comment|// segment has no value
name|values
operator|=
name|BinaryDocValues
operator|.
name|EMPTY
expr_stmt|;
name|docsWithField
operator|=
operator|new
name|Bits
operator|.
name|MatchNoBits
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|values
operator|=
name|v
expr_stmt|;
specifier|final
name|Bits
name|b
init|=
name|reader
operator|.
name|getDocsWithField
argument_list|(
name|field
argument_list|)
decl_stmt|;
name|docsWithField
operator|=
name|b
operator|==
literal|null
condition|?
operator|new
name|Bits
operator|.
name|MatchAllBits
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
else|:
name|b
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
literal|"Cannot load doc values"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
operator|new
name|BytesValues
argument_list|(
literal|false
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|int
name|setDocument
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|this
operator|.
name|docId
operator|=
name|docId
expr_stmt|;
return|return
name|docsWithField
operator|.
name|get
argument_list|(
name|docId
argument_list|)
condition|?
literal|1
else|:
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|BytesRef
name|nextValue
parameter_list|()
block|{
name|values
operator|.
name|get
argument_list|(
name|docId
argument_list|,
name|scratch
argument_list|)
expr_stmt|;
return|return
name|scratch
return|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|getScriptValues
specifier|public
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
argument_list|(
literal|false
argument_list|)
argument_list|)
return|;
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

