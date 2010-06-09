begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.field.floats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|field
operator|.
name|floats
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
name|IndexReader
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
name|search
operator|.
name|FieldCache
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
name|FieldDataOptions
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
name|NumericFieldData
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
name|support
operator|.
name|FieldDataLoader
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
name|TFloatArrayList
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
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|FloatFieldData
specifier|public
specifier|abstract
class|class
name|FloatFieldData
extends|extends
name|NumericFieldData
argument_list|<
name|FloatDocFieldData
argument_list|>
block|{
DECL|field|EMPTY_FLOAT_ARRAY
specifier|static
specifier|final
name|float
index|[]
name|EMPTY_FLOAT_ARRAY
init|=
operator|new
name|float
index|[
literal|0
index|]
decl_stmt|;
DECL|field|values
specifier|protected
specifier|final
name|float
index|[]
name|values
decl_stmt|;
DECL|field|freqs
specifier|protected
specifier|final
name|int
index|[]
name|freqs
decl_stmt|;
DECL|method|FloatFieldData
specifier|protected
name|FloatFieldData
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|FieldDataOptions
name|options
parameter_list|,
name|float
index|[]
name|values
parameter_list|,
name|int
index|[]
name|freqs
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
name|options
argument_list|)
expr_stmt|;
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|freqs
operator|=
name|freqs
expr_stmt|;
block|}
DECL|method|value
specifier|abstract
specifier|public
name|float
name|value
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|values
specifier|abstract
specifier|public
name|float
index|[]
name|values
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|docFieldData
annotation|@
name|Override
specifier|public
name|FloatDocFieldData
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
DECL|method|createFieldData
annotation|@
name|Override
specifier|protected
name|FloatDocFieldData
name|createFieldData
parameter_list|()
block|{
return|return
operator|new
name|FloatDocFieldData
argument_list|(
name|this
argument_list|)
return|;
block|}
DECL|method|stringValue
annotation|@
name|Override
specifier|public
name|String
name|stringValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
name|Float
operator|.
name|toString
argument_list|(
name|value
argument_list|(
name|docId
argument_list|)
argument_list|)
return|;
block|}
DECL|method|forEachValue
annotation|@
name|Override
specifier|public
name|void
name|forEachValue
parameter_list|(
name|StringValueProc
name|proc
parameter_list|)
block|{
if|if
condition|(
name|freqs
operator|==
literal|null
condition|)
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
name|Float
operator|.
name|toString
argument_list|(
name|values
index|[
name|i
index|]
argument_list|)
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
else|else
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
name|Float
operator|.
name|toString
argument_list|(
name|values
index|[
name|i
index|]
argument_list|)
argument_list|,
name|freqs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|byteValue
annotation|@
name|Override
specifier|public
name|byte
name|byteValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
operator|(
name|byte
operator|)
name|value
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|shortValue
annotation|@
name|Override
specifier|public
name|short
name|shortValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
operator|(
name|short
operator|)
name|value
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|intValue
annotation|@
name|Override
specifier|public
name|int
name|intValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
operator|(
name|int
operator|)
name|value
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|longValue
annotation|@
name|Override
specifier|public
name|long
name|longValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
operator|(
name|long
operator|)
name|value
argument_list|(
name|docId
argument_list|)
return|;
block|}
DECL|method|floatValue
annotation|@
name|Override
specifier|public
name|float
name|floatValue
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
DECL|method|doubleValue
annotation|@
name|Override
specifier|public
name|double
name|doubleValue
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
return|return
operator|(
name|double
operator|)
name|value
argument_list|(
name|docId
argument_list|)
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
name|FLOAT
return|;
block|}
DECL|method|forEachValue
specifier|public
name|void
name|forEachValue
parameter_list|(
name|ValueProc
name|proc
parameter_list|)
block|{
if|if
condition|(
name|freqs
operator|==
literal|null
condition|)
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
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
else|else
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
argument_list|,
name|freqs
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|interface|ValueProc
specifier|public
specifier|static
interface|interface
name|ValueProc
block|{
DECL|method|onValue
name|void
name|onValue
parameter_list|(
name|float
name|value
parameter_list|,
name|int
name|freq
parameter_list|)
function_decl|;
block|}
DECL|method|load
specifier|public
specifier|static
name|FloatFieldData
name|load
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|String
name|field
parameter_list|,
name|FieldDataOptions
name|options
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
name|options
argument_list|,
operator|new
name|FloatTypeLoader
argument_list|()
argument_list|)
return|;
block|}
DECL|class|FloatTypeLoader
specifier|static
class|class
name|FloatTypeLoader
extends|extends
name|FieldDataLoader
operator|.
name|FreqsTypeLoader
argument_list|<
name|FloatFieldData
argument_list|>
block|{
DECL|field|terms
specifier|private
specifier|final
name|TFloatArrayList
name|terms
init|=
operator|new
name|TFloatArrayList
argument_list|()
decl_stmt|;
DECL|method|FloatTypeLoader
name|FloatTypeLoader
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
literal|0
argument_list|)
expr_stmt|;
block|}
DECL|method|collectTerm
annotation|@
name|Override
specifier|public
name|void
name|collectTerm
parameter_list|(
name|String
name|term
parameter_list|)
block|{
name|terms
operator|.
name|add
argument_list|(
name|FieldCache
operator|.
name|NUMERIC_UTILS_FLOAT_PARSER
operator|.
name|parseFloat
argument_list|(
name|term
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|buildSingleValue
annotation|@
name|Override
specifier|public
name|FloatFieldData
name|buildSingleValue
parameter_list|(
name|String
name|field
parameter_list|,
name|int
index|[]
name|order
parameter_list|)
block|{
return|return
operator|new
name|SingleValueFloatFieldData
argument_list|(
name|field
argument_list|,
name|options
argument_list|,
name|order
argument_list|,
name|terms
operator|.
name|toNativeArray
argument_list|()
argument_list|,
name|buildFreqs
argument_list|()
argument_list|)
return|;
block|}
DECL|method|buildMultiValue
annotation|@
name|Override
specifier|public
name|FloatFieldData
name|buildMultiValue
parameter_list|(
name|String
name|field
parameter_list|,
name|int
index|[]
index|[]
name|order
parameter_list|)
block|{
return|return
operator|new
name|MultiValueFloatFieldData
argument_list|(
name|field
argument_list|,
name|options
argument_list|,
name|order
argument_list|,
name|terms
operator|.
name|toNativeArray
argument_list|()
argument_list|,
name|buildFreqs
argument_list|()
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

