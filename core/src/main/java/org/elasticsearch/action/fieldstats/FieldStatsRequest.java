begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.fieldstats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|fieldstats
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionRequestValidationException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ValidateActions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|broadcast
operator|.
name|BroadcastRequest
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
name|Strings
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
name|bytes
operator|.
name|BytesReference
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
name|xcontent
operator|.
name|XContentHelper
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
operator|.
name|Token
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
name|List
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|FieldStatsRequest
specifier|public
class|class
name|FieldStatsRequest
extends|extends
name|BroadcastRequest
argument_list|<
name|FieldStatsRequest
argument_list|>
block|{
DECL|field|DEFAULT_LEVEL
specifier|public
specifier|final
specifier|static
name|String
name|DEFAULT_LEVEL
init|=
literal|"cluster"
decl_stmt|;
DECL|field|fields
specifier|private
name|String
index|[]
name|fields
init|=
name|Strings
operator|.
name|EMPTY_ARRAY
decl_stmt|;
DECL|field|level
specifier|private
name|String
name|level
init|=
name|DEFAULT_LEVEL
decl_stmt|;
DECL|field|indexConstraints
specifier|private
name|IndexConstraint
index|[]
name|indexConstraints
init|=
operator|new
name|IndexConstraint
index|[
literal|0
index|]
decl_stmt|;
DECL|method|getFields
specifier|public
name|String
index|[]
name|getFields
parameter_list|()
block|{
return|return
name|fields
return|;
block|}
DECL|method|setFields
specifier|public
name|void
name|setFields
parameter_list|(
name|String
index|[]
name|fields
parameter_list|)
block|{
if|if
condition|(
name|fields
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"specified fields can't be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|fields
operator|=
name|fields
expr_stmt|;
block|}
DECL|method|getIndexConstraints
specifier|public
name|IndexConstraint
index|[]
name|getIndexConstraints
parameter_list|()
block|{
return|return
name|indexConstraints
return|;
block|}
DECL|method|setIndexConstraints
specifier|public
name|void
name|setIndexConstraints
parameter_list|(
name|IndexConstraint
index|[]
name|indexConstraints
parameter_list|)
block|{
if|if
condition|(
name|indexConstraints
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"specified index_constraints can't be null"
argument_list|)
throw|;
block|}
name|this
operator|.
name|indexConstraints
operator|=
name|indexConstraints
expr_stmt|;
block|}
DECL|method|source
specifier|public
name|void
name|source
parameter_list|(
name|BytesReference
name|content
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|IndexConstraint
argument_list|>
name|indexConstraints
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|XContentParser
name|parser
init|=
name|XContentHelper
operator|.
name|createParser
argument_list|(
name|content
argument_list|)
init|)
block|{
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|Token
name|token
init|=
name|parser
operator|.
name|nextToken
argument_list|()
decl_stmt|;
assert|assert
name|token
operator|==
name|Token
operator|.
name|START_OBJECT
assert|;
for|for
control|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
init|;
name|token
operator|!=
name|Token
operator|.
name|END_OBJECT
condition|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
control|)
block|{
switch|switch
condition|(
name|token
condition|)
block|{
case|case
name|FIELD_NAME
case|:
name|fieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
break|break;
case|case
name|START_OBJECT
case|:
if|if
condition|(
literal|"index_constraints"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|parseIndexContraints
argument_list|(
name|indexConstraints
argument_list|,
name|parser
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
break|break;
case|case
name|START_ARRAY
case|:
if|if
condition|(
literal|"fields"
operator|.
name|equals
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
if|if
condition|(
name|token
operator|.
name|isValue
argument_list|()
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
name|parser
operator|.
name|text
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unexpected token ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unknown field ["
operator|+
name|fieldName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unexpected token ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
name|this
operator|.
name|fields
operator|=
name|fields
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|fields
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
name|this
operator|.
name|indexConstraints
operator|=
name|indexConstraints
operator|.
name|toArray
argument_list|(
operator|new
name|IndexConstraint
index|[
name|indexConstraints
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
DECL|method|parseIndexContraints
specifier|private
name|void
name|parseIndexContraints
parameter_list|(
name|List
argument_list|<
name|IndexConstraint
argument_list|>
name|indexConstraints
parameter_list|,
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|Token
name|token
init|=
name|parser
operator|.
name|currentToken
argument_list|()
decl_stmt|;
assert|assert
name|token
operator|==
name|Token
operator|.
name|START_OBJECT
assert|;
name|String
name|field
init|=
literal|null
decl_stmt|;
name|String
name|currentName
init|=
literal|null
decl_stmt|;
for|for
control|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
init|;
name|token
operator|!=
name|Token
operator|.
name|END_OBJECT
condition|;
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
control|)
block|{
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|field
operator|=
name|currentName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
for|for
control|(
name|Token
name|fieldToken
init|=
name|parser
operator|.
name|nextToken
argument_list|()
init|;
name|fieldToken
operator|!=
name|Token
operator|.
name|END_OBJECT
condition|;
name|fieldToken
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
control|)
block|{
if|if
condition|(
name|fieldToken
operator|==
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|fieldToken
operator|==
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
name|IndexConstraint
operator|.
name|Property
name|property
init|=
name|IndexConstraint
operator|.
name|Property
operator|.
name|parse
argument_list|(
name|currentName
argument_list|)
decl_stmt|;
name|String
name|value
init|=
literal|null
decl_stmt|;
name|String
name|optionalFormat
init|=
literal|null
decl_stmt|;
name|IndexConstraint
operator|.
name|Comparison
name|comparison
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Token
name|propertyToken
init|=
name|parser
operator|.
name|nextToken
argument_list|()
init|;
name|propertyToken
operator|!=
name|Token
operator|.
name|END_OBJECT
condition|;
name|propertyToken
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
control|)
block|{
if|if
condition|(
name|propertyToken
operator|.
name|isValue
argument_list|()
condition|)
block|{
if|if
condition|(
literal|"format"
operator|.
name|equals
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
condition|)
block|{
name|optionalFormat
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|comparison
operator|=
name|IndexConstraint
operator|.
name|Comparison
operator|.
name|parse
argument_list|(
name|parser
operator|.
name|currentName
argument_list|()
argument_list|)
expr_stmt|;
name|value
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|propertyToken
operator|!=
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unexpected token ["
operator|+
name|propertyToken
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
name|indexConstraints
operator|.
name|add
argument_list|(
operator|new
name|IndexConstraint
argument_list|(
name|field
argument_list|,
name|property
argument_list|,
name|comparison
argument_list|,
name|value
argument_list|,
name|optionalFormat
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unexpected token ["
operator|+
name|fieldToken
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unexpected token ["
operator|+
name|token
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
DECL|method|level
specifier|public
name|String
name|level
parameter_list|()
block|{
return|return
name|level
return|;
block|}
DECL|method|level
specifier|public
name|void
name|level
parameter_list|(
name|String
name|level
parameter_list|)
block|{
name|this
operator|.
name|level
operator|=
name|level
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|validate
specifier|public
name|ActionRequestValidationException
name|validate
parameter_list|()
block|{
name|ActionRequestValidationException
name|validationException
init|=
name|super
operator|.
name|validate
argument_list|()
decl_stmt|;
if|if
condition|(
literal|"cluster"
operator|.
name|equals
argument_list|(
name|level
argument_list|)
operator|==
literal|false
operator|&&
literal|"indices"
operator|.
name|equals
argument_list|(
name|level
argument_list|)
operator|==
literal|false
condition|)
block|{
name|validationException
operator|=
name|ValidateActions
operator|.
name|addValidationError
argument_list|(
literal|"invalid level option ["
operator|+
name|level
operator|+
literal|"]"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|fields
operator|==
literal|null
operator|||
name|fields
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|validationException
operator|=
name|ValidateActions
operator|.
name|addValidationError
argument_list|(
literal|"no fields specified"
argument_list|,
name|validationException
argument_list|)
expr_stmt|;
block|}
return|return
name|validationException
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
name|super
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|fields
operator|=
name|in
operator|.
name|readStringArray
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
name|indexConstraints
operator|=
operator|new
name|IndexConstraint
index|[
name|size
index|]
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
name|indexConstraints
index|[
name|i
index|]
operator|=
operator|new
name|IndexConstraint
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
name|level
operator|=
name|in
operator|.
name|readString
argument_list|()
expr_stmt|;
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeStringArrayNullable
argument_list|(
name|fields
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|indexConstraints
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexConstraint
name|indexConstraint
range|:
name|indexConstraints
control|)
block|{
name|out
operator|.
name|writeString
argument_list|(
name|indexConstraint
operator|.
name|getField
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|indexConstraint
operator|.
name|getProperty
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeByte
argument_list|(
name|indexConstraint
operator|.
name|getComparison
argument_list|()
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeString
argument_list|(
name|indexConstraint
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|out
operator|.
name|getVersion
argument_list|()
operator|.
name|onOrAfter
argument_list|(
name|Version
operator|.
name|V_2_0_1
argument_list|)
condition|)
block|{
name|out
operator|.
name|writeOptionalString
argument_list|(
name|indexConstraint
operator|.
name|getOptionalFormat
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|out
operator|.
name|writeString
argument_list|(
name|level
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

