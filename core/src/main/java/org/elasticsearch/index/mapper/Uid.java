begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|BytesRefBuilder
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
name|BytesRefs
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
DECL|class|Uid
specifier|public
specifier|final
class|class
name|Uid
block|{
DECL|field|DELIMITER
specifier|public
specifier|static
specifier|final
name|char
name|DELIMITER
init|=
literal|'#'
decl_stmt|;
DECL|field|DELIMITER_BYTE
specifier|public
specifier|static
specifier|final
name|byte
name|DELIMITER_BYTE
init|=
literal|0x23
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|id
specifier|private
specifier|final
name|String
name|id
decl_stmt|;
DECL|method|Uid
specifier|public
name|Uid
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
block|}
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|type
return|;
block|}
DECL|method|id
specifier|public
name|String
name|id
parameter_list|()
block|{
return|return
name|id
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
name|Uid
name|uid
init|=
operator|(
name|Uid
operator|)
name|o
decl_stmt|;
if|if
condition|(
name|id
operator|!=
literal|null
condition|?
operator|!
name|id
operator|.
name|equals
argument_list|(
name|uid
operator|.
name|id
argument_list|)
else|:
name|uid
operator|.
name|id
operator|!=
literal|null
condition|)
return|return
literal|false
return|;
if|if
condition|(
name|type
operator|!=
literal|null
condition|?
operator|!
name|type
operator|.
name|equals
argument_list|(
name|uid
operator|.
name|type
argument_list|)
else|:
name|uid
operator|.
name|type
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
name|int
name|result
init|=
name|type
operator|!=
literal|null
condition|?
name|type
operator|.
name|hashCode
argument_list|()
else|:
literal|0
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
operator|(
name|id
operator|!=
literal|null
condition|?
name|id
operator|.
name|hashCode
argument_list|()
else|:
literal|0
operator|)
expr_stmt|;
return|return
name|result
return|;
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
name|createUid
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
return|;
block|}
DECL|method|toBytesRef
specifier|public
name|BytesRef
name|toBytesRef
parameter_list|()
block|{
return|return
name|createUidAsBytes
argument_list|(
name|type
argument_list|,
name|id
argument_list|)
return|;
block|}
DECL|method|createUid
specifier|public
specifier|static
name|Uid
name|createUid
parameter_list|(
name|String
name|uid
parameter_list|)
block|{
name|int
name|delimiterIndex
init|=
name|uid
operator|.
name|indexOf
argument_list|(
name|DELIMITER
argument_list|)
decl_stmt|;
comment|// type is not allowed to have # in it..., ids can
return|return
operator|new
name|Uid
argument_list|(
name|uid
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|delimiterIndex
argument_list|)
argument_list|,
name|uid
operator|.
name|substring
argument_list|(
name|delimiterIndex
operator|+
literal|1
argument_list|)
argument_list|)
return|;
block|}
DECL|method|createUidAsBytes
specifier|public
specifier|static
name|BytesRef
name|createUidAsBytes
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
return|return
name|createUidAsBytes
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|type
argument_list|)
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|id
argument_list|)
argument_list|)
return|;
block|}
DECL|method|createUidAsBytes
specifier|public
specifier|static
name|BytesRef
name|createUidAsBytes
parameter_list|(
name|String
name|type
parameter_list|,
name|BytesRef
name|id
parameter_list|)
block|{
return|return
name|createUidAsBytes
argument_list|(
operator|new
name|BytesRef
argument_list|(
name|type
argument_list|)
argument_list|,
name|id
argument_list|)
return|;
block|}
DECL|method|createUidAsBytes
specifier|public
specifier|static
name|BytesRef
name|createUidAsBytes
parameter_list|(
name|BytesRef
name|type
parameter_list|,
name|BytesRef
name|id
parameter_list|)
block|{
specifier|final
name|BytesRef
name|ref
init|=
operator|new
name|BytesRef
argument_list|(
name|type
operator|.
name|length
operator|+
literal|1
operator|+
name|id
operator|.
name|length
argument_list|)
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|type
operator|.
name|bytes
argument_list|,
name|type
operator|.
name|offset
argument_list|,
name|ref
operator|.
name|bytes
argument_list|,
literal|0
argument_list|,
name|type
operator|.
name|length
argument_list|)
expr_stmt|;
name|ref
operator|.
name|offset
operator|=
name|type
operator|.
name|length
expr_stmt|;
name|ref
operator|.
name|bytes
index|[
name|ref
operator|.
name|offset
operator|++
index|]
operator|=
name|DELIMITER_BYTE
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|id
operator|.
name|bytes
argument_list|,
name|id
operator|.
name|offset
argument_list|,
name|ref
operator|.
name|bytes
argument_list|,
name|ref
operator|.
name|offset
argument_list|,
name|id
operator|.
name|length
argument_list|)
expr_stmt|;
name|ref
operator|.
name|offset
operator|=
literal|0
expr_stmt|;
name|ref
operator|.
name|length
operator|=
name|ref
operator|.
name|bytes
operator|.
name|length
expr_stmt|;
return|return
name|ref
return|;
block|}
DECL|method|createUidsForTypesAndId
specifier|public
specifier|static
name|BytesRef
index|[]
name|createUidsForTypesAndId
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|types
parameter_list|,
name|Object
name|id
parameter_list|)
block|{
return|return
name|createUidsForTypesAndIds
argument_list|(
name|types
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|id
argument_list|)
argument_list|)
return|;
block|}
DECL|method|createUidsForTypesAndIds
specifier|public
specifier|static
name|BytesRef
index|[]
name|createUidsForTypesAndIds
parameter_list|(
name|Collection
argument_list|<
name|String
argument_list|>
name|types
parameter_list|,
name|Collection
argument_list|<
name|?
argument_list|>
name|ids
parameter_list|)
block|{
name|BytesRef
index|[]
name|uids
init|=
operator|new
name|BytesRef
index|[
name|types
operator|.
name|size
argument_list|()
operator|*
name|ids
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|BytesRefBuilder
name|typeBytes
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
name|BytesRefBuilder
name|idBytes
init|=
operator|new
name|BytesRefBuilder
argument_list|()
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|String
name|type
range|:
name|types
control|)
block|{
name|typeBytes
operator|.
name|copyChars
argument_list|(
name|type
argument_list|)
expr_stmt|;
for|for
control|(
name|Object
name|id
range|:
name|ids
control|)
block|{
name|uids
index|[
name|index
operator|++
index|]
operator|=
name|Uid
operator|.
name|createUidAsBytes
argument_list|(
name|typeBytes
operator|.
name|get
argument_list|()
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|id
argument_list|,
name|idBytes
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|uids
return|;
block|}
DECL|method|createUid
specifier|public
specifier|static
name|String
name|createUid
parameter_list|(
name|String
name|type
parameter_list|,
name|String
name|id
parameter_list|)
block|{
return|return
name|type
operator|+
name|DELIMITER
operator|+
name|id
return|;
block|}
block|}
end_class

end_unit

