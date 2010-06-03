begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.field.shorts
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|field
operator|.
name|shorts
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
name|TShortArrayList
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
DECL|class|ShortFieldData
specifier|public
specifier|abstract
class|class
name|ShortFieldData
extends|extends
name|FieldData
block|{
DECL|field|EMPTY_SHORT_ARRAY
specifier|static
specifier|final
name|short
index|[]
name|EMPTY_SHORT_ARRAY
init|=
operator|new
name|short
index|[
literal|0
index|]
decl_stmt|;
DECL|field|values
specifier|protected
specifier|final
name|short
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
DECL|method|ShortFieldData
specifier|protected
name|ShortFieldData
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|FieldDataOptions
name|options
parameter_list|,
name|short
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
name|short
name|value
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
DECL|method|values
specifier|abstract
specifier|public
name|short
index|[]
name|values
parameter_list|(
name|int
name|docId
parameter_list|)
function_decl|;
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
name|SHORT
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
name|short
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
name|ShortFieldData
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
name|ShortTypeLoader
argument_list|()
argument_list|)
return|;
block|}
DECL|class|ShortTypeLoader
specifier|static
class|class
name|ShortTypeLoader
extends|extends
name|FieldDataLoader
operator|.
name|FreqsTypeLoader
argument_list|<
name|ShortFieldData
argument_list|>
block|{
DECL|field|terms
specifier|private
specifier|final
name|TShortArrayList
name|terms
init|=
operator|new
name|TShortArrayList
argument_list|()
decl_stmt|;
DECL|method|ShortTypeLoader
name|ShortTypeLoader
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
operator|(
name|short
operator|)
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
operator|(
name|short
operator|)
name|FieldCache
operator|.
name|NUMERIC_UTILS_INT_PARSER
operator|.
name|parseInt
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
name|ShortFieldData
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
name|SingleValueShortFieldData
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
name|ShortFieldData
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
name|MultiValueShortFieldData
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

