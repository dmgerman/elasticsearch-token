begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.xcontent.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|support
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
name|elasticsearch
operator|.
name|common
operator|.
name|Booleans
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
name|XContentParser
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
name|Map
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AbstractXContentParser
specifier|public
specifier|abstract
class|class
name|AbstractXContentParser
implements|implements
name|XContentParser
block|{
annotation|@
name|Override
DECL|method|booleanValue
specifier|public
name|boolean
name|booleanValue
parameter_list|()
throws|throws
name|IOException
block|{
name|Token
name|token
init|=
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
return|return
name|intValue
argument_list|()
operator|!=
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
return|return
name|Booleans
operator|.
name|parseBoolean
argument_list|(
name|textCharacters
argument_list|()
argument_list|,
name|textOffset
argument_list|()
argument_list|,
name|textLength
argument_list|()
argument_list|,
literal|false
comment|/* irrelevant */
argument_list|)
return|;
block|}
return|return
name|doBooleanValue
argument_list|()
return|;
block|}
DECL|method|doBooleanValue
specifier|protected
specifier|abstract
name|boolean
name|doBooleanValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|shortValue
specifier|public
name|short
name|shortValue
parameter_list|()
throws|throws
name|IOException
block|{
name|Token
name|token
init|=
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
return|return
name|Short
operator|.
name|parseShort
argument_list|(
name|text
argument_list|()
argument_list|)
return|;
block|}
return|return
name|doShortValue
argument_list|()
return|;
block|}
DECL|method|doShortValue
specifier|protected
specifier|abstract
name|short
name|doShortValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|intValue
specifier|public
name|int
name|intValue
parameter_list|()
throws|throws
name|IOException
block|{
name|Token
name|token
init|=
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
return|return
name|Integer
operator|.
name|parseInt
argument_list|(
name|text
argument_list|()
argument_list|)
return|;
block|}
return|return
name|doIntValue
argument_list|()
return|;
block|}
DECL|method|doIntValue
specifier|protected
specifier|abstract
name|int
name|doIntValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|longValue
specifier|public
name|long
name|longValue
parameter_list|()
throws|throws
name|IOException
block|{
name|Token
name|token
init|=
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|text
argument_list|()
argument_list|)
return|;
block|}
return|return
name|doLongValue
argument_list|()
return|;
block|}
DECL|method|doLongValue
specifier|protected
specifier|abstract
name|long
name|doLongValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|floatValue
specifier|public
name|float
name|floatValue
parameter_list|()
throws|throws
name|IOException
block|{
name|Token
name|token
init|=
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
return|return
name|Float
operator|.
name|parseFloat
argument_list|(
name|text
argument_list|()
argument_list|)
return|;
block|}
return|return
name|doFloatValue
argument_list|()
return|;
block|}
DECL|method|doFloatValue
specifier|protected
specifier|abstract
name|float
name|doFloatValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|doubleValue
specifier|public
name|double
name|doubleValue
parameter_list|()
throws|throws
name|IOException
block|{
name|Token
name|token
init|=
name|currentToken
argument_list|()
decl_stmt|;
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
return|return
name|Double
operator|.
name|parseDouble
argument_list|(
name|text
argument_list|()
argument_list|)
return|;
block|}
return|return
name|doDoubleValue
argument_list|()
return|;
block|}
DECL|method|doDoubleValue
specifier|protected
specifier|abstract
name|double
name|doDoubleValue
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|Override
DECL|method|textOrNull
specifier|public
name|String
name|textOrNull
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|currentToken
argument_list|()
operator|==
name|Token
operator|.
name|VALUE_NULL
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|text
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|bytesOrNull
specifier|public
name|BytesRef
name|bytesOrNull
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|currentToken
argument_list|()
operator|==
name|Token
operator|.
name|VALUE_NULL
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|bytes
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|map
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|XContentMapConverter
operator|.
name|readMap
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|mapOrdered
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapOrdered
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|XContentMapConverter
operator|.
name|readOrderedMap
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|mapAndClose
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapAndClose
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|map
argument_list|()
return|;
block|}
finally|finally
block|{
name|close
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|mapOrderedAndClose
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapOrderedAndClose
parameter_list|()
throws|throws
name|IOException
block|{
try|try
block|{
return|return
name|mapOrdered
argument_list|()
return|;
block|}
finally|finally
block|{
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

