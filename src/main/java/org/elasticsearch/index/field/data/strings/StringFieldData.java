begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.field.data.strings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|field
operator|.
name|data
operator|.
name|strings
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
name|common
operator|.
name|RamUsage
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
name|field
operator|.
name|data
operator|.
name|FieldData
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
name|field
operator|.
name|data
operator|.
name|FieldDataType
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
name|field
operator|.
name|data
operator|.
name|support
operator|.
name|FieldDataLoader
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|StringFieldData
specifier|public
specifier|abstract
class|class
name|StringFieldData
extends|extends
name|FieldData
argument_list|<
name|StringDocFieldData
argument_list|>
block|{
DECL|field|values
specifier|protected
specifier|final
name|BytesRef
index|[]
name|values
decl_stmt|;
DECL|method|StringFieldData
specifier|protected
name|StringFieldData
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|BytesRef
index|[]
name|values
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|)
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
DECL|method|computeSizeInBytes
specifier|protected
name|long
name|computeSizeInBytes
parameter_list|()
block|{
name|long
name|size
init|=
name|RamUsage
operator|.
name|NUM_BYTES_ARRAY_HEADER
decl_stmt|;
for|for
control|(
name|BytesRef
name|value
range|:
name|values
control|)
block|{
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|size
operator|+=
name|RamUsage
operator|.
name|NUM_BYTES_OBJECT_HEADER
operator|+
operator|(
name|value
operator|.
name|length
operator|+
operator|(
literal|2
operator|*
name|RamUsage
operator|.
name|NUM_BYTES_INT
operator|)
operator|)
expr_stmt|;
block|}
block|}
return|return
name|size
return|;
block|}
DECL|method|values
specifier|public
name|BytesRef
index|[]
name|values
parameter_list|()
block|{
return|return
name|this
operator|.
name|values
return|;
block|}
DECL|method|value
specifier|abstract
specifier|public
name|BytesRef
name|value
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|values
specifier|abstract
specifier|public
name|BytesRef
index|[]
name|values
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
annotation|@
name|Override
DECL|method|docFieldData
specifier|public
name|StringDocFieldData
name|docFieldData
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|super
operator|.
name|docFieldData
argument_list|(
name|docId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|stringValue
specifier|public
name|BytesRef
name|stringValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|value
argument_list|(
name|docId
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createFieldData
specifier|protected
name|StringDocFieldData
name|createFieldData
parameter_list|()
block|{
return|return
operator|new
name|StringDocFieldData
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|type
specifier|public
name|FieldDataType
name|type
parameter_list|()
block|{
return|return
name|FieldDataType
operator|.
name|DefaultTypes
operator|.
name|STRING
return|;
block|}
annotation|@
name|Override
DECL|method|forEachValue
specifier|public
name|void
name|forEachValue
parameter_list|(
name|StringValueProc
name|proc
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|proc
operator|.
name|onValue
argument_list|(
name|values
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|load
specifier|public
specifier|static
name|StringFieldData
name|load
parameter_list|(
name|AtomicReader
name|reader
parameter_list|,
name|String
name|field
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|FieldDataLoader
operator|.
name|load
argument_list|(
name|reader
argument_list|,
name|field
argument_list|,
operator|new
name|StringTypeLoader
argument_list|()
argument_list|)
return|;
block|}
DECL|class|StringTypeLoader
specifier|static
class|class
name|StringTypeLoader
extends|extends
name|FieldDataLoader
operator|.
name|FreqsTypeLoader
argument_list|<
name|StringFieldData
argument_list|>
block|{
DECL|field|terms
specifier|private
specifier|final
name|ArrayList
argument_list|<
name|BytesRef
argument_list|>
name|terms
init|=
operator|new
name|ArrayList
argument_list|<
name|BytesRef
argument_list|>
argument_list|()
decl_stmt|;
DECL|method|StringTypeLoader
name|StringTypeLoader
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
comment|// the first one indicates null value
name|terms
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collectTerm
specifier|public
name|void
name|collectTerm
parameter_list|(
name|BytesRef
name|term
parameter_list|)
block|{
name|terms
operator|.
name|add
argument_list|(
name|term
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|buildSingleValue
specifier|public
name|StringFieldData
name|buildSingleValue
parameter_list|(
name|String
name|field
parameter_list|,
name|int
index|[]
name|ordinals
parameter_list|)
block|{
return|return
operator|new
name|SingleValueStringFieldData
argument_list|(
name|field
argument_list|,
name|ordinals
argument_list|,
name|terms
operator|.
name|toArray
argument_list|(
operator|new
name|BytesRef
index|[
name|terms
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|buildMultiValue
specifier|public
name|StringFieldData
name|buildMultiValue
parameter_list|(
name|String
name|field
parameter_list|,
name|int
index|[]
index|[]
name|ordinals
parameter_list|)
block|{
return|return
operator|new
name|MultiValueStringFieldData
argument_list|(
name|field
argument_list|,
name|ordinals
argument_list|,
name|terms
operator|.
name|toArray
argument_list|(
operator|new
name|BytesRef
index|[
name|terms
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

